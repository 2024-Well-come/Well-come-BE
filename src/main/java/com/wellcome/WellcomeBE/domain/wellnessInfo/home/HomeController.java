package com.wellcome.WellcomeBE.domain.wellnessInfo.home;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/api/home")
    public ResponseEntity<HomeResponse> getRandomWellnessInfoList(){
        return ResponseEntity.ok(homeService.getRandomWellnessInfoList());
    }

}
