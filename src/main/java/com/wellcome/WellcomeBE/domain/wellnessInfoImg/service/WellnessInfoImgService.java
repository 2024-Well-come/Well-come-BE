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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final int CONCURRENT_REQUESTS = 500;


    // DB WellnessInfo에 해당하는 데이터를 조회
    @Transactional
    public void fetchAndSaveTourImg (int page) {

        Pageable pageable = PageRequest.of(page, CONCURRENT_REQUESTS);
        List<WellnessInfo> wellnessInfoList = wellnessInfoRepository.findAll(pageable).toList();

        Flux.fromIterable(wellnessInfoList)
                .flatMap(wellnessInfo -> fetchImage(wellnessInfo.getContentId())
                        .flatMap(response -> {
                            if (response != null && response.getResponse() != null
                                    && response.getResponse().getBody() != null
                                    && response.getResponse().getBody().getItems() != null
                                    && response.getResponse().getBody().getItems().getItem() != null) {

                                List<TourImageApiResponse.Response.Body.Items.Item> itemList = response.getResponse().getBody().getItems().getItem();

                                if (itemList.isEmpty()) {
                                    log.info("{}에 상세 이미지에 대한 검색 결과 없음", wellnessInfo.getContentId());
                                    return Mono.empty(); // 이미지가 없는 경우
                                }

                                // WellnessInfoImg 엔티티 리스트 생성
                                List<WellnessInfoImg> images = itemList.stream()
                                        .map(imgItem -> WellnessInfoImg.builder()
                                                .wellnessInfo(wellnessInfo)
                                                .imgUrl(imgItem.getOriginimgurl())
                                                .build())
                                        .collect(Collectors.toList());

                                // DB에 이미지 엔티티 리스트 저장
                                wellnessInfoImgRepository.saveAll(images); // 즉시 저장
                                log.info("Successfully saved {} images for wellnessInfo {}", images.size(), wellnessInfo.getContentId());

                                return Mono.just(images); // 저장한 이미지 리스트 반환
                            } else {
                                log.info("{}에 대한 이미지 정보를 가져오는 데 실패했습니다.", wellnessInfo.getContentId());
                                return Mono.empty();
                            }
                        }))
                .collectList() // 모든 결과를 리스트로 수집
                .block(); // 블록하여 작업이 완료될 때까지 대기
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
