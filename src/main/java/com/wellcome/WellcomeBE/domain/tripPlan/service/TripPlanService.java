package com.wellcome.WellcomeBE.domain.tripPlan.service;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanDetailResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.global.OpeningHoursUtils;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;
    private final TripPlanPlaceRepository tripPlanPlaceRepository;
    private final TokenProvider tokenProvider;
    private final GoogleMapInfoService googleMapInfoService;

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
        List<Long> tripPlanIdList = request.getDeletePlanIdList();

        boolean allMatch = tripPlanRepository.findByIdIn(tripPlanIdList).stream()
                .allMatch(tripPlan -> tripPlan.getMember().getId().equals(currentMember.getId()));
        if (!allMatch) {
            throw new CustomException(ACCESS_DENIED);
        }

        // 여행 폴더 내 여행지 삭제
        List<TripPlanPlace> tripPlanPlaceList = tripPlanPlaceRepository.findByTripPlanIdIn(tripPlanIdList);
        tripPlanPlaceRepository.deleteAllInBatch(tripPlanPlaceList);

        // 여행 폴더 일괄 삭제 처리
        tripPlanRepository.deleteAllByIdInBatch(tripPlanIdList);
    }

    public TripPlanDetailResponse getTripPlan(Long planId, Thema thema, int page) {

        // 권한 확인
        Member currentMember = tokenProvider.getMember();

        TripPlan tripPlan = tripPlanRepository.findById(planId)
                .orElseThrow(() -> new CustomException(TRIP_PLAN_NOT_FOUND));
        if(tripPlan.getMember().getId() != currentMember.getId()){
            throw new CustomException(ACCESS_DENIED);
        }

        // 상세 조회
        Set<String> themaSet = new HashSet<>();
        if(thema != null){
            themaSet.add(thema.getName());
        }

        PageRequest pageRequest = PageRequest.of(page, 8);
        Page<TripPlanPlace> result = tripPlanPlaceRepository.findByTripPlanIdAndThema(pageRequest, planId, thema);
        List<TripPlanDetailResponse.SavedWellnessInfoList.SavedWellnessInfoItem> savedWellnessInfoItemList = result.stream()
                .map(tripPlanPlace -> {
                    // 전체 조회할 경우 테마 종류 누적
                    if(thema == null){
                        themaSet.add(tripPlanPlace.getWellnessInfo().getThema().getName());
                    }

                    // 구글맵 API 정보
                    String parentId = tripPlanPlace.getWellnessInfo().getParentId();
                    PlaceReviewResponse.PlaceResult placeDetails =
                            parentId == null ? null : googleMapInfoService.getPlaceDetails(parentId).block().getResult();

                    // DTO 반환
                    return TripPlanDetailResponse.SavedWellnessInfoList.SavedWellnessInfoItem.from(tripPlanPlace.getWellnessInfo(), placeDetails);
                })
                .collect(Collectors.toList());
        TripPlanDetailResponse.SavedWellnessInfoList savedWellnessInfoList = TripPlanDetailResponse.SavedWellnessInfoList.from(
                result.getTotalElements(),
                result.getNumber(),
                result.hasPrevious(),
                result.hasNext(),
                savedWellnessInfoItemList
        );

        return TripPlanDetailResponse.from(new ArrayList<>(themaSet), tripPlan, savedWellnessInfoList);
    }

}
