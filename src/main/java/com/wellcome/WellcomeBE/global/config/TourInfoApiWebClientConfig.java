package com.wellcome.WellcomeBE.global.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class TourInfoApiWebClientConfig {

    @Value("${tour_api.keys.one}")
    private String apiKeyOne;

    @Value("${tour_api.keys.two}")
    private String apiKeyTwo;

    private List<String> apiKeys;
    private static final String TOUR_API_BASE_URL = "http://apis.data.go.kr/B551011/KorService1/";


    @PostConstruct
    public void init() {
        // Initialize the API keys list with the injected values
        apiKeys = Arrays.asList(apiKeyOne, apiKeyTwo);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    private UriComponentsBuilder buildTourApiCommonUrl(String endpoint) {
        return UriComponentsBuilder.fromHttpUrl(TOUR_API_BASE_URL + endpoint)
                .queryParam("serviceKey", apiKeyOne)
                .queryParam("MobileOS", "AND")
                .queryParam("MobileApp", "Wellcome")
                .queryParam("_type", "json")
                .queryParam("listYN", "Y");
    }

    public String getTourBasicApiUrl(Map<String, String> additionalParams) {
        UriComponentsBuilder uriBuilder = buildTourApiCommonUrl("areaBasedList1");
        additionalParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build(true).toUriString();
    }

    public String getTourSearchApiUrl(Map<String, String> additionalParams) {
        UriComponentsBuilder uriBuilder = buildTourApiCommonUrl("searchKeyword1");
        additionalParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build(true).toUriString();
    }

    @Bean
    public WebClient tourImageApiWebClient() {
        return WebClient.builder()
                .defaultHeader("accept", "application/json")
                .build();
    }


    public String getServiceKey() {
        return this.apiKeyOne;
    }
}
