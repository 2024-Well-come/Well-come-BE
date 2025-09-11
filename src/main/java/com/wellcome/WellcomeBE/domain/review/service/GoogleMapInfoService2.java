package com.wellcome.WellcomeBE.domain.review.service;

import com.wellcome.WellcomeBE.domain.review.dto.PlacePredictionResponse;
import com.wellcome.WellcomeBE.domain.review.dto.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.config.GoogleReviewApiWebClientConfig;
import com.wellcome.WellcomeBE.global.exception.GoogleApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoogleMapInfoService2 {

    private final WebClient webClient;
    private final GoogleReviewApiWebClientConfig googleConfig;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final Executor executorService;

    public GoogleMapInfoService2(WebClient googlePlaceApiWebClient,
                                 GoogleReviewApiWebClientConfig googleConfig,
                                 WellnessInfoRepository wellnessInfoRepository,
                                 @Qualifier("reactiveJsonRedisTemplate") ReactiveRedisTemplate<String, Object> reactiveRedisTemplate,
                                 @Qualifier("asyncExecutor") Executor executorService) {
        this.webClient = googlePlaceApiWebClient;
        this.googleConfig = googleConfig;
        this.wellnessInfoRepository = wellnessInfoRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.executorService = executorService;
    }

    // ------------------ Google API 호출 (PlacePrediction) ------------------
    private CompletableFuture<PlacePredictionResponse> callPlacePredictionApi(String uri) {
        return webClient.get()
                .uri(URI.create(uri))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new GoogleApiException(
                                        String.valueOf(clientResponse.statusCode().value()),
                                        body
                                )))
                )
                .bodyToMono(PlacePredictionResponse.class)
                .flatMap(response -> {
                    if (!"OK".equals(response.getStatus())) {
                        String msg = response.getError_message() != null ? response.getError_message() : "Unknown Google API error";
                        return Mono.error(new GoogleApiException(response.getStatus(), msg));
                    }
                    return Mono.just(response);
                })
                .toFuture();
    }

    // ------------------ Google API 호출 (PlaceReview) ------------------
    private CompletableFuture<PlaceReviewResponse> callPlaceReviewApi(String uri) {
        return webClient.get()
                .uri(URI.create(uri))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new GoogleApiException(
                                        String.valueOf(clientResponse.statusCode().value()),
                                        body
                                )))
                )
                .bodyToMono(PlaceReviewResponse.class)
                .flatMap(response -> {
                    // HTTP 200이더라도 Google API 오류 발생됨 -> status, error_message 존재
                    if (!"OK".equals(response.getStatus())) {
                        String msg = response.getError_message() != null
                                ? response.getError_message()
                                : "Unknown Google API error";
                        return Mono.error(new GoogleApiException(response.getStatus(), msg));
                    }
                    return Mono.just(response);
                })
                .toFuture();
    }

    // ------------------ 타이틀 기반 검색 ------------------
    private CompletableFuture<PlacePredictionResponse> getPlaceIdByTitle(String title) {
        String uri = googleConfig.buildPlaceUri("autocomplete/json", Map.of("input", title));
        return callPlacePredictionApi(uri);
    }

    // ------------------ 주소 기반 검색 ------------------
    private CompletableFuture<PlacePredictionResponse> getPlaceIdByAddress(String address) {
        String uri = googleConfig.buildPlaceUri("autocomplete/json", Map.of("input", address));
        log.info("[REQUEST] 주소 기반 요청 - address={} | uri={}", address, uri);
        return callPlacePredictionApi(uri);
    }

    // ------------------ 주소/타이틀 우선순위 검색 ------------------
    private CompletableFuture<PlacePredictionResponse> getPlaceId(String address, String title) {
        return getPlaceIdByTitle(title).thenCompose(response -> {
            if (response != null && !response.getPredictions().isEmpty()) {
                return CompletableFuture.completedFuture(response);
            } else {
                return getPlaceIdByAddress(address);
            }
        });
    }

    // ------------------ ParentId 추출 ------------------
    private String extractParentIdFromResponse(PlacePredictionResponse response) {
        return response.getPredictions().get(0).getPlace_id();
    }

    // ------------------ 다중 ------------------
    private CompletableFuture<String> processWellnessInfoGoogleMapParentId(WellnessInfo wellnessInfo) {
        return getPlaceId(wellnessInfo.getAddress(), wellnessInfo.getTitle())
                .thenApply(response -> {
                    if (response != null && !response.getPredictions().isEmpty()) {
                        return extractParentIdFromResponse(response);
                    }
                    return null;
                });
    }

    // ------------------ 단건 처리 ------------------
    public void processAndSaveSingleWellnessInfo(WellnessInfo wellnessInfo) {
        processWellnessInfoGoogleMapParentId(wellnessInfo)
                .thenAccept(parentId -> {
                    if (parentId != null) {
                        wellnessInfoRepository.updateParentIdById(wellnessInfo.getId(), parentId);
                    }
                })
                .exceptionally(ex -> {
                    log.error("[단일 처리 실패] id={}, error={}", wellnessInfo.getId(), ex.getMessage(), ex);
                    return null;
                });
    }

    // ------------------ 전체 병렬 처리 ------------------
    public void processWellnessInfo() {
        List<WellnessInfo> wellnessInfoList = wellnessInfoRepository.findAll();
        List<CompletableFuture<Void>> futures = wellnessInfoList.stream()
                .map(wellnessInfo -> CompletableFuture
                        .supplyAsync(() -> processWellnessInfoGoogleMapParentId(wellnessInfo), executorService)
                        .thenCompose(future -> future)
                        .thenAccept(parentId -> {
                            if (parentId != null) {
                                wellnessInfoRepository.updateParentIdById(wellnessInfo.getId(), parentId);
                            }
                        })
                        .exceptionally(ex -> {
                            log.error("[비동기 실패] wellnessInfoId={}, error={}", wellnessInfo.getId(), ex.getMessage(), ex);
                            return null;
                        })
                )
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    // ------------------ 리뷰 조회 ------------------
    private Mono<PlaceReviewResponse> fetchAndCachePlaceDetails(String placeId) {
        String uri = googleConfig.buildPlaceUri("details/json", Map.of(
                "fields", "name,rating,formatted_phone_number,opening_hours,website,user_ratings_total,reviews",
                "place_id", placeId
        ));
        return Mono.fromFuture(callPlaceReviewApi(uri))
                .flatMap(response -> reactiveRedisTemplate.opsForValue()
                        .set(placeId, response, Duration.ofDays(7))
                        .thenReturn(response)
                );
    }

    public Mono<PlaceReviewResponse> getPlaceDetails(String placeId) {
        return reactiveRedisTemplate.opsForValue()
                .get(placeId)
                .cast(PlaceReviewResponse.class)
                .switchIfEmpty(fetchAndCachePlaceDetails(placeId));
    }
}
