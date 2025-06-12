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
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_JSON_PARSING_ERROR;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_RESPONSE_ERROR;
import static com.wellcome.WellcomeBE.global.type.Keyword.KEYWORDS;

@Service
@Slf4j
public class WellnessInfoService2 {
    private final WebClient webClient;
    private final TourInfoApiWebClientConfig webClientConfig;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final TourApiErrorHandler errorHandler = new TourApiErrorHandler();
    private final S3Service s3Service;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GANGWONDO_AREACODE = "32";
    private static final int NUM_OF_ROWS_BASIC = 100;
    private static final int NUM_OF_ROWS_SEARCH = 5;

    public WellnessInfoService2(
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

    // 특정 카테고리에 대한 한국관광공사 API 호출 및 데이터 저장 로직 실행
    @Async("asyncExecutor")
    public CompletableFuture<Integer> fetchAndSaveByCategory(CategoryDetail category) {
        String categoryLog = category.getCat1() + " " + category.getCat2() + " " + category.getCat3();

        log.info("[비동기 스레드 확인] category='{}', thread='{}'", categoryLog, Thread.currentThread().getName());

        log.info("[시작] category='{}' 에 대한 API 호출 및 데이터 저장", categoryLog);

        int page = 1;
        boolean hasNext = true;
        int savedCount = 0;

        while (hasNext) {
            TourBasicApiResponse response = null;

            // API 호출
            try {
                response = fetchFromTourBasicApi(category, page);
            } catch (CustomException e) {
                log.error("[API 호출 실패] category='{}', page={}, error={}", categoryLog, page, e.getMessage(), e);
            } catch (Exception e) {
                log.error("[기타 오류] category='{}', page={}, error={}", categoryLog, page, e.getMessage(), e);
            }

            try{
                List<TourBasicApiResponse.Response.Body.Items.Item> items = response.getResponse().getBody().getItems().getItem();
                if(items == null || items.isEmpty()){
                    log.warn("[데이터 없음] category='{}', page={}", categoryLog, page);
                }

                // 썸네일 이미지 처리
                List<WellnessInfo> entities = items.stream()
                        .map(item -> {
                            try {
                                return processImgByItem(item);
                            } catch (Exception e){
                                log.error("[이미지 처리 오류] contentId={}, error={}", item.getContentid(), e.getMessage(), e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                // 데이터 DB 저장
                if(!entities.isEmpty()) {
                    wellnessInfoRepository.saveAll(entities);
                    savedCount += entities.size();
                    log.info("[저장 완료] category='{}', page={}, {}건", categoryLog, page, entities.size());
                }else{
                    log.warn("[저장 대상 없음] category='{}', page={}", categoryLog, page);
                }
            } catch(Exception e){
                log.error("[데이터 처리 오류] category='{}', page={}, error={}", categoryLog, page, e.getMessage(), e);
            }

            int totalCount = response.getResponse().getBody().getTotalCount();
            int pageNo = response.getResponse().getBody().getPageNo();
            hasNext = pageNo * NUM_OF_ROWS_BASIC < totalCount;
            page++;
        }

        log.info("[작업 완료] 카테고리: '{}' 에 대한 API 호출 및 데이터 저장 / {}건", categoryLog, savedCount);
        return CompletableFuture.completedFuture(savedCount);
    }

    // 지정 키워드들에 대한 한국관광공사 API 호출 및 데이터 저장 로직 실행
    public int fetchAndSaveByKeywords(){
        int savedCount = 0;

        for(String keyword : KEYWORDS){
            int page = 1;
            boolean hasNext = true;
            int savedCountPerKeyword = 0;

            while(hasNext){
                TourBasicApiResponse response = null;

                // API 호출
                try {
                    response = fetchFromTourSearchApi(keyword, page);
                } catch (CustomException e) {
                    log.error("[API 호출 실패] keyword='{}', page={}, error={}", keyword, page, e.getMessage(), e);
                } catch (Exception e) {
                    log.error("[기타 오류] keyword='{}', page={}, error={}", keyword, page, e.getMessage(), e);
                }

                try{
                    List<TourBasicApiResponse.Response.Body.Items.Item> items = response.getResponse().getBody().getItems().getItem();
                    if(items == null || items.isEmpty()){
                        log.warn("[데이터 없음] keyword='{}', page={}", keyword, page);
                    }

                    // 썸네일 이미지 처리
                    List<WellnessInfo> entities = items.stream()
                            .map(item -> {
                                try {
                                    return processImgByItem(item);
                                } catch (Exception e){
                                    log.error("[이미지 처리 오류] contentId={}, error={}", item.getContentid(), e.getMessage(), e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    // 데이터 DB 저장
                    if(!entities.isEmpty()) {
                        wellnessInfoRepository.saveAll(entities);
                        savedCountPerKeyword += entities.size();
                        //log.info("[저장 완료] keyword='{}', page={}, {}건", keyword, page, entities.size());
                    }else{
                        log.warn("[저장 대상 없음] keyword='{}', page={}", keyword, page);
                    }
                } catch(Exception e){
                    log.error("[데이터 처리 오류] keyword='{}', page={}, error={}", keyword, page, e.getMessage(), e);
                }

                int totalCount = response.getResponse().getBody().getTotalCount();
                int pageNo = response.getResponse().getBody().getPageNo();
                hasNext = pageNo * NUM_OF_ROWS_BASIC < totalCount;
                page++;
            }

            log.info("[작업 완료] 키워드: '{}' 에 대한 API 호출 및 데이터 저장 / {}건", keyword, savedCountPerKeyword);
            savedCount += savedCountPerKeyword;
        }

        return savedCount;
    }

    private TourBasicApiResponse fetchFromTourBasicApi(CategoryDetail categoryDetail, int pageNo) {

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
            ResponseEntity<String> response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            return handleResponse(response);
        } catch (URISyntaxException e) {
            throw new CustomException(TOUR_API_RESPONSE_ERROR, "Invalid URI syntax");
        }
    }

    private TourBasicApiResponse fetchFromTourSearchApi(String keyword, int pageNo) {

        // 추가 파라미터 설정
        Map<String, String> params = new HashMap<>();
        params.put("areaCode", GANGWONDO_AREACODE);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("numOfRows", String.valueOf(NUM_OF_ROWS_SEARCH));

        // 파라미터 값 UTF-8로 인코딩
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
            params.put("keyword", encodedKeyword);
        } catch (UnsupportedEncodingException e) {
            throw new CustomException(TOUR_API_RESPONSE_ERROR, "Encoding failed for keyword");
        }

        String uriString = webClientConfig.getTourSearchApiUrl(params);

        try {
            URI uri = new URI(uriString);
            ResponseEntity<String> response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            return handleResponse(response);
        } catch (URISyntaxException e) {
            throw new CustomException(TOUR_API_RESPONSE_ERROR, "Invalid URI syntax");
        }
    }

    // 썸네일 이미지가 있을 경우 S3에 업로드
    private WellnessInfo processImgByItem(TourBasicApiResponse.Response.Body.Items.Item item) {
        String originalUrl = item.getFirstimage2();
//        String s3Url = null;
//
//        // 이미지 URL이 있는 경우 S3에 업로드
//        if (originalUrl != null && !originalUrl.trim().isEmpty()) {
//            s3Url = s3Service.uploadImgFromUrl(originalUrl, item.getContentid());
//        }

//        return convertToEntity(item, s3Url);
        return convertToEntity(item, originalUrl); //TODO 임시
    }

    private WellnessInfo convertToEntity(TourBasicApiResponse.Response.Body.Items.Item item, String s3ThumbnailUrl){
        try {
            return item.toEntity(s3ThumbnailUrl);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to convert item to entity: ", e);
        }
    }

    private TourBasicApiResponse handleResponse(ResponseEntity<String> response){
        MediaType contentType = response.getHeaders().getContentType();
        String responseBody = response.getBody();

        // JSON 응답 처리
        if (MediaType.APPLICATION_JSON.equals(contentType)) {
            return handleJsonResponse(responseBody);
        }
        // XML 응답 처리 (에러 처리)
        else {
            String errorMessage = errorHandler.handleXmlErrorResponse(responseBody);
            throw new CustomException(TOUR_API_RESPONSE_ERROR, TOUR_API_RESPONSE_ERROR.getMessage() + ": " + errorMessage);
        }
    }

    // JSON 응답의 resultMsg 값에 따라 데이터 처리 및 에러 처리
    private TourBasicApiResponse handleJsonResponse(String responseBody){
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode responseNode = jsonNode.path("response");

            // 에러 처리
            if (responseNode.isEmpty() || responseNode.isMissingNode()) {
                String resultCode = jsonNode.path("resultCode").asText();
                String errorMessage = errorHandler.getErrorMessage(resultCode);
                throw new CustomException(TOUR_API_RESPONSE_ERROR, TOUR_API_RESPONSE_ERROR.getMessage() + ": " + errorMessage);
            }
            // 정상 데이터 처리
            else {
                return objectMapper.readValue(responseBody, TourBasicApiResponse.class);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 중 오류 발생: {}", e.getMessage());
            throw new CustomException(TOUR_API_JSON_PARSING_ERROR);
        }
    }
}
