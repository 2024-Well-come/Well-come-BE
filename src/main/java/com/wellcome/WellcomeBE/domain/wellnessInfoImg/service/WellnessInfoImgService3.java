package com.wellcome.WellcomeBE.domain.wellnessInfoImg.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.WellnessInfoImg;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.dto.response.TourImageApiResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfoImg.repository.WellnessInfoImgRepository;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import com.wellcome.WellcomeBE.global.config.TourInfoApiWebClientConfig;
import com.wellcome.WellcomeBE.global.exception.CustomErrorCode;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import com.wellcome.WellcomeBE.global.exception.TourApiErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.io.ParseException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.WELLNESS_INFO_CONTENT_ID_NOT_FOUND;


@Service
@Slf4j
public class WellnessInfoImgService3 {

    private final WebClient webClient;
    private final TourInfoApiWebClientConfig webClientConfig;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final WellnessInfoImgRepository wellnessInfoImgRepository;
    private final TourApiErrorHandler errorHandler = new TourApiErrorHandler();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    //private final AtomicInteger dbLookupCount = new AtomicInteger(0); // 💡 DB 조회 카운터 추가

    // AsyncExecutor 주입 (ThreadPoolTaskExecutor)
    private final Executor asyncExecutor;

    public WellnessInfoImgService3(
            WebClient webClient,
            WellnessInfoRepository wellnessInfoRepository,
            TourInfoApiWebClientConfig webClientConfig,
            WellnessInfoImgRepository wellnessInfoImgRepository,
            Executor asyncExecutor
    ) {
        this.webClient = webClient;
        this.wellnessInfoRepository = wellnessInfoRepository;
        this.webClientConfig = webClientConfig;
        this.wellnessInfoImgRepository = wellnessInfoImgRepository;
        this.asyncExecutor = asyncExecutor;
    }

