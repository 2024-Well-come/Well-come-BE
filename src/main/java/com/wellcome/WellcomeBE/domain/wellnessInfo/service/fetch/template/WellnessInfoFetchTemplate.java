package com.wellcome.WellcomeBE.domain.wellnessInfo.service.fetch.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.TourBasicApiResponse;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.exception.TourApiErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.*;

@Slf4j
@RequiredArgsConstructor
public abstract class WellnessInfoFetchTemplate<P, T> {
    private final WebClient webClient;
    private final WellnessInfoSaver<T> saver;
    private final TourApiErrorHandler errorHandler = new TourApiErrorHandler();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String GANGWONDO_AREACODE = "32";
    private static final int retryLimit = 2; //API 호출 재시도 가능 횟수 제한
    private static final int consecutiveFailsLimit = 3; //API 호출이 연속 실패하는 페이지 횟수 제한

    /**
     * 특정 카테고리 또는 키워드에 대한 API 호출 및 데이터 저장
     * @param type: CategoryDetail(카테고리) or String(키워드)
     */
    @Async("asyncExecutor")
    public CompletableFuture<Integer> fetchAndSave(P type) { //type: CategoryDetail or String

        int page = 1;
        boolean hasNext = true;
        int savedCount = 0;

        List<Integer> failedPages = new ArrayList<>(); //API 호출 실패한 페이지 정보
        int consecutiveFails = 0; //API 호출이 연속적으로 실패한 페이지 수

        String label = getLabel(type);
        logStart(label);
        log.info("[비동기 스레드 확인] '{}', thread='{}'", label, Thread.currentThread().getName());

        while (hasNext) {
            TourBasicApiResponse response = null;
            boolean success = false;

            // API 호출
            for(int attempt = 1; attempt <= retryLimit; attempt++){
                try {
                    response = fetchFromApi(type, page).get();
                    success = true;
                    break;
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof CustomException ce) {
                        if (ce.getCustomErrorCode() == TOUR_API_REQUEST_ERROR) {
                            logRetry(label, page, attempt, ce);
                            continue;
                        }
                        logError(label, page, ce);
                    } else {
                        logError(label, page, cause);
                    }
                    break;
                } catch (InterruptedException e) {
                    logError(label, page, e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if(!success){
                failedPages.add(page);
                if(++consecutiveFails > consecutiveFailsLimit) break;
                page++;
                continue;
            }else{
                consecutiveFails = 0;
            }

            // 응답 받은 데이터 처리
            try{
                TourBasicApiResponse.Response.Body.Items items = response.getResponse().getBody().getItems();
                if(items == null) {
                    logNoDataFound(label, page);
                }else{
                    final int currentPage = page;

                    // DTO -> Entity 변환
                    List<TourBasicApiResponse.Response.Body.Items.Item> itemList = items.getItem();
                    List<T> entities = itemList.stream()
                            .map(item -> {
                                try {
                                    return saver.convertEntity(item);
                                } catch (ParseException e) {
                                    logConvertError(label, currentPage, e);
                                    return null;
                                }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    // 데이터 DB 저장
                    if(!entities.isEmpty()) {
                        saver.saveAll(entities);
                        savedCount += entities.size();
                        logPageSaved(label, page, savedCount);
                    }
                }
            } catch(Exception e) {
                logProcessError(label, page, e);
            }

            int totalCount = response.getResponse().getBody().getTotalCount();
            int pageNo = response.getResponse().getBody().getPageNo();
            hasNext = pageNo * getNumOfRows() < totalCount;
            page++;
        }

        logComplete(label, savedCount, failedPages);
        return CompletableFuture.completedFuture(savedCount);
    }

    /**
     * 특정 카테고리 또는 키워드 + 특정 페이지에 대한 API 호출
     * @param type: CategoryDetail(카테고리) or String(키워드)
     */
    private CompletableFuture<TourBasicApiResponse> fetchFromApi(P type, int pageNo){

        // 파라미터 구성 및 URI 생성
        Map<String, String> params = new HashMap<>();
        params.put("areaCode", GANGWONDO_AREACODE);
        String uriString = constructUri(params, type, pageNo);

        // API 호출
        try {
            URI uri = new URI(uriString);
            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class)
                    .toFuture()
                    .thenApply(this::handleResponse);
        } catch (Exception e) {
            return failAsFuture(wrapCustomException(e));
        }
    }

    private CustomException wrapCustomException(Exception e) {
        if (e instanceof URISyntaxException)
            return new CustomException(TOUR_API_URI_ERROR, e.getMessage());
        if (e instanceof WebClientRequestException)
            return new CustomException(TOUR_API_REQUEST_ERROR, e.getMessage());
        if (e instanceof WebClientResponseException)
            return new CustomException(TOUR_API_RESPONSE_ERROR, e.getMessage());
        return new CustomException(TOUR_API_UNKNOWN_ERROR, e.getMessage());
    }

    private <T> CompletableFuture<T> failAsFuture(Throwable ex) {
        CompletableFuture<T> failed = new CompletableFuture<>();
        failed.completeExceptionally(ex);
        return failed;
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
            throw new CustomException(TOUR_API_JSON_PARSING_ERROR);
        }
    }

    protected abstract String getLabel(P type);
    protected abstract String constructUri(Map<String, String> params, P type, int pageNo);
    protected abstract int getNumOfRows(); //한 페이지당 불러올 데이터 수

    /**
     * 공통 로깅
     */
    private void logStart(String label){
        log.info("[시작] '{}' 에 대한 API 호출 및 데이터 저장", label);
    }
    private void logRetry(String label, int page, int attempt, Exception e) {
        log.warn("[재시도 {}/{}] '{}': page={}, causeType={}, error={}", attempt, retryLimit, label, page, e.getClass().getSimpleName(), e.getMessage());
    }
    private void logError(String label, int page, Throwable e) {
        log.error("[API 호출 실패] '{}': page={}, error={}", label, page, e.getMessage(), e);
    }
    private void logNoDataFound(String label, int page) {
        log.warn("[조회된 데이터 없음] '{}': page={}", label, page);
    }
    private void logConvertError(String label, int page, Exception e) {
        log.error("[엔티티 변환 실패] '{}': page={}, error={}", label, page, e.getMessage(), e);
    }
    private void logProcessError(String label, int page, Exception e) {
        log.error("[데이터 처리 오류] '{}': page={}, error={}", label, page, e.getMessage(), e);
    }
    private void logPageSaved(String label, int page, int count) {
        log.info("[페이지 저장 완료] '{}': page={}, savedCnt={}", label, page, count);
    }
    private void logComplete(String label, int savedCount, List<Integer> failedPages){
        log.info("[완료] '{}' 에 대한 API 호출 및 데이터 저장 / 저장 건수: {} / 실패한 페이지 목록: {}", label, savedCount, failedPages);
    }
}
