package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.Article.dto.ArticleResponse;
import com.wellcome.WellcomeBE.domain.Article.repository.ArticleRepository;
import com.wellcome.WellcomeBE.domain.community.dto.response.ReviewPostResponse;
import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request.WellnessInfoListRequest;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.*;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.global.type.ImgSavedType;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.WELLNESS_INFO_NOT_FOUND;


@Service
@RequiredArgsConstructor
@Slf4j
public class WellnessInfoApiService {

    private final WellnessInfoRepository wellnessInfoRepository;
    private final TokenProvider tokenProvider;
    private final GoogleMapInfoService googleMapInfoService;
    private final LikedRepository likedRepository;
    private final WellnessInfoImgRepository wellnessInfoImgRepository;
    private final TripPlanPlaceRepository tripPlanPlaceRepository;
    private final CommunityRepository communityRepository;
    private final ArticleRepository articleRepository;
    private static final int REVIEW_POST_BRIEF_LIST_SIZE = 3;


    /**
     * 웰니스 장소 추천 목록 조회
     * - 로그인 가정
     */
    public WellnessInfoResponse getWellnessInfoList(int page, WellnessInfoListRequest request, ImgSavedType savedType) {

        // 사용자 정보 조회
        Member member = tokenProvider.getMember();

        // 웰니스 정보 조회
        List<Thema> themaList = request.getThemaList();
        List<Sigungu> sigunguList = request.getSigunguList();

        boolean isThemaListEmpty = themaList == null || themaList.isEmpty();
        boolean isSigunguListEmpty = sigunguList == null || sigunguList.isEmpty();

        Page<Object[]> data;
        Pageable pageable = PageRequest.of(page, 10);
        List<Object[]> types;

        if (isThemaListEmpty && isSigunguListEmpty) {
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
                .map(objects -> WellnessInfoResponse.WellnessInfoList.WellnessInfoItem.from((WellnessInfo) objects[0], (Boolean) objects[1], savedType))
                .collect(Collectors.toList());
        WellnessInfoResponse.WellnessInfoList WellnessInfoList = WellnessInfoResponse.WellnessInfoList.from(
                data.getTotalElements(), data.getNumber(),
                data.hasPrevious(), data.hasNext(),
                wellnessInfoItemList);

        return WellnessInfoResponse.from(new ArrayList<>(searchedThemaSet), new ArrayList<>(searchedSigunguSet), request, WellnessInfoList);
    }

    /**
     * [FEAT] 웰니스 장소 상세 조회(1) - 기본 정보 조회
     */
    @Transactional
    public WellnessInfoBasicResponse getWellnessInfoBasic(Long wellnessInfoId) {


        // 1. 웰니스 정보 가져오기
        WellnessInfo wellness = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.WELLNESS_INFO_NOT_FOUND));

        // 2. Google Place API를 통해 장소 세부 정보 가져오기
        String parentId = wellness.getParentId();
        PlaceReviewResponse.PlaceResult placeResult =
                parentId == null ? null : googleMapInfoService.getPlaceDetails(parentId).block().getResult();

        // 3. 웰니스 이미지 목록 가져오기
        List<String> wellnessInfoImg = wellnessInfoImgRepository.findByWellnessInfo(wellness);
        boolean liked = likedRepository.existsByWellnessInfoAndMember(wellness, tokenProvider.getMember());

        // 조회 수 추가
        wellness.updateViewNum();

        return WellnessInfoBasicResponse.from(wellness, wellnessInfoImg, placeResult, liked);
    }

    /**
     * 웰니스 장소 상세 조회(3) - 구글 리뷰 조회
     */
    public WellnessInfoGoogleReviewResponse getWellnessInfoGoogleReviews(Long wellnessInfoId) {

        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new CustomException(WELLNESS_INFO_NOT_FOUND));

        Double rating = null;
        List<WellnessInfoGoogleReviewResponse.GoogleReview> reviewList = new ArrayList<>();

        if (wellnessInfo.getParentId() != null) {
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
     * [FEAT] 웰니스 장소 상세 조회(2) - 주변 장소 추천
     */
    public WellnessInfoNearbyList getSurroundingWellnessInfo(Long wellnessInfoId) {
        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.WELLNESS_INFO_NOT_FOUND));

        Double mapX = wellnessInfo.getMapX();
        Double mapY = wellnessInfo.getMapY();
        Double radius = 10.0; // 예시로 10km 설정

        List<WellnessInfo> nearbyWellness = wellnessInfoRepository.findTop6NearbyWellnessInfo(mapX, mapY, wellnessInfoId, radius);


        List<WellnessInfoNearbyList.WellnessNearbyDto> wellnessNearbyDtoList = nearbyWellness.stream()
                .map(place -> {
                    PlaceReviewResponse.PlaceResult placeResult = null;
                    if (place.getParentId() != null) {
                        placeResult = googleMapInfoService.getPlaceDetails(place.getParentId()).block().getResult();
                    }
                    double distance = calculateDistance(mapY, mapX, place.getMapY(), place.getMapX());

                    return WellnessInfoNearbyList.WellnessNearbyDto.form(place, placeResult, distance);
                })
                .collect(Collectors.toList());
        return WellnessInfoNearbyList.from(wellnessNearbyDtoList);
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

    /**
     * 웰니스 장소 상세 조회(4) - 후기 게시글 조회
     */
    public WellnessInfoReviewPostResponse getWellnessInfoReviewPosts(Long wellnessInfoId) {

        // 유효한 웰니스 정보인지 확인
        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new CustomException(WELLNESS_INFO_NOT_FOUND));

        // 해당 웰니스 장소에 대해 개별 리뷰가 작성된 여행 폴더 조회
        List<TripPlan> reviewedPlan = tripPlanPlaceRepository.findByWellnessInfoAndReviewConditionsExist(wellnessInfo);

        // 후기 게시글 조회
        PageRequest pageRequest = PageRequest.of(0, REVIEW_POST_BRIEF_LIST_SIZE);
        List<ReviewPostResponse.ReviewPostBrief> reviewList =
                communityRepository.findByTripPlanInOrderByCreatedAtDesc(pageRequest, reviewedPlan)
                        .stream()
                        .map(ReviewPostResponse.ReviewPostBrief::from)
                        .collect(Collectors.toList());

        return new WellnessInfoReviewPostResponse(reviewList);
    }

    /**
     * 웰니스 장소 상세 조회(5) - 아티클 조회
     */

    public WellnessInfoArticleResponse getWellnessInfoArticle(Long wellnessInfoId){
        // 유효한 웰니스 정보인지 확인
        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(wellnessInfoId)
                .orElseThrow(() -> new CustomException(WELLNESS_INFO_NOT_FOUND));

        // 해당 웰니스 장소에 대한 아티클 목록 조회
        List<ArticleResponse.ArticleItem> top5Article = articleRepository.findTop5ByWellnessInfoOrderByCreatedAt(wellnessInfo).stream().map(ArticleResponse.ArticleItem::from).toList();
        return new WellnessInfoArticleResponse(top5Article);
    }



}
