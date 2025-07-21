package com.wellcome.WellcomeBE.domain.review;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.config.GoogleReviewApiWebClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@Qualifier("jsonRedisTemplate")
public class GoogleMapInfoService2 {

    private final WebClient webClient;
    private final GoogleReviewApiWebClientConfig googleConfig;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final Executor executorService;

    public GoogleMapInfoService2(WebClient googlePlaceApiWebClient,
                                GoogleReviewApiWebClientConfig googleConfig,
                                WellnessInfoRepository wellnessInfoRepository,
                                 @Qualifier("asyncExecutor")Executor executorService) {
        this.webClient = googlePlaceApiWebClient;
        this.googleConfig = googleConfig;
        this.wellnessInfoRepository = wellnessInfoRepository;
        this.executorService = executorService;
    }

    // 1. 전체 병렬 처리
    public void processWellnessInfo() {
        List<WellnessInfo> wellnessInfoList = wellnessInfoRepository.findAll();

        List<CompletableFuture<Void>> futures = wellnessInfoList.stream()
                .map(wellnessInfo -> CompletableFuture
                        .supplyAsync(() -> {
                            log.info("[START] 처리 시작 - wellnessInfoId={} | thread={}", wellnessInfo.getId(), Thread.currentThread().getName());
                            return processSingleWellnessInfo(wellnessInfo);
                        }, executorService)
                        .thenCompose(future -> future)
                        .thenAccept(updated -> {
                            if (updated != null) {
                                wellnessInfoRepository.save(updated);
                                log.info("[UPDATED] 저장 완료 - wellnessInfoId={} | thread={}", updated.getId(), Thread.currentThread().getName());
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

    // 2. 단건 처리
    private CompletableFuture<WellnessInfo> processSingleWellnessInfo(WellnessInfo wellnessInfo) {
        log.info("[API_CALL] 외부 API 호출 준비 - wellnessInfoId={} | thread={}", wellnessInfo.getId(), Thread.currentThread().getName());
        return getPlaceId(wellnessInfo.getAddress(), wellnessInfo.getTitle())
                .thenApply(response -> {
                    log.info("[API_RESPONSE] 응답 수신 완료 - wellnessInfoId={} | thread={}", wellnessInfo.getId(), Thread.currentThread().getName());
                    if (response != null && !response.getPredictions().isEmpty()) {
                        // 외부 API로부터 parentId를 추출하고 wellnessInfo에 설정
                        String parentId = extractParentIdFromResponse(response);
                        wellnessInfo.setParentId(parentId);
                        return wellnessInfo;
                    }
                    log.warn("[NO_RESULT] 결과 없음 - wellnessInfoId={} | thread={}", wellnessInfo.getId(), Thread.currentThread().getName());
                    return null;

                });
    }

    private String extractParentIdFromResponse(PlacePredictionResponse response) {
        String placeId = response.getPredictions().get(0).getPlace_id();
        log.info("[EXTRACT] Place ID 추출 완료 - placeId={} | thread={}", placeId, Thread.currentThread().getName());
        return placeId;
    }

    // 3. 주소/타이틀 우선순위 검색
    private CompletableFuture<PlacePredictionResponse> getPlaceId(String address, String title) {
        return getPlaceIdByTitle(title).thenCompose(response -> {
            if (response != null && !response.getPredictions().isEmpty()) {
                return CompletableFuture.completedFuture(response);
            } else {
                return getPlaceIdByAddress(address);
            }
        });
    }

    // 4. 타이틀 기반 검색
    private CompletableFuture<PlacePredictionResponse> getPlaceIdByTitle(String title) {
        Map<String, String> params = new HashMap<>();
        params.put("input", title);

        String uri = googleConfig.buildPlaceUri("autocomplete/json", params);

        return webClient.get()
                .uri(URI.create(uri))
                .retrieve()
                .bodyToMono(PlacePredictionResponse.class)
                .toFuture();
    }

    // 5. 주소 기반 검색
    private CompletableFuture<PlacePredictionResponse> getPlaceIdByAddress(String address) {
        Map<String, String> params = new HashMap<>();
        params.put("input", address);

        String uri = googleConfig.buildPlaceUri("autocomplete/json", params);

        log.info("[REQUEST] 주소 기반 요청 - address={} | uri={} | thread={}", address, uri, Thread.currentThread().getName());
        return webClient.get()
                .uri(URI.create(uri))
                .retrieve()
                .bodyToMono(PlacePredictionResponse.class)
                .toFuture();
    }

}
