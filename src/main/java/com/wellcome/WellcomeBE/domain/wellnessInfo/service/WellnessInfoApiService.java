package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.wellcome.WellcomeBE.domain.like.repository.LikedRepository;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.domain.member.repository.MemberRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request.WellnessInfoListRequest;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoBasicResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoResponse;

import com.wellcome.WellcomeBE.global.OpeningHoursUtils;
import com.wellcome.WellcomeBE.global.exception.CustomException;
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
                .isLiked(likedRepository.findLikedByWellnessInfoAndMember(wellness,tokenProvider.getMember()))
                .isOpen(openStatus.getIsOpen())
                .openDetail(openStatus.getOpenDetail())
                .tel(wellness.getTel())
                .website(placeResult.getWebsite())
                .build();
    }

}
