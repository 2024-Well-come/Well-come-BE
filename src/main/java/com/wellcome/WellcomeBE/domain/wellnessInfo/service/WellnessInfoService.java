package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.exception.TourApiErrorHandler;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_JSON_PARSING_ERROR;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_RESPONSE_ERROR;
import static com.wellcome.WellcomeBE.global.type.Keyword.KEYWORDS;

@Service
@Slf4j
public class WellnessInfoService {

    private final WebClient tourBasicApiWebClient;
    private final WebClient tourSearchApiWebClient;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final TourApiErrorHandler errorHandler = new TourApiErrorHandler();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GANGWONDO_AREACODE = "32";
    private static final int NUM_OF_ROWS_BASIC = 100;
    private static final int NUM_OF_ROWS_SEARCH = 5;

    public WellnessInfoService(
            @Qualifier("tourBasicApiWebClient") WebClient tourBasicApiWebClient,
            @Qualifier("tourSearchApiWebClient") WebClient tourSearchApiWebClient,
            WellnessInfoRepository wellnessInfoRepository) {
        this.tourBasicApiWebClient = tourBasicApiWebClient;
        this.tourSearchApiWebClient = tourSearchApiWebClient;
        this.wellnessInfoRepository = wellnessInfoRepository;
    }

    public void fetchAndSaveTourInfo() {

        // Basic API 호출 및 저장
        Flux.fromIterable(CategoryDetail.CATEGORY_PARAM_LIST)
                .flatMap(this::fetchData)
                .buffer(50)
                .doOnNext(entities -> wellnessInfoRepository.saveAll(entities))
                .collectList()
                .block();

        // Search API 호출 및 저장
        Flux.fromIterable(KEYWORDS)
                .flatMap(this::fetchDataByKeyword)
                .collectList()
                .doOnNext(entities -> wellnessInfoRepository.saveAll(entities))
                .block();
    }

    private Flux<WellnessInfo> fetchData(CategoryDetail categoryDetail) {
        return fetchFromTourBasicApi(categoryDetail, 1)
                .expand(response -> {
                    int currentPage = response.getResponse().getBody().getPageNo();
                    int totalCount = response.getResponse().getBody().getTotalCount();

                    if (currentPage * NUM_OF_ROWS_BASIC < totalCount) {
                        return fetchFromTourBasicApi(categoryDetail, currentPage + 1);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(response -> {
                    TourBasicApiResponse.Response.Body.Items items = response.getResponse().getBody().getItems();
                    if (items == null || items.getItem() == null || items.getItem().isEmpty()) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(items.getItem());
                })
                .map(this::convertToEntity);
    }

    private Mono<TourBasicApiResponse> fetchFromTourBasicApi(CategoryDetail categoryDetail, int pageNo){
        return tourBasicApiWebClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.queryParam("areaCode", GANGWONDO_AREACODE)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", NUM_OF_ROWS_BASIC)
                            .queryParam("cat1", categoryDetail.getCat1());

                    if (categoryDetail.getCat2() != null) {
                        uriBuilder.queryParam("cat2", categoryDetail.getCat2());
                    }
                    if (categoryDetail.getCat3() != null) {
                        uriBuilder.queryParam("cat3", categoryDetail.getCat3());
                    }
                    return uriBuilder.build();
                })
                .exchangeToMono(this::handleResponse);
    }

    private Flux<WellnessInfo> fetchDataByKeyword(String keyword) {
        return fetchDataFromTourSearchApi(keyword, 1)
                .expand(response -> {
                    int currentPage = response.getResponse().getBody().getPageNo();
                    int totalCount = response.getResponse().getBody().getTotalCount();

                    if (currentPage * NUM_OF_ROWS_SEARCH < totalCount) {
                        return fetchDataFromTourSearchApi(keyword, currentPage + 1);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(response -> {
                    TourBasicApiResponse.Response.Body.Items items = response.getResponse().getBody().getItems();
                    if (items == null || items.getItem() == null || items.getItem().isEmpty()) {
                        log.info("keyword '{}'에 대한 검색 결과 없음", keyword);
                        return Flux.empty();
                    }
                    return Flux.fromIterable(response.getResponse().getBody().getItems().getItem());
                })
                .map(this::convertToEntity);
    }

    private Mono<TourBasicApiResponse> fetchDataFromTourSearchApi(String keyword, int pageNo) {
        return tourSearchApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("areaCode", GANGWONDO_AREACODE)
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", NUM_OF_ROWS_SEARCH)
                        .queryParam("keyword", keyword)
                        .build())
                .exchangeToMono(this::handleResponse);
    }

    private WellnessInfo convertToEntity(TourBasicApiResponse.Response.Body.Items.Item item){
        try {
            return item.toEntity();
        } catch (ParseException e) {
            throw new RuntimeException("Failed to convert item to entity: {}", e);
        }
    }

    private Mono<TourBasicApiResponse> handleResponse(ClientResponse clientResponse){
        return clientResponse.bodyToMono(String.class)
                .flatMap(responseBody -> {
                    MediaType contentType = clientResponse.headers().contentType().orElse(MediaType.APPLICATION_JSON);

                    // JSON 응답 처리
                    if (MediaType.APPLICATION_JSON.equals(contentType)) {
                        return handleJsonResponse(responseBody);
                    }
                    // XML 응답 처리 (에러 처리)
                    else {
                        String errorMessage = errorHandler.handleXmlErrorResponse(responseBody);
                        return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, TOUR_API_RESPONSE_ERROR.getMessage() + ": " + errorMessage));
                    }
                });
    }

    // JSON 응답의 resultMsg 값에 따라 데이터 처리 및 에러 처리
    private Mono<TourBasicApiResponse> handleJsonResponse(String responseBody){
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode responseNode = jsonNode.path("response");

            // 에러 처리
            if (responseNode.isEmpty() || responseNode.isMissingNode()) {
                String resultCode = jsonNode.path("resultCode").asText();
                String errorMessage = errorHandler.getErrorMessage(resultCode);
                return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, TOUR_API_RESPONSE_ERROR.getMessage() + ": " + errorMessage));
            }
            // 정상 데이터 처리
            else {
                TourBasicApiResponse tourBasicApiResponse = objectMapper.readValue(responseBody, TourBasicApiResponse.class);
                return Mono.just(tourBasicApiResponse);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 중 오류 발생: {}", e.getMessage());
            return Mono.error(new CustomException(TOUR_API_JSON_PARSING_ERROR));
        }
    }

//    private Mono<TourBasicApiResponse> handleXmlResponse(String responseBody) {
//        String errorMessage = errorHandler.handleXmlErrorResponse(responseBody);
//        return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, TOUR_API_RESPONSE_ERROR.getMessage() + ": " + errorMessage));
//    }

}
