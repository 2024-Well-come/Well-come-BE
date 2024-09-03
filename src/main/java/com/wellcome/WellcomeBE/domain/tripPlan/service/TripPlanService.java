package com.wellcome.WellcomeBE.domain.tripPlan.service;

import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;

    public void createTripPlan(TripPlanRequest request){
        //TODO: 무작위 생성 폴더 이름 추가

        TripPlan tripPlan = TripPlan.builder()
                .title(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
        tripPlanRepository.save(tripPlan);
    }
}
