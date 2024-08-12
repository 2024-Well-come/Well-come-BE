package com.wellcome.WellcomeBE.domain.wellnessInfo.controller;

import com.wellcome.WellcomeBE.domain.wellnessInfo.service.WellnessInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WellnessInfoController {

    private final WellnessInfoService wellnessInfoService;

    // 한국관광공사_국문 API 호출 및 데이터 저장
    @GetMapping("/getTourBasicApiData")
    public void fetchAndSaveTourInfo(){
        wellnessInfoService.fetchAndSaveTourInfo();
    }

}
