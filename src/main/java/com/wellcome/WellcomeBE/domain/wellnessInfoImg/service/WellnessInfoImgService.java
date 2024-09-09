package com.wellcome.WellcomeBE.domain.wellnessInfoImg.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.WellnessInfoImg;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.response.TourImageApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.global.config.TourInfoApiWebClientConfig;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.exception.TourApiErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_RESPONSE_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class WellnessInfoImgService {

    private final WebClient tourImageApiWebClient;
    private final TourInfoApiWebClientConfig config;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final WellnessInfoImgRepository wellnessInfoImgRepository;

    private final TourApiErrorHandler errorHandler = new TourApiErrorHandler();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int BATCH_SIZE = 50;
    private static final int CONCURRENT_REQUESTS = 10;
    private static final Duration REQUEST_DELAY = Duration.ofMillis(200);


    // DB WellnessInfo에 해당하는 데이터를 조회
    public void fetchAndSaveTourImg() {
        List<WellnessInfo> wellnessInfoList = wellnessInfoRepository.findTop10ByOrderByIdAsc();
        Flux.fromIterable(wellnessInfoList) // DB 조회 결과에 대해 Iterable 사용
                .flatMap(this::fetchAndSaveImage, CONCURRENT_REQUESTS) // 이미지 API 호출 및 저장
                .buffer(BATCH_SIZE)
                .delayElements(REQUEST_DELAY)
                .collectList()
                .block();
    }

    // 이미지 API 호출 및 DB에 저장하는 메서드
    private Mono<Void> fetchAndSaveImage(WellnessInfo wellnessInfo) {
        return fetchImage(wellnessInfo.getContentId()) // 이미지 API 호출
                .flatMapMany(response -> {
                    TourImageApiResponse.Response response1 = response.getResponse();
                    if (response1 == null || response1.getBody().getItems() == null || response1.getBody().getItems().getItem() == null || response1.getBody().getItems().getItem().isEmpty()) {
                        log.info("{}에 상세 이미지에 대한 검색 결과 없음", wellnessInfo.getContentId());
                        return Flux.empty();
                    }
                    TourImageApiResponse.Response.Body.Items items = response.getResponse().getBody().getItems();
                    return Flux.fromIterable(items.getItem()); // API 응답에서 이미지 리스트 추출
                })
                .map(imgItem -> WellnessInfoImg.builder() // WellnessInfoImg 엔티티 생성
                        .wellnessInfo(wellnessInfo)
                        .imgUrl(imgItem.getOriginimgurl()) // API에서 반환된 이미지 URL 사용
                        .build())
                .collectList()
                .flatMap(images -> Mono.fromRunnable(() -> wellnessInfoImgRepository.saveAll(images))) // 생성된 엔티티 리스트 저장
                .then(); // Mono<Void> 반환
    }


    // tour4.0 Image API 요청
    private Mono<TourImageApiResponse> fetchImage(String wellnessInfoId) {
        URI uriString = UriComponentsBuilder.fromUriString("http://apis.data.go.kr/B551011/KorService1/detailImage1")
                .queryParam("MobileOS", "AND")
                .queryParam("MobileApp", "Wellcome")
                .queryParam("_type", "json")
                .queryParam("contentId", wellnessInfoId)
                .queryParam("imageYN", "Y")
                .queryParam("subImageYN", "Y")
                .queryParam("serviceKey", config.getServiceKey())
                .build(true)
                .toUri();

        log.info("Requesting URL: {}", uriString.toString());

        return tourImageApiWebClient.get()
                .uri(uriString)
                .exchangeToMono(this::handleResponse);
    }

    // API 응답 처리
    private Mono<TourImageApiResponse> handleResponse(ClientResponse clientResponse) {
        MediaType contentType = clientResponse.headers().contentType().orElse(MediaType.APPLICATION_JSON);
        return clientResponse.bodyToMono(String.class)
                .flatMap(responseBody -> {
                    if (MediaType.APPLICATION_JSON.equals(contentType)) {
                        // JSON 응답을 TourImageApiResponse 객체로 변환
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            TourImageApiResponse response = objectMapper.readValue(responseBody, TourImageApiResponse.class);
                            return Mono.just(response);
                        } catch (JsonProcessingException e) {
                            return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, "JSON 변환 오류: " + e.getMessage()));
                        }
                    } else {
                        // XML 응답 처리
                        String errorMessage = errorHandler.handleXmlErrorResponse(responseBody);
                        return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, TOUR_API_RESPONSE_ERROR.getMessage() + ": " + errorMessage));
                    }
                });
    }



}
