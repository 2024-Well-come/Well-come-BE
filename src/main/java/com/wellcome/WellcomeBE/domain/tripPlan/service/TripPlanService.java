package com.wellcome.WellcomeBE.domain.tripPlan.service;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.ACCESS_DENIED;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;
    private final TripPlanPlaceRepository tripPlanPlaceRepository;
    private final TokenProvider tokenProvider;

    public void createTripPlan(TripPlanRequest request){
        //TODO: 무작위 생성 폴더 이름 추가

        TripPlan tripPlan = TripPlan.builder()
                .title(request.getName())
                .startDate(request.getTripStartDate())
                .endDate(request.getTripEndDate())
                .member( tokenProvider.getMember())
                .build();
        tripPlanRepository.save(tripPlan);
    }

    public TripPlanResponse.TripPlanListResponse getTripPlanList(){
        List<TripPlan> result = tripPlanRepository.findByMember(tokenProvider.getMember());
        List<TripPlanResponse.TripPlanPlaceItem> planPlaceItems = result.stream().map(TripPlanResponse.TripPlanPlaceItem::from).collect(Collectors.toList());
        return TripPlanResponse.TripPlanListResponse.builder().tripPlanList(planPlaceItems).build();
    }

    @Transactional
    public void deleteTripPlan(TripPlanDeleteRequest request) {

        Member currentMember = tokenProvider.getMember();

        // 여행 폴더 삭제 권한 확인
        List<Long> tripPlanIdList = request.getDeletePlanList();

        boolean allMatch = tripPlanRepository.findByIdIn(tripPlanIdList).stream()
                .allMatch(tripPlan -> tripPlan.getMember().getId().equals(currentMember.getId()));
        if (!allMatch) {
            throw new CustomException(ACCESS_DENIED);
        }

        // 여행 폴더 내 여행지 삭제
        List<TripPlanPlace> tripPlanPlaceList = tripPlanPlaceRepository.findByIdIn(tripPlanIdList);
        tripPlanPlaceRepository.deleteAllInBatch(tripPlanPlaceList);

        // 여행 폴더 일괄 삭제 처리
        tripPlanRepository.deleteAllByIdInBatch(tripPlanIdList);
    }

}
