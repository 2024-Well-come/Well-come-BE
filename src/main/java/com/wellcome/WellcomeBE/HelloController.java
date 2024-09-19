package com.wellcome.WellcomeBE;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // jar 빌드 테스트용 
    @GetMapping("/health-check")
    public String hello(){
        return "health-check";
    }

}