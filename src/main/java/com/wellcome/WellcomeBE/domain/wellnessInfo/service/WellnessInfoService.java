package com.wellcome.WellcomeBE.domain.wellnessInfo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.config.TourInfoApiWebClientConfig;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.exception.TourApiErrorHandler;
import com.wellcome.WellcomeBE.global.image.S3Service;
import com.wellcome.WellcomeBE.global.type.CategoryDetail;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_JSON_PARSING_ERROR;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_RESPONSE_ERROR;
import static com.wellcome.WellcomeBE.global.type.Keyword.KEYWORDS;

@Service
@Slf4j
public class WellnessInfoService {

    private final WebClient webClient;
    private final TourInfoApiWebClientConfig webClientConfig;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final TourApiErrorHandler errorHandler = new TourApiErrorHandler();
    private final S3Service s3Service;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GANGWONDO_AREACODE = "32";
    private static final int NUM_OF_ROWS_BASIC = 100;
    private static final int NUM_OF_ROWS_SEARCH = 5;

    public WellnessInfoService(
            WebClient webClient,
            WellnessInfoRepository wellnessInfoRepository,
            TourInfoApiWebClientConfig webClientConfig,
            S3Service s3Service
    ) {
        this.webClient = webClient;
        this.wellnessInfoRepository = wellnessInfoRepository;
        this.webClientConfig = webClientConfig;
        this.s3Service = s3Service;
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
                .doOnNext(wellnessInfoRepository::saveAll)
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
                .map(this::processImgByItem);
    }

    private Mono<TourBasicApiResponse> fetchFromTourBasicApi(CategoryDetail categoryDetail, int pageNo){

        // 추가 파라미터 설정
        Map<String, String> params = new HashMap<>();
        params.put("areaCode", GANGWONDO_AREACODE);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(NUM_OF_ROWS_BASIC));
        params.put("cat1", categoryDetail.getCat1());
        if (categoryDetail.getCat2() != null) {
            params.put("cat2", categoryDetail.getCat2());
        }
        if (categoryDetail.getCat3() != null) {
            params.put("cat3", categoryDetail.getCat3());
        }

        String uriString = webClientConfig.getTourBasicApiUrl(params);

        try {
            URI uri = new URI(uriString);
            return webClient.get()
                    .uri(uri)
                    .exchangeToMono(this::handleResponse);
        } catch (URISyntaxException e) {
            log.error("Invalid URI syntax: {}", e.getMessage());
            return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, "Invalid URI syntax"));
        }

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
                .map(this::processImgByItem);
    }

    private Mono<TourBasicApiResponse> fetchDataFromTourSearchApi(String keyword, int pageNo) {

        // 추가 파라미터 설정
        Map<String, String> params = new HashMap<>();
        params.put("areaCode", GANGWONDO_AREACODE);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(NUM_OF_ROWS_SEARCH));
        //params.put("keyword", keyword);

        // 파라미터 값 UTF-8로 인코딩
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
            params.put("keyword", encodedKeyword);
        } catch (UnsupportedEncodingException e) {
            log.error("Encoding failed for keyword: {}", e.getMessage());
            return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, "Encoding failed for keyword"));
        }

        String uriString = webClientConfig.getTourSearchApiUrl(params);

        try {
            URI uri = new URI(uriString);
            return webClient.get()
                    .uri(uri)
                    .exchangeToMono(this::handleResponse);
        } catch (URISyntaxException e) {
            log.error("Invalid URI syntax: {}", e.getMessage());
            return Mono.error(new CustomException(TOUR_API_RESPONSE_ERROR, "Invalid URI syntax"));
        }
    }

    private WellnessInfo convertToEntity(TourBasicApiResponse.Response.Body.Items.Item item, String s3ThumbnailUrl){
        try {
            return item.toEntity(s3ThumbnailUrl);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to convert item to entity: ", e);
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

    // 썸네일 이미지가 있을 경우 S3에 업로드
    private WellnessInfo processImgByItem(TourBasicApiResponse.Response.Body.Items.Item item) {
        String originalUrl = item.getFirstimage2();
        String s3Url = null;

        // 이미지 URL이 있는 경우 S3에 업로드
        if (originalUrl != null && !originalUrl.trim().isEmpty()) {
            s3Url = s3Service.uploadImgFromUrl(originalUrl, item.getContentid());
        }

        return convertToEntity(item, s3Url);
    }

    /**
     * 이미 저장된 웰니스 정보에 대해, 썸네일 이미지 S3에 업로드 후 컬럼 업데이트(s3ThumbnailUrl)
     * - thumbnailUrl: API에서 제공하는 썸네일 이미지 URL
     * - s3ThumbnailUrl: S3 객체 URL
     */
    @Transactional
    public void uploadThumbnailImgToS3(){
        List<WellnessInfo> hasThumbnailUrlWellnessInfoList = wellnessInfoRepository.findByThumbnailUrlNotNull();

        hasThumbnailUrlWellnessInfoList.forEach(wellnessInfo -> {
            String originalUrl = wellnessInfo.getThumbnailUrl();

            if(originalUrl != null){
                String s3Url = s3Service.uploadImgFromUrl(originalUrl, wellnessInfo.getContentId());
                wellnessInfo.updateS3ThumbnailUrl(s3Url);
            }
        });

        wellnessInfoRepository.saveAll(hasThumbnailUrlWellnessInfoList);
    }

}
