package com.wellcome.WellcomeBE.domain.tripPlanPlace.service;

import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request.TripPlanPlaceRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TripPlanPlaceService {

    private final WellnessInfoRepository wellnessInfoRepository;
    private final TripPlanRepository tripPlanRepository;
    private final TripPlanPlaceRepository tripPlanPlaceRepository;

    public void createTripPlanPlace(Long planId, TripPlanPlaceRequest request){
        WellnessInfo wellnessInfo = wellnessInfoRepository.findById(request.getWellnessInfoId()).orElseThrow(() -> new CustomException(CustomErrorCode.WELLNESS_INFO_NOT_FOUND));
        TripPlan tripPlan = tripPlanRepository.findById(planId).orElseThrow(() -> new CustomException(CustomErrorCode.TRIP_PLAN_NOT_FOUND));
        TripPlanPlace tripPlanPlace = TripPlanPlace.builder()
                .tripPlan(tripPlan)
                .wellnessInfo(wellnessInfo)
                .build();
        tripPlanPlaceRepository.save(tripPlanPlace);
    }
}
