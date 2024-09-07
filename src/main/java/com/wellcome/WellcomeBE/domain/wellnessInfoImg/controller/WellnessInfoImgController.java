package com.wellcome.WellcomeBE.domain.wellnessInfoImg.controller;

import com.wellcome.WellcomeBE.domain.wellnessInfoImg.service.WellnessInfoImgService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WellnessInfoImgController {

   private final WellnessInfoImgService wellnessInfoImgService;

   // 한국관광공사_국문 API 이미지 호출 및 데이터 저장
   @GetMapping("/getTourImgApiData")
   public void fetchAndSaveTourInfo(){
      wellnessInfoImgService.fetchAndSaveTourImg();
   }
}
