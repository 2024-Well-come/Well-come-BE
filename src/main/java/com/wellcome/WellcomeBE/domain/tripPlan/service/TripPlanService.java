package com.wellcome.WellcomeBE.domain.tripPlan.service;

import com.wellcome.WellcomeBE.domain.member.Member;
import com.wellcome.WellcomeBE.domain.review.GoogleMapInfoService;
import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanDetailResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanReviewResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.repository.TripPlanRepository;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.repository.TripPlanPlaceRepository;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.security.TokenProvider;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.ACCESS_DENIED;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TRIP_PLAN_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;
    private final TripPlanPlaceRepository tripPlanPlaceRepository;
    private final TokenProvider tokenProvider;
    private final GoogleMapInfoService googleMapInfoService;

    public void createTripPlan(TripPlanRequest request){

        TripPlan tripPlan = TripPlan.builder()
                .title(request.getName())
                .startDate(request.getTripStartDate())
                .endDate(request.getTripEndDate())
                .member(tokenProvider.getMember())
                .build();
        tripPlanRepository.save(tripPlan);
    }

    public TripPlanResponse.TripPlanBriefResponse getTripPlanList(){
        List<TripPlan> result = tripPlanRepository.findByMember(tokenProvider.getMember());
        List<TripPlanResponse.TripPlanPlaceItem> planPlaceItems = result.stream().map(TripPlanResponse.TripPlanPlaceItem::from).collect(Collectors.toList());
        return TripPlanResponse.TripPlanBriefResponse.builder().tripPlanList(planPlaceItems).build();
    }

    public TripPlanResponse.TripPlanListResponse getTripPlans(String sort, int page){
        PageRequest pageRequest = PageRequest.of(page,10);
        Page<TripPlan> tripPlans;
        if ("upcoming".equalsIgnoreCase(sort)) {
            tripPlans = tripPlanRepository.findUpcomingPlansByMember(tokenProvider.getMember(),pageRequest);
        } else {
            tripPlans = tripPlanRepository.findCreateLatestPlansByMember(tokenProvider.getMember(),pageRequest);
        }
        // TripPlanItem으로 변환
        List<TripPlanResponse.TripPlanItem> tripPlanItems = tripPlans.getContent().stream()
                .map(tripPlan -> TripPlanResponse.TripPlanItem.from(
                        tripPlan,
                        tripPlan.getTripPlanPlaces().isEmpty() ? null : tripPlan.getTripPlanPlaces().get(0).getWellnessInfo().getThumbnailUrl(),
                        tripPlan.getTripPlanPlaces().size()))
                .collect(Collectors.toList());

        // TripPlanListItem 생성
        TripPlanResponse.TripPlanListItem tripPlanListItem = TripPlanResponse.TripPlanListItem.from(
                tripPlans.getTotalElements(),
                tripPlans.getNumber(),
                tripPlans.hasPrevious(),
                tripPlans.hasNext(),
                tripPlanItems);

        return TripPlanResponse.TripPlanListResponse.builder()
                .upcomingTripList(getUpcomingTrips())
                .tripPlanList(tripPlanListItem)
                .build();

    }
    private List<TripPlanResponse.TripPlanItem> getUpcomingTrips() {
        List<TripPlan> upcomingTrips = tripPlanRepository.findAllByTripStartDateAfterByMember(tokenProvider.getMember());
        return upcomingTrips.stream().map(tripPlan -> TripPlanResponse.TripPlanItem.from(
                        tripPlan,
                        tripPlan.getTripPlanPlaces().isEmpty() ? null : tripPlan.getTripPlanPlaces().get(0).getWellnessInfo().getThumbnailUrl(),
                        tripPlan.getTripPlanPlaces().size()))
                .collect(Collectors.toList());
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
        Set<Thema> themaSet = new HashSet<>();
        if(thema != null){
            themaSet.add(thema);
        }

        PageRequest pageRequest = PageRequest.of(page, 8);
        Page<TripPlanPlace> result = tripPlanPlaceRepository.findByTripPlanIdAndThema(pageRequest, planId, thema);
        List<TripPlanDetailResponse.SavedWellnessInfoList.SavedWellnessInfoItem> savedWellnessInfoItemList = result.stream()
                .map(tripPlanPlace -> {
                    // 전체 조회할 경우 테마 종류 누적
                    if(thema == null){
                        themaSet.add(tripPlanPlace.getWellnessInfo().getThema());
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


    @Transactional
    public void updateTripPlan(Long planId, TripPlanRequest request) {
        TripPlan existingPlan = tripPlanRepository.findById(planId).orElseThrow(() -> new CustomException(CustomErrorCode.TRIP_PLAN_NOT_FOUND));

        if (request.getName() != null) {
            existingPlan.updateTitle(request.getName());
        }
        if (request.getTripStartDate() != null) {
            existingPlan.updateStartDate(request.getTripStartDate());
        }
        if (request.getTripEndDate() != null) {
            existingPlan.updateEndDate(request.getTripEndDate());
        }

    }

    public TripPlanReviewResponse getTripPlanWriteReview(int page) {
        // 회원 정보
        Member member = tokenProvider.getMember();

        // 조회 당시의 날짜
        LocalDate currentDate = LocalDate.now();
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize);

        // 3. 회원의 과거 여행 계획을 날짜 기준으로 최신순 정렬
        Page<TripPlan> tripPlanPage = tripPlanRepository.findPastTripPlans(member, currentDate, pageable);

        List<TripPlanReviewResponse.TripPlanReviewItem> reviewItems = tripPlanPage.getContent().stream()
                .map(tripPlan -> TripPlanReviewResponse.TripPlanReviewItem.from(tripPlan, tripPlan.getTripPlanPlaces().size()))
                .collect(Collectors.toList());

        return TripPlanReviewResponse.from(tripPlanPage.getTotalElements(),tripPlanPage.getTotalPages(),tripPlanPage.hasPrevious(),tripPlanPage.hasNext(),reviewItems);
    }
}
