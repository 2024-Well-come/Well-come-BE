package com.wellcome.WellcomeBE.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@Slf4j
public class TourInfoApiWebClientConfig {

    @Value("${api.key}")
    private String apiKey;

    private static final String BASE_URL = "http://apis.data.go.kr/B551011/KorService1/";

    // 공통 baseUrl 생성
    private String buildUrl(String endpoint) {
        String uriString = UriComponentsBuilder.fromHttpUrl(BASE_URL + endpoint)
                .queryParam("serviceKey", apiKey)
                .queryParam("MobileOS", "AND")
                .queryParam("MobileApp", "Wellcome")
                .queryParam("_type", "json")
                .queryParam("listYN", "Y")
                .build(false)
                .toUriString();

        return uriString;
    }

    // 한국관광공사_국문 관광정보 서비스 API - 지역기반관광정보조회
    @Bean
    public WebClient tourBasicApiWebClient() {
        return WebClient.builder()
                .baseUrl(buildUrl("areaBasedList1"))
                .build();
    }

    // 한국관광공사_국문 관광정보 서비스 API - 키워드검색조회
    @Bean
    public WebClient tourSearchApiWebClient() {
        return WebClient.builder()
                .baseUrl(buildUrl("searchKeyword1"))
                .build();
    }

}
