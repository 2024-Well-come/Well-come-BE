package com.wellcome.WellcomeBE.domain.wellnessInfoImg.controller;

import com.wellcome.WellcomeBE.domain.wellnessInfoImg.service.WellnessInfoImgService3;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WellnessInfoImgController {

   private final WellnessInfoImgService3 wellnessInfoImgService3;

   @GetMapping("/getTourImgApiData")
   public void fetchAndSaveTourImage() throws Exception {
      wellnessInfoImgService3.fetchAndSaveTourImages();
   }
}
