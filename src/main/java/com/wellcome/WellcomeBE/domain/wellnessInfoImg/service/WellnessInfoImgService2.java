package com.wellcome.WellcomeBE.domain.wellnessInfoImg.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.WellnessInfoImg;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.request.WellnessContentIdOnly;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.response.TourImageApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.config.TourInfoApiWebClientConfig;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.exception.TourApiErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_JSON_PARSING_ERROR;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_RESPONSE_ERROR;

@Service
@Slf4j
public class WellnessInfoImgService2 {

    private final WebClient webClient;
    private final TourInfoApiWebClientConfig webClientConfig;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final WellnessInfoImgRepository wellnessInfoImgRepository;
    private final TourApiErrorHandler errorHandler = new TourApiErrorHandler();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public WellnessInfoImgService2(
            WebClient webClient,
            WellnessInfoRepository wellnessInfoRepository,
            TourInfoApiWebClientConfig webClientConfig,
            WellnessInfoImgRepository wellnessInfoImgRepository
    ) {
        this.webClient = webClient;
        this.wellnessInfoRepository = wellnessInfoRepository;
        this.webClientConfig = webClientConfig;
        this.wellnessInfoImgRepository = wellnessInfoImgRepository;
    }

    public void fetchAndSaveTourImage() {
        List<WellnessContentIdOnly> wellnessList = wellnessInfoRepository.findAllContentIdOnly();
        if (wellnessList == null || wellnessList.isEmpty()) {
            log.warn("wellnessList가 null이거나 비어있습니다. API 호출을 중단합니다.");
            return; // 또는 예외 던지기
        }


        Flux.fromIterable(wellnessList)
                .flatMap(this::fetchData) // contentId마다 병렬 호출
                //.buffer(50) // 50개씩 저장
                //.parallel(3)
                .doOnNext(wellnessInfoImgRepository::save)
                .collectList()
                .block(); // 동기 대기
    }

    private Flux<WellnessInfoImg> fetchData(WellnessContentIdOnly info) {
        String contentId = info.getContentId();
        log.info("현재 찾아온 content:{}",contentId);

        return fetchDataFromTourImgApi(contentId)
                .map(response -> Tuples.of(contentId, response)) // contentId와 응답 묶기
                .flatMapMany(tuple -> {
                    String cid = tuple.getT1();
                    TourImageApiResponse response = tuple.getT2();

                    TourImageApiResponse.Response.Body.Items items = response.getResponse().getBody().getItems();
                    if (items == null || items.getItem() == null || items.getItem().isEmpty() ) {
                        log.info("📭 No images found for contentId: {}", contentId);
                        return Flux.empty();
                    }

                    try {
                               // 문제 추적용 로그
                        //log.info("🔍 Try to findByContentId: {}", contentId);
                        Optional<WellnessInfo> optionalWellness = wellnessInfoRepository.findByContentId(cid);


                        if (optionalWellness.isEmpty()) {
                            log.warn("WellnessInfo not found for contentId: {}", cid);
                            return Flux.empty();
                        }
                        WellnessInfo wellnessInfo = optionalWellness.get();

                        return Flux.fromIterable(items.getItem())
                                .map(item -> convertToEntity(item, wellnessInfo));

                    } catch (IncorrectResultSizeDataAccessException e) {
                        // 예외 발생 contentId 로깅
                        log.error("❌ contentId {} caused multiple result error", contentId, e);
                        throw e; // 혹은 fallback 로직 실행 가능
                    }

                });
    }

    private Mono<TourImageApiResponse> fetchDataFromTourImgApi(String contentId) {
        Map<String, String> params = new HashMap<>();
        log.info("현재 요청 중:{}",contentId);
        params.put("contentId", contentId);

        String uriString = webClientConfig.getTourImageApiUrl(params);
        try {
            URI uri = new URI(uriString);
            log.info("요청 URI: {}", uri.toString());
            return webClient.get()
                    .uri(uri)
                    .exchangeToMono(this::handleResponse);
        } catch (URISyntaxException e) {
            log.error("Invalid URI syntax: {}", e.getMessage());
            return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, "Invalid URI syntax"));
        }
    }

    private WellnessInfoImg convertToEntity(TourImageApiResponse.Response.Body.Items.Item item, WellnessInfo wellnessInfo) {
        try {
            return item.toEntity(wellnessInfo);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to convert item to entity: ", e);
        }
    }

    private Mono<TourImageApiResponse> handleResponse(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(responseBody -> {
                    MediaType contentType = clientResponse.headers().contentType().orElse(MediaType.APPLICATION_JSON);

                    if (MediaType.APPLICATION_JSON.equals(contentType)) {
                        return handleJsonResponse(responseBody);
                    } else {
                        String errorMessage = errorHandler.handleXmlErrorResponse(responseBody);
                        return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, TOUR_API_RESPONSE_ERROR.getMessage() + ": " + errorMessage));
                    }
                });
    }

    private Mono<TourImageApiResponse> handleJsonResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode responseNode = jsonNode.path("response");

            if (responseNode.isEmpty() || responseNode.isMissingNode()) {
                String resultCode = jsonNode.path("resultCode").asText();
                String errorMessage = errorHandler.getErrorMessage(resultCode);
                return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, TOUR_API_RESPONSE_ERROR.getMessage() + ": " + errorMessage));
            } else {
                TourImageApiResponse tourImageApiResponse = objectMapper.readValue(responseBody, TourImageApiResponse.class);
                return Mono.just(tourImageApiResponse);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 중 오류 발생: {}", e.getMessage());
            return Mono.error(new CustomException(TOUR_API_JSON_PARSING_ERROR));
        }
    }
}
