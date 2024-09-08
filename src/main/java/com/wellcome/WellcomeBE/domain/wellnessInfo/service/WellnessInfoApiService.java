package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request.WellnessInfoListRequest;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessNearbyDto;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoBasicResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoGoogleReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.global.OpeningHoursUtils;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.WELLNESS_INFO_NOT_FOUND;


@Service
@RequiredArgsConstructor
@Slf4j
public class WellnessInfoApiService {

    private final WellnessInfoRepository wellnessInfoRepository;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final GoogleMapInfoService googleMapInfoService;
    private final LikedRepository likedRepository;
    private final WellnessInfoImgRepository wellnessInfoImgRepository;


    /**
     * 웰니스 장소 추천 목록 조회
     * - 로그인 가정
     */
    public WellnessInfoResponse getWellnessInfoList(int page, WellnessInfoListRequest request) {

        // 사용자 정보 조회
        Member member = tokenProvider.getMember();

        // 웰니스 정보 조회
        List<Thema> themaList = request.getThemaList();
        List<Sigungu> sigunguList = request.getSigunguList();

        boolean isThemaListEmpty = themaList == null || themaList.isEmpty();
        boolean isSigunguListEmpty = sigunguList == null || sigunguList.isEmpty();

        Page<Object[]> data;
        Pageable pageable = PageRequest.of(page, 5);
        List<Object[]> types;

        if(isThemaListEmpty && isSigunguListEmpty){
            data = wellnessInfoRepository.findAllByOrderByViewDesc(pageable, member);
            types = wellnessInfoRepository.findDistinctAllThemaAndSigungu();
        } else if (isThemaListEmpty) { //SigunguList로 필터링
            data = wellnessInfoRepository.findBySigungu(pageable, member, sigunguList);
            types = wellnessInfoRepository.findDistinctThemaAndSigunguBySigungu(sigunguList);
        } else if (isSigunguListEmpty) { //ThemaList로 필터링
            data = wellnessInfoRepository.findByThema(pageable, member, themaList);
            types = wellnessInfoRepository.findDistinctThemaAndSigunguByThema(themaList);
        } else {
            data = wellnessInfoRepository.findByThemaAndSigungu(pageable, member, themaList, sigunguList);
            types = wellnessInfoRepository.findDistinctThemaAndSigungu(themaList, sigunguList);
        }

        Set<Thema> searchedThemaSet = new HashSet<>();
        Set<Sigungu> searchedSigunguSet = new HashSet<>();
        for (Object[] type : types) {
            searchedThemaSet.add((Thema) type[0]);
            searchedSigunguSet.add((Sigungu) type[1]);
        }

        List<WellnessInfoResponse.WellnessInfoList.WellnessInfoItem> wellnessInfoItemList = data.stream()
                .map(objects -> WellnessInfoResponse.WellnessInfoList.WellnessInfoItem.from((WellnessInfo) objects[0], (Boolean) objects[1]))
                .collect(Collectors.toList());
        WellnessInfoResponse.WellnessInfoList WellnessInfoList = WellnessInfoResponse.WellnessInfoList.from(
                data.getTotalElements(), data.getNumber(),
                data.hasPrevious(), data.hasNext(),
                wellnessInfoItemList);

        return WellnessInfoResponse.from(new ArrayList<>(searchedThemaSet), new ArrayList<>(searchedSigunguSet), request, WellnessInfoList);
    }

    /**
     *  [FEAT] 웰니스 장소 상세 조회(1) - 기본 정보 조회
     */
    public WellnessInfoBasicResponse getWellnessInfoBasic(Long wellnessInfoId){


        // 1. 웰니스 정보 가져오기
        WellnessInfo wellness = wellnessInfoRepository.findById(wellnessInfoId)
                 .orElseThrow(() -> new CustomException(CustomErrorCode.WELLNESS_INFO_NOT_FOUND));

        // 2. Google Place API를 통해 장소 세부 정보 가져오기
        String parentId = wellness.getParentId();
        PlaceReviewResponse.PlaceResult placeResult =
                parentId == null ? null : googleMapInfoService.getPlaceDetails(parentId).block().getResult();

        // 3. 웰니스 이미지 목록 가져오기
        List<String> wellnessInfoImg = wellnessInfoImgRepository.findByWellnessInfo(wellness);

        // 4. JSON 데이터에서 운영 시간 찾기
        OpeningHoursUtils.OpenStatus openStatus = OpeningHoursUtils.getOpenStatus(placeResult);


        return WellnessInfoBasicResponse.builder()
                .wellnessInfoId(wellness.getId())
                .thumbnailUrl(wellness.getThumbnailUrl())
                .imgList(wellnessInfoImg)
                .title(wellness.getTitle())
                .category(wellness.getCategory().getName())
                .address(wellness.getAddress())
                .mapX(wellness.getMapX())
                .mapY(wellness.getMapY())
                .isLiked(likedRepository.existsByWellnessInfoAndMember(wellness,tokenProvider.getMember()))
                .isOpen(openStatus.getIsOpen())
                .openDetail(openStatus.getOpenDetail())
                .tel(wellness.getTel())
                .website(placeResult.getWebsite())
                .build();
    }

    /**
     * 웰니스 장소 상세 조회(3) - 구글 리뷰 조회
     */
    public WellnessInfoGoogleReviewResponse getWellnessInfoGoogleReviews(Long wellnessInfoId) {
        
        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellnessInfoId)
                        .orElseThrow(() -> new CustomException(WELLNESS_INFO_NOT_FOUND));

        Double rating = null;
        List<WellnessInfoGoogleReviewResponse.GoogleReview> reviewList = new ArrayList<>();

        if(wellnessInfo.getParentId() != null){
            PlaceReviewResponse.PlaceResult placeResult = googleMapInfoService.getPlaceDetails(wellnessInfo.getParentId()).block().getResult();
            rating = placeResult.getRating();
            reviewList = Optional.ofNullable(placeResult.getReviews())
                    .filter(reviews -> !reviews.isEmpty())
                    .map(reviews -> reviews.stream()
                            .map(WellnessInfoGoogleReviewResponse.GoogleReview::from)
                            .collect(Collectors.toList()))
                    .orElseGet(ArrayList::new);
        }

        return WellnessInfoGoogleReviewResponse.from(rating, reviewList);
    }

    /**
     *  [FEAT] 웰니스 장소 상세 조회(2) - 주변 장소 추천
     */
    public List<WellnessNearbyDto> getSurroundingWellnessInfo(Long wellnessInfoId) {
        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.WELLNESS_INFO_NOT_FOUND));

        Double mapX = wellnessInfo.getMapX();
        Double mapY = wellnessInfo.getMapY();
        Double radius = 5.0; // 예시로 5km 설정

        List<WellnessInfo> nearbyWellness = wellnessInfoRepository.findTop6NearbyWellnessInfo(mapX, mapY, wellnessInfoId, radius);

        return nearbyWellness.stream()
                .map(WellnessInfo -> {
                    PlaceReviewResponse.PlaceResult placeResult = googleMapInfoService.getPlaceDetails(wellnessInfo.getParentId()).block().getResult();
                    double distance = calculateDistance(mapY, mapX, WellnessInfo.getMapY(), WellnessInfo.getMapX());

                    return WellnessNearbyDto.form(WellnessInfo, placeResult, distance);
                })
                .collect(Collectors.toList());
    }

    private static double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371; // 지구의 반지름 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // km 단위로 변환
    }

}
