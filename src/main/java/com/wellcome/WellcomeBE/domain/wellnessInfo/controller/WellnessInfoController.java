package com.wellcome.WellcomeBE.domain.wellnessInfo.controller;

import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request.WellnessInfoListRequest;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.*;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.WellnessInfoApiService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.WellnessInfoService;
import com.wellcome.WellcomeBE.global.type.ImgSavedType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // TODO 이미지 저장 방식 선택 후 endpoint 수정 필요
    // 웰니스 장소 추천 목록 (썸네일 이미지 URL DB에 바로 저장)
    @PostMapping("/api/wellness-info")
    public ResponseEntity<WellnessInfoResponse> getWellnessInfoListWithOriginalThumbnail(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestBody WellnessInfoListRequest request
    ){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoList(page, request, ImgSavedType.ORIGINAL));
    }

//    // 웰니스 장소 추천 목록 (썸네일 이미지 URL DB에 바로 저장)
//    @PostMapping("/api/wellness-info")
//    public ResponseEntity<WellnessInfoResponse> getWellnessInfoListWithS3Thumbnail(
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestBody WellnessInfoListRequest request
//    ){
//        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoList(page, request, ImgSavedType.S3));
//    }

    // 웰니스 장소 상세 조회(1) - 기본 정보 조회
    @GetMapping("/api/wellness-info/{wellnessInfoId}/basic")
    public ResponseEntity<WellnessInfoBasicResponse> wellnessInfoBasic(@PathVariable Long wellnessInfoId){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoBasic(wellnessInfoId));
    }

    // 웰니스 장소 상세 조회(2) - 주변 추전 장소
    @GetMapping("/api/wellness-info/{wellnessInfoId}/nearby-places")
    public ResponseEntity<WellnessInfoNearbyList>getSurroundingPlaces(@PathVariable Long wellnessInfoId){
        return ResponseEntity.ok(wellnessInfoApiService.getSurroundingWellnessInfo(wellnessInfoId));
    }

    // 웰니스 장소 상세 조회(3) - 구글 리뷰 조회
    @GetMapping("/api/wellness-info/{wellnessInfoId}/google-reviews")
    public ResponseEntity<WellnessInfoGoogleReviewResponse> getWellnessInfoGoogleReviews(
            @PathVariable("wellnessInfoId") Long wellnessInfoId
    ){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoGoogleReviews(wellnessInfoId));
    }

}
