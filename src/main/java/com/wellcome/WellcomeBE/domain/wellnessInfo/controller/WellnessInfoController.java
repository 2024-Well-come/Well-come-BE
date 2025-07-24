package com.wellcome.WellcomeBE.domain.wellnessInfo.controller;

import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request.WellnessInfoListRequest;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.*;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.WellnessInfoApiService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.setup.WellnessInfoService2;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.setup.WellnessInfoService;
import com.wellcome.WellcomeBE.domain.wellnessInfo.service.update.WellnessInfoTempService;
import com.wellcome.WellcomeBE.global.type.ImgSavedType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class WellnessInfoController {

    private final WellnessInfoService wellnessInfoService;
    private final WellnessInfoApiService wellnessInfoApiService;
    private final WellnessInfoService2 wellnessInfoService2;
    private final WellnessInfoTempService wellnessInfoTempService;

    // 한국관광공사_국문 API 호출 및 데이터 저장 (기존: WebFlux)
    @GetMapping("/getTourBasicApiData-webflux")
    public void fetchAndSaveTourInfoByWebFlux(){
        long start = System.currentTimeMillis();

        wellnessInfoService.fetchAndSaveTourInfo();
        wellnessInfoService.uploadThumbnailImgToS3();

        long end = System.currentTimeMillis();
        log.info("** API 호출 및 데이터 저장 완료 ** \n" +
                        "  - 총 처리 시간: {}ms", end - start);
    }

    // 한국관광공사_국문 API 호출 및 데이터 저장 (변경: @Async)
    @GetMapping("/getTourBasicApiData")
    public void fetchAndSaveTourInfo(){
        wellnessInfoService2.fetchAllData();
        wellnessInfoService2.uploadThumbnailImgToS3();
    }

    // 웰니스 정보 업데이트 (추가 및 삭제된 데이터 반영)
    @GetMapping("/getTourBasicApiData-update")
    public void updateTourInfo(){
        wellnessInfoTempService.updateTourInfo();
    }

    // 웰니스 장소 추천 목록 (썸네일 이미지 URL은 S3에 업로드된 URL을 가져온다.)
    @PostMapping("/api/wellness-info")
    public ResponseEntity<WellnessInfoResponse> getWellnessInfoList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestBody WellnessInfoListRequest request
    ){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoList(page, request, ImgSavedType.S3));
    }

    // 웰니스 장소 상세 조회(1) - 기본 정보 조회
    @GetMapping("/api/wellness-info/{wellnessInfoId}/basic")
    public ResponseEntity<WellnessInfoBasicResponse> wellnessInfoBasic(@PathVariable Long wellnessInfoId){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoBasic(wellnessInfoId));
    }

    // 웰니스 장소 상세 조회(2) - 유용한 정보 제공
    @GetMapping("/api/wellness-info/{wellnessInfoId}/usefulInformation")
    public ResponseEntity<WellnessInfoUsefulInformationResponse>getUsefulInformation(@PathVariable Long wellnessInfoId){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoUsefulInformation(wellnessInfoId));
    }

    // 웰니스 장소 상세 조회(3) - 구글 리뷰 조회
    @GetMapping("/api/wellness-info/{wellnessInfoId}/google-reviews")
    public ResponseEntity<WellnessInfoGoogleReviewResponse> getWellnessInfoGoogleReviews(
            @PathVariable("wellnessInfoId") Long wellnessInfoId
    ){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoGoogleReviews(wellnessInfoId));
    }

    // 웰니스 장소 상세 조회(4) - 후기 게시글 조회
    @GetMapping("/api/wellness-info/{wellnessInfoId}/reviews")
    public ResponseEntity<WellnessInfoReviewPostResponse> getWellnessInfoReviewPosts(
            @PathVariable("wellnessInfoId") Long wellnessInfoId
    ){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoReviewPosts(wellnessInfoId));
    }

    
    // 현재 날씨 정보 조회 (강원도 날씨 테스트용)
    @GetMapping("/api/weather-info")
    public ResponseEntity<WeatherResponse> getWeatherInfo() {
        return ResponseEntity.ok(wellnessInfoService.fetchWeatherInfo(73, 134)); //강원도
    }

    //v1
    // 웰니스 장소 상세 조회(2) - 주변 추전 장소
    @GetMapping("/api/wellness-info/{wellnessInfoId}/nearby-places")
    public ResponseEntity<WellnessInfoNearResponse>getSurroundingPlaces(@PathVariable Long wellnessInfoId){
        return ResponseEntity.ok(wellnessInfoApiService.getSurroundingWellnessInfo(wellnessInfoId));
    }
    // 웰니스 장소 상세 조회(5) - 아티클 조회
    @GetMapping("/api/wellness-info/{wellnessInfoId}/articles")
    public ResponseEntity<WellnessInfoArticleResponse> getWellnessIngoArticles(
            @PathVariable("wellnessInfoId") Long wellnessInfoId
    ){
        return ResponseEntity.ok(wellnessInfoApiService.getWellnessInfoArticle(wellnessInfoId));
    }

}
