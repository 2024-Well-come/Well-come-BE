package com.wellcome.WellcomeBE.domain.wellnessInfo.controller;

import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request.WellnessInfoListRequest;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoBasicResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoGoogleReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.WellnessInfoApiService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.WellnessInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WellnessInfoController {

    private final WellnessInfoService wellnessInfoService;
    private final WellnessInfoApiService wellnessInfoApiService;

    // 한국관광공사_국문 API 호출 및 데이터 저장
    @GetMapping("/getTourBasicApiData")
    public void fetchAndSaveTourInfo(){
        wellnessInfoService.fetchAndSaveTourInfo();
    }

    // 웰니스 장소 추천 목록
    @GetMapping("/api/wellness-info")
    public ResponseEntity<WellnessInfoResponse> getWellnessInfoList(
            @RequestParam(value = "page") int page,
            @RequestBody WellnessInfoListRequest request
    ){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoList(page, request));
    }

    // 웰니스 장소 상세 조회(1) - 기본 정보 조회
    @GetMapping("/api/wellness-info/{wellnessInfoId}/basic")
    public ResponseEntity<WellnessInfoBasicResponse> wellnessInfoBasic(@PathVariable Long wellnessInfoId){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoBasic(wellnessInfoId));
    }

    // 웰니스 장소 상세 조회(3) - 구글 리뷰 조회
    @GetMapping("/api/wellness-info/{wellnessInfoId}/google-reviews")
    public ResponseEntity<WellnessInfoGoogleReviewResponse> getWellnessInfoGoogleReviews(
            @PathVariable("wellnessInfoId") Long wellnessInfoId
    ){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoGoogleReviews(wellnessInfoId));
    }

}
