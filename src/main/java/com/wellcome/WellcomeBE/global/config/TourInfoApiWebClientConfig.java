package com.wellcome.WellcomeBE.global.config;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.netty.http.client.HttpClient;
import java.time.Duration;
import java.util.Map;

@Configuration
@Slf4j
public class TourInfoApiWebClientConfig {

    @Value("${tour_api.key}")
    private String apiKey;

    private static final String TOUR_API_BASE_URL = "http://apis.data.go.kr/B551011/KorService2/";
    private static final String WEATHER_API_BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";

    // WebClient 생성
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000) //연결 타임아웃
                .responseTimeout(Duration.ofMillis(5000)); //응답 타임아웃

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
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
                .queryParam("_type", "json");
                //.queryParam("listYN", "Y");
    }

    // 지역기반관광정보조회
    public String getTourBasicApiUrl(Map<String, String> additionalParams) {
        UriComponentsBuilder uriBuilder = buildTourApiCommonUrl("areaBasedList2");
        additionalParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build(true).toUriString();
    }

    // 키워드검색조회
    public String getTourSearchApiUrl(Map<String, String> additionalParams) {
        UriComponentsBuilder uriBuilder = buildTourApiCommonUrl("searchKeyword2");
        additionalParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build(true).toUriString();
    }

    @Bean
    public WebClient tourImageApiWebClient() {
        return WebClient.builder()
                //.baseUrl("http://apis.data.go.kr/B551011/KorService1/")
                .defaultHeader("accept", "application/json")
                .build();
    }

    // tour4.0 Image API 요청
    private UriComponentsBuilder  buildTourImageApiCommonUrl (String endpoint) {
        return UriComponentsBuilder.fromHttpUrl(TOUR_API_BASE_URL + endpoint)
                .queryParam("MobileOS", "AND")
                .queryParam("MobileApp", "Wellcome")
                .queryParam("_type", "json")
                .queryParam("imageYN", "Y")
                .queryParam("numOfRows",6)
                .queryParam("pageNo",1)
                .queryParam("serviceKey",apiKey);

    }

    // wellness 별 이미지 요청
    public String getTourImageApiUrl(Map<String, String> additionalParams) {
        UriComponentsBuilder uriBuilder = buildTourImageApiCommonUrl("detailImage2");
        additionalParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build(false).toUriString();
    }



    public String getServiceKey() {
        return apiKey;
    }

    /**
     * 기상청_단기예보 API (초단기예보 조회)
     */
    public String getWeatherApiUrl(Map<String, String> additionalParams){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(WEATHER_API_BASE_URL)
                .queryParam("serviceKey", apiKey)
                .queryParam("dataType", "JSON");
        additionalParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build(true).toUriString();
    }

}