    public void fetchAndSaveTourImages() throws Exception {
        Instant start = Instant.now();
        List<WellnessInfo> wellnessList = wellnessInfoRepository.findAll();


        if (wellnessList.isEmpty()) {
            log.warn("Wellness 리스트가 비어 있습니다.");
            return;
        }
        Map<String, WellnessInfo> wellnessMap = wellnessList.stream()
                .collect(Collectors.toMap(WellnessInfo::getContentId, wi -> wi));


        log.info("[메인 스레드 시작] thread={}", Thread.currentThread().getName());

        List<String> failedContentIds = Collections.synchronizedList(new ArrayList<>());

        List<CompletableFuture<Void>> futures = wellnessList.stream()
                .map(contentIdObj -> CompletableFuture.runAsync(() -> {
                    String contentId = contentIdObj.getContentId();
                    WellnessInfo wellness = wellnessMap.get(contentId);
                    log.info("[스레드 시작] contentId={}, thread={}", contentId, Thread.currentThread().getName());
                    try {
                        List<WellnessInfoImg> images = fetchDataAndConvert(contentId,wellness);
                        wellnessInfoImgRepository.saveAll(images);
                        log.info("[스레드 완료] contentId={}, 저장 건수={}", contentId, images.size());
                    } catch (Exception e) {
                        failedContentIds.add(contentId);
                        log.error("[스레드 실패] contentId={}, error={}", contentId, e.getMessage(), e);
                    }
                }, asyncExecutor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        long elapsed = Duration.between(start, Instant.now()).toMillis();
        log.info("** 1차 처리 완료 ** \n" +
                        "  - 총 처리 시간: {}ms \n" +
                        //"  - 실제 DB 조회 수: {}건 \n" +
                        "  - 총 contentId 수: {}건 \n" +
                        "  - 실패 contentId 수: {}건",
                elapsed, wellnessList.size(), failedContentIds.size());

        if (!failedContentIds.isEmpty()) {
            log.info("🔁 {}건의 실패 요청에 대해 재시도 시작 (3초 후)", failedContentIds.size());
            Thread.sleep(3000);
            retryFailedContentIds(failedContentIds, wellnessMap);
        }
    }

    public void singleFetchAndSaveTourImages(String contentId) {
        CompletableFuture.runAsync(() -> {
            log.info("[단일 요청 스레드 시작] contentId={}, thread={}", contentId, Thread.currentThread().getName());
            try {
                WellnessInfo wellness = wellnessInfoRepository.findByContentId(contentId)
                        .orElseThrow(() -> new CustomException(WELLNESS_INFO_CONTENT_ID_NOT_FOUND));

                List<WellnessInfoImg> images = fetchDataAndConvert(contentId, wellness);
                wellnessInfoImgRepository.saveAll(images);
                log.info("[단일 요청 스레드 완료] contentId={}, 저장 건수={}", contentId, images.size());
            } catch (Exception e) {
                log.error("[단일 요청 실패] contentId={}, error={}", contentId, e.getMessage(), e);
            }
        }, asyncExecutor);
    }


    private List<WellnessInfoImg> fetchDataAndConvert(String contentId, WellnessInfo wellness) throws Exception {
        String uriString = webClientConfig.getTourImageApiUrl(Map.of("contentId", contentId));
        URI uri = new URI(uriString);

        ResponseEntity<String> response;
        try {
            response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .toEntity(String.class)
                    .toFuture()
                    .get(); // 동기 블로킹
        } catch (Exception e) {
            throw wrapCustomException(e);
        }

        String responseBody = response.getBody();
        MediaType contentType = response.getHeaders().getContentType();

        if (!MediaType.APPLICATION_JSON.equals(contentType)) {
            String message = errorHandler.handleXmlErrorResponse(responseBody);
            throw new CustomException(CustomErrorCode.TOUR_API_RESPONSE_ERROR, "XML 오류 응답: " + message);
        }

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode itemsNode = root.path("response").path("body").path("items").path("item");

        if (itemsNode.isMissingNode() || !itemsNode.isArray() || itemsNode.size() == 0) {
            log.warn("[데이터 없음] contentId={}", contentId);
            return Collections.emptyList();
        }


        //한계값을 위한 임시입니다. 삭제하거라~
        ///int currentCount = dbLookupCount.incrementAndGet();
        //log.info("[DB 조회] contentId={}, 현재까지 조회 수={}", contentId, currentCount);

        List<WellnessInfoImg> images = new ArrayList<>();
        for (JsonNode node : itemsNode) {
            try {
                TourImageApiResponse.Response.Body.Items.Item item =
                        objectMapper.treeToValue(node, TourImageApiResponse.Response.Body.Items.Item.class);
                images.add(item.toEntity(wellness));
            } catch (ParseException e) {
                log.warn("[엔티티 변환 실패] contentId={}, error={}", contentId, e.getMessage());
            }
        }

        return images;
    }

    private void retryFailedContentIds(List<String> failedContentIds, Map<String, WellnessInfo> wellnessMap) throws Exception{

    List<String> retryFailList = Collections.synchronizedList(new ArrayList<>());

        List<CompletableFuture<Void>> retryFutures = failedContentIds.stream()
                .map(contentId -> CompletableFuture.runAsync(() -> {
                    log.info("[재시도 시작] contentId={}, thread={}", contentId, Thread.currentThread().getName());
                    try {
                        List<WellnessInfoImg> images =fetchDataAndConvert(contentId, wellnessMap.get(contentId));
                        wellnessInfoImgRepository.saveAll(images);
                        log.info("[재시도 성공] contentId={}, 저장 건수={}", contentId, images.size());
                    } catch (Exception e) {
                        retryFailList.add(contentId);
                        log.error("[재시도 실패] contentId={}, error={}", contentId, e.getMessage(), e);
                    }
                }, asyncExecutor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(retryFutures.toArray(new CompletableFuture[0])).get();

        log.info("✅ 재시도 완료 - 성공: {}건 / 실패: {}건",
                failedContentIds.size() - retryFailList.size(), retryFailList.size());

        if (!retryFailList.isEmpty()) {
            log.warn("🚫 최종 실패 contentId 목록: {}", retryFailList);
        }
    }

    private CustomException wrapCustomException(Exception e) {
        if (e instanceof URISyntaxException)
            return new CustomException(CustomErrorCode.TOUR_API_URI_ERROR, e.getMessage());
        if (e instanceof WebClientRequestException)
            return new CustomException(CustomErrorCode.TOUR_API_REQUEST_ERROR, e.getMessage());
        if (e instanceof WebClientResponseException)
            return new CustomException(CustomErrorCode.TOUR_API_RESPONSE_ERROR, e.getMessage());
        return new CustomException(CustomErrorCode.TOUR_API_UNKNOWN_ERROR, e.getMessage());
    }

}
