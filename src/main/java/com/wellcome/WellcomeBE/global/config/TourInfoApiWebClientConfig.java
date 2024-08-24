package com.wellcome.WellcomeBE.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TourInfoApiWebClientConfig {

    @Value("${tour_api.key}")
    private String apiKey;

    // 한국관광공사_국문 관광정보 서비스 API - 지역기반관광정보조회
    @Bean
    public WebClient tourBasicApiWebClient() {
        return WebClient.builder()
                .baseUrl("http://apis.data.go.kr/B551011/KorService1/areaBasedList1?"
                        + "serviceKey=" + apiKey
                        + "&MobileOS=AND"
                        + "&MobileApp=Wellcome"
                        + "&_type=json"
                        + "&listYN=Y")
                .build();
    }

    // 한국관광공사_국문 관광정보 서비스 API - 키워드검색조회
    @Bean
    public WebClient tourSearchApiWebClient() {
        return WebClient.builder()
                .baseUrl("http://apis.data.go.kr/B551011/KorService1/searchKeyword1?"
                        + "serviceKey=" + apiKey
                        + "&MobileOS=AND"
                        + "&MobileApp=Wellcome"
                        + "&_type=json"
                        + "&listYN=Y")
                .build();
    }

}
