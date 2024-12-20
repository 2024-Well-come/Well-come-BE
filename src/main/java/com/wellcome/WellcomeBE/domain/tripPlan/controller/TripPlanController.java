package com.wellcome.WellcomeBE.domain.tripPlan.controller;

import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanDetailResponse;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request.TripPlanPlaceDeleteRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.request.TripPlanCreateRequest;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.dto.response.TripPlanReviewResponse;
import com.wellcome.WellcomeBE.domain.tripPlan.service.TripPlanService;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request.TripPlanPlaceCreateRequest;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.service.TripPlanPlaceService;
import com.wellcome.WellcomeBE.global.type.Thema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-trips")
@RequiredArgsConstructor
public class TripPlanController {

    private final TripPlanService tripPlanService;
    private final TripPlanPlaceService tripPlanPlaceService;

    // 여행 폴더 미리보기
    @GetMapping("/plans/brief")
    public ResponseEntity<TripPlanResponse.TripPlanBriefResponse> tripPlanList(){
        return ResponseEntity.ok(tripPlanService.getTripPlanList());
    }

    // 여행 폴더 생성
    @PostMapping("/plans")
    public ResponseEntity<?> createTripPlan(@RequestBody @Valid TripPlanCreateRequest request){
        tripPlanService.createTripPlan(request);
        return ResponseEntity.ok().build();
    }

    // 여행 폴더 삭제
    @DeleteMapping("/plans")
    public ResponseEntity<Void> deleteTripPlan(@Valid @RequestBody TripPlanDeleteRequest request){
        tripPlanService.deleteTripPlan(request);
        return ResponseEntity.ok().build();
    }
    // 여행 폴더 목록 조회
    @GetMapping("/plans")
    public ResponseEntity<TripPlanResponse.TripPlanListResponse> getTripPlans(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page
    ){
        return ResponseEntity.ok(tripPlanService.getTripPlans(sort, page));
    }

    // 여행 폴더 내 여행지 추가
    @PostMapping("/plans/{planId}/places")
    public ResponseEntity<?> createTripPlanPlace(@PathVariable("planId") Long planId, @RequestBody @Valid TripPlanPlaceCreateRequest request){
        tripPlanPlaceService.createTripPlanPlace(planId,request);
        return ResponseEntity.ok().build();
    }

    // 여행 폴더 내 여행지 삭제
    @DeleteMapping("/plans/{planId}/places")
    public ResponseEntity<Void> deleteTripPlanPlace(
            @PathVariable("planId") Long planId,
            @Valid @RequestBody TripPlanPlaceDeleteRequest request
    ){
        tripPlanPlaceService.deleteTripPlanPlace(planId, request);
        return ResponseEntity.ok().build();
    }

    // 여행 폴더 상세 조회
    @GetMapping("/plans/{planId}")
    public ResponseEntity<TripPlanDetailResponse> getTripPlan(
            @PathVariable("planId") Long planId,
            @RequestParam(value = "thema", required = false) Thema thema,
            @RequestParam(value = "page", defaultValue = "0") int page
    ){
        return ResponseEntity.ok(tripPlanService.getTripPlan(planId, thema, page));
    }

    @PatchMapping("plans/{planId}")
    public ResponseEntity<?> modifyTripPlan(@PathVariable Long planId, @RequestBody TripPlanCreateRequest request){
        tripPlanService.updateTripPlan(planId,request);
        return ResponseEntity.ok().build();
    }

    // 리뷰 작성할 여행 폴더 목록
    @GetMapping("/review")
    public ResponseEntity<TripPlanReviewResponse> writeReviewTripPlan(
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        TripPlanReviewResponse tripPlanWriteReview = tripPlanService.getTripPlanWriteReview(page);
        return ResponseEntity.ok(tripPlanWriteReview);
    }
}
