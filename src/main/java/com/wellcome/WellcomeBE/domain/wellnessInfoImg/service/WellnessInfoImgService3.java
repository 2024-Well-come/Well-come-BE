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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
@Slf4j
public class WellnessInfoImgService3 {

    private final WebClient webClient;
    private final TourInfoApiWebClientConfig webClientConfig;
    private final WellnessInfoRepository wellnessInfoRepository;
    private final WellnessInfoImgRepository wellnessInfoImgRepository;
    private final TourApiErrorHandler errorHandler = new TourApiErrorHandler();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicInteger dbLookupCount = new AtomicInteger(0); // 💡 DB 조회 카운터 추가

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
        List<WellnessContentIdOnly> wellnessList = wellnessInfoRepository.findAllContentIdOnly();

        if (wellnessList == null || wellnessList.isEmpty()) {
            log.warn("Wellness 리스트가 비어 있습니다.");
            return;
        }

        log.info("[메인 스레드 시작] thread={}", Thread.currentThread().getName());

        List<CompletableFuture<Void>> futures = wellnessList.stream()
                .map(contentIdObj -> CompletableFuture.runAsync(() -> {
                    String contentId = contentIdObj.getContentId();
                    log.info("[스레드 시작] contentId={}, thread={}", contentId, Thread.currentThread().getName());
                    try {
                        List<WellnessInfoImg> images = fetchDataAndConvert(contentId);
                        wellnessInfoImgRepository.saveAll(images);
                        log.info("[스레드 완료] contentId={}, 저장 건수={}", contentId, images.size());
                    } catch (Exception e) {
                        log.error("[스레드 실패] contentId={}, error={}", contentId, e.getMessage(), e);
                    }
                }, asyncExecutor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        long elapsed = Duration.between(start, Instant.now()).toMillis();
        log.info("** API 호출 및 데이터 저장 완료 ** \n" +
                "  - 총 처리 시간: {}ms \n" +
                "  - 실제 DB 조회 수: {}건 \n" +
                "  - 총 contentId 수: {}건", elapsed, dbLookupCount.get(),wellnessList.size());
    }

    private List<WellnessInfoImg> fetchDataAndConvert(String contentId) throws Exception {
        String uriString = webClientConfig.getTourImageApiUrl(Map.of("contentId", contentId));
        URI uri = new URI(uriString);

        String responseBody = webClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(String.class)
                .toFuture()
                .get()
                .getBody();

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode itemsNode = root.path("response").path("body").path("items").path("item");

        if (itemsNode.isMissingNode() || !itemsNode.isArray() || itemsNode.size() == 0) {
            log.warn("[데이터 없음] contentId={}", contentId);
            return Collections.emptyList();
        }


        //한계값을 위한 임시입니다. 삭제하거라~
        int currentCount = dbLookupCount.incrementAndGet();
        log.info("[DB 조회] contentId={}, 현재까지 조회 수={}", contentId, currentCount);

        WellnessInfo wellness = wellnessInfoRepository.findByContentId(contentId)
                .orElseThrow(() -> new IllegalArgumentException("No wellness info found for contentId: " + contentId));

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
}
