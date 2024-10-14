package com.wellcome.WellcomeBE.domain.tripPlanPlace.service;

import com.wellcome.WellcomeBE.domain.community.repository.CommunityRepository;
import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request.TripPlanPlaceDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request.TripPlanPlaceCreateRequest;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



@Service
@RequiredArgsConstructor
public class TripPlanPlaceService {

    private final WellnessInfoRepository wellnessInfoRepository;
    private final TripPlanRepository tripPlanRepository;
    private final TripPlanPlaceRepository tripPlanPlaceRepository;
    private final CommunityRepository communityRepository;
    private final TokenProvider tokenProvider;

    public void createTripPlanPlace(Long planId, TripPlanPlaceCreateRequest request){
        // 웰니스 정보 존재 확인
        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(request.getWellnessInfoId()).orElseThrow(() -> new CustomException(CustomErrorCode.WELLNESS_INFO_NOT_FOUND));

        // 여행 폴더 생성 여부 확인
        TripPlan tripPlan = tripPlanRepository.findById(planId).orElseThrow(() -> new CustomException(CustomErrorCode.TRIP_PLAN_NOT_FOUND));

        // 기존에 추가된 여행지 여부 확인
        if(tripPlanPlaceRepository.existsByTripPlanAndWellnessInfoAndMember(tripPlan,wellnessInfo,tokenProvider.getMember())){
            throw new CustomException(CustomErrorCode.TRIP_PLAN_PLACE_DUPLICATION);
        }


        TripPlanPlace tripPlanPlace = TripPlanPlace.builder()
                .tripPlan(tripPlan)
                .wellnessInfo(wellnessInfo)
                .build();
        tripPlanPlaceRepository.save(tripPlanPlace);
    }

    @Transactional
    public void deleteTripPlanPlace(Long planId, TripPlanPlaceDeleteRequest request) {

        Member currentMember = tokenProvider.getMember();

        List<Long> tripPlanPlaceIdList = request.getDeletePlanPlaceIdList();

        // 1. 여행 폴더 존재 유무 및 유저 권한 확인
        TripPlan tripPlan = tripPlanRepository.findByIdAndMemberId(planId, currentMember.getId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ACCESS_DENIED));

        // 2. 삭제하려는 여행지 리스트가 모두 해당 여행 폴더에 속해 있는지 확인
        List<TripPlanPlace> tripPlanPlaceList = tripPlanPlaceRepository.findByIdIn(tripPlanPlaceIdList);

        // TripPlanPlaceId가 존재하지 않을 경우 예외 처리
        if (tripPlanPlaceList.size() != tripPlanPlaceIdList.size()) {
            throw new CustomException(CustomErrorCode.TRIP_PLAN_PLACE_NOT_FOUND, "존재하지 않는 여행지 식별자가 포함되어 있습니다.");
        }

        // 삭제하려는 여행지가 요청된 TripPlan에 속하지 않는 경우 예외 처리
        tripPlanPlaceList.stream()
                .filter(tripPlanPlace -> !tripPlanPlace.getTripPlan().getId().equals(tripPlan.getId()))
                .findAny()
                .ifPresent(tripPlanPlace -> {
                    throw new CustomException(CustomErrorCode.TRIP_PLAN_PLACE_NOT_IN_FOLDER);
                });

        // 3. 커뮤니티 참조 여부 확인
        boolean isReferencedInCommunity = !communityRepository.findByTripPlan(tripPlan).isEmpty();

        if (isReferencedInCommunity) {
            // 4-1. 커뮤니티에서 참조 중일 경우, 삭제 대신 상태를 비활성화 처리
            tripPlanPlaceList.forEach(TripPlanPlace::markAsInactive);
            tripPlanPlaceRepository.saveAll(tripPlanPlaceList);
        } else {
            // 4-2. 커뮤니티에서 참조하지 않을 경우, 여행지 일괄 삭제(이미 삭제된 여행지일 경우 무시)
            tripPlanPlaceRepository.deleteAllByIdInBatch(tripPlanPlaceIdList);
        }
    }
}
