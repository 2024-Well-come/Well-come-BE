package com.wellcome.WellcomeBE.domain.review;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.config.GoogleReviewApiWebClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
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
                                 @Qualifier("asyncExecutor") Executor executorService) {
        this.webClient = googlePlaceApiWebClient;
        this.googleConfig = googleConfig;
        this.wellnessInfoRepository = wellnessInfoRepository;
        this.executorService = executorService;
    }

    //전체 병렬 처리
    public void processWellnessInfo() {
        List<WellnessInfo> wellnessInfoList = wellnessInfoRepository.findAll();

        List<CompletableFuture<Void>> futures = wellnessInfoList.stream()
                .map(wellnessInfo -> CompletableFuture
                        .supplyAsync(() -> processWellnessInfoGoogleMapParentId(wellnessInfo), executorService)
                        .thenCompose(future -> future)
                        .thenAccept(parentId -> {
                            if (parentId != null) {
                                wellnessInfoRepository.updateParentIdById(wellnessInfo.getId(), parentId);
                                //log.info("[UPDATED] DB 업데이트 완료 - id={}, parentId={}", wellnessInfo.getId(), parentId);
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

    //단건 처리 래퍼 - 업데이트 포함
    public void processAndSaveSingleWellnessInfo(WellnessInfo wellnessInfo) {
        processWellnessInfoGoogleMapParentId(wellnessInfo)
                .thenAccept(parentId -> {
                    if (parentId != null) {
                        wellnessInfoRepository.updateParentIdById(wellnessInfo.getId(), parentId);
                        //log.info("[SINGLE_UPDATED] DB 업데이트 완료 - id={}, parentId={}", wellnessInfo.getId(), parentId);
                    }
                })
                .exceptionally(ex -> {
                    log.error("[단일 처리 실패] id={}, error={}", wellnessInfo.getId(), ex.getMessage(), ex);
                    return null;
                });
    }

    //핵심 비즈니스 로직만 (API 호출-> parentId 추출)
    private CompletableFuture<String> processWellnessInfoGoogleMapParentId(WellnessInfo wellnessInfo) {
        //log.info("[API_CALL] 외부 API 호출 준비 - wellnessInfoId={} | thread={}", wellnessInfo.getId(), Thread.currentThread().getName());
        return getPlaceId(wellnessInfo.getAddress(), wellnessInfo.getTitle())
                .thenApply(response -> {
                    if (response != null && !response.getPredictions().isEmpty()) {
                        String parentId = extractParentIdFromResponse(response);
                        //log.info("[EXTRACT] parentId 추출 완료 - id={}, parentId={}", wellnessInfo.getId(), parentId);
                        return parentId;
                    }
                    //log.warn("[NO_RESULT] 결과 없음 - wellnessInfoId={}", wellnessInfo.getId());
                    return null;
                });
    }

    private String extractParentIdFromResponse(PlacePredictionResponse response) {
        return response.getPredictions().get(0).getPlace_id();
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

        log.info("[REQUEST] 주소 기반 요청 - address={} | uri={}", address, uri);
        return webClient.get()
                .uri(URI.create(uri))
                .retrieve()
                .bodyToMono(PlacePredictionResponse.class)
                .toFuture();
    }
}
