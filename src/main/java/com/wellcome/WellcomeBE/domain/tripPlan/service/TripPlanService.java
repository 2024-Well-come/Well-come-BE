package com.wellcome.WellcomeBE.domain.tripPlan.service;

import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;
    private final TokenProvider tokenProvider;

    public void createTripPlan(TripPlanRequest request){
        //TODO: 무작위 생성 폴더 이름 추가

        TripPlan tripPlan = TripPlan.builder()
                .title(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .member( tokenProvider.getMember())
                .build();
        tripPlanRepository.save(tripPlan);
    }

    public TripPlanResponse.TripPlanListResponse getTripPlanList(){
        List<TripPlan> result = tripPlanRepository.findByMember(tokenProvider.getMember());
        List<TripPlanResponse.TripPlanPlaceItem> planPlaceItems = result.stream().map(TripPlanResponse.TripPlanPlaceItem::from).collect(Collectors.toList());
        return TripPlanResponse.TripPlanListResponse.builder().tripPlanList(planPlaceItems).build();
    }
}
