package com.wellcome.WellcomeBE.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Configuration
@Slf4j
public class TourInfoApiWebClientConfig {

    @Value("${tour_api.key}")
    private String apiKey;

    private static final String TOUR_API_BASE_URL = "http://apis.data.go.kr/B551011/KorService1/";

    // WebClient 생성
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    /**
     * 한국관광공사_국문 관광정보 API
     */
    // 국문 관광정보 API 공통 URL 생성
    private UriComponentsBuilder buildTourApiCommonUrl(String endpoint) {
        return UriComponentsBuilder.fromHttpUrl(TOUR_API_BASE_URL + endpoint)
                .queryParam("serviceKey", apiKey)
                .queryParam("MobileOS", "AND")
                .queryParam("MobileApp", "Wellcome")
                .queryParam("_type", "json")
                .queryParam("listYN", "Y");
    }

    // 지역기반관광정보조회
    public String getTourBasicApiUrl(Map<String, String> additionalParams) {
        UriComponentsBuilder uriBuilder = buildTourApiCommonUrl("areaBasedList1");
        additionalParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build(true).toUriString();
    }

    // 키워드검색조회
    public String getTourSearchApiUrl(Map<String, String> additionalParams) {
        UriComponentsBuilder uriBuilder = buildTourApiCommonUrl("searchKeyword1");
        additionalParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build(true).toUriString();
    }

    @Bean
    public WebClient tourImageApiWebClient() {
        return WebClient.builder()
                .baseUrl("http://apis.data.go.kr/B551011/KorService1/")
                .defaultHeader("accept", "application/json")
                .build();
    }

    public String getServiceKey() {
        return apiKey;
    }

}
