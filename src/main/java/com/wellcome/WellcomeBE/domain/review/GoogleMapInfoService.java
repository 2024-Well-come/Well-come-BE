package com.wellcome.WellcomeBE.domain.review;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.repository.WellnessInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GoogleMapInfoService {

    @Value("${google_api.key}")
    private String apiKey;

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient webClient;
    private final WellnessInfoRepository wellnessInfoRepository;


    // 생성자 주입
    public GoogleMapInfoService(WebClient googlePlaceInfoWebClient, RedisTemplate<String, Object> redisTemplate, WellnessInfoRepository wellnessInfoRepository) {
        this.webClient = googlePlaceInfoWebClient;
        this.redisTemplate = redisTemplate;
        this.wellnessInfoRepository = wellnessInfoRepository;
    }

    /** placeId 찾기
     * https://maps.googleapis.com/maps/api/place/autocomplete/json?input=address&key=YOUR_API_KEY
     */

    // 모든 wellnessInfo 데이터를 가져와 처리
    public void processWellnessInfo() {
        // JPA 리포지토리에서 모든 데이터를 가져옴
        List<WellnessInfo> wellnessInfoList = wellnessInfoRepository.findAll();

        // 각 wellnessInfo에 대해 비동기 작업 처리
        wellnessInfoList.forEach(wellnessInfo -> {
            processSingleWellnessInfo(wellnessInfo)
                    .thenAccept(updatedWellnessInfo -> wellnessInfoRepository.save(updatedWellnessInfo));
        });
    }

    // 단일 wellnessInfo에 대해 외부 API 호출 및 parentId 저장 (비동기 처리)
    private CompletableFuture<WellnessInfo> processSingleWellnessInfo(WellnessInfo wellnessInfo) {
        return getPlaceId(wellnessInfo.getAddress(), wellnessInfo.getTitle())
                .map(placePredictionResponse -> {
                    // 외부 API로부터 parentId를 추출하고 wellnessInfo에 설정
                    String parentId = extractParentIdFromResponse(placePredictionResponse);
                    wellnessInfo.setParentId(parentId);
                    return wellnessInfo;
                }).toFuture();
    }

    // PlacePredictionResponse에서 parentId 추출
    private String extractParentIdFromResponse(PlacePredictionResponse response) {
        // 예시: PlacePredictionResponse 객체에서 parentId 추출 로직 작성
        log.info("저장할 값"+ response.getPredictions().get(0).getPlace_id());
        return response.getPredictions().get(0).getPlace_id(); // 필요에 따라 수정
    }

    // 비동기적으로 PlaceId 가져오기
    private Mono<PlacePredictionResponse> getPlaceId(String address, String title) {
        return getPlaceIdByTitle(title)
                .doOnNext(response -> log.info("Response from title lookup: {}", response))
                .switchIfEmpty(
                        getPlaceIdByAddress(address)
                                .doOnNext(response -> log.info("Response from address lookup: {}", response))
                );
    }


    private Mono<PlacePredictionResponse> getPlaceIdByAddress(String address) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("autocomplete/json")
                        .queryParam("input", address)
                        .queryParam("language", "ko")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(PlacePredictionResponse.class);
    }

    private Mono<PlacePredictionResponse> getPlaceIdByTitle(String title) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("autocomplete/json")
                        .queryParam("input", title)
                        .queryParam("language", "ko")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(PlacePredictionResponse.class);
    }


    /** 리뷰 조회 기능
     * https://maps.googleapis.com/maps/api/place/details/json?fields=name,rating,formatted_phone_number,opening_hours,reviews&place_id= &key=YOUR_API_KEY
     */

    public Mono<PlaceReviewResponse> getPlaceDetails(String placeId) {
        // Redis에서 캐시된 데이터를 먼저 조회
        PlaceReviewResponse cachedResponse = (PlaceReviewResponse) redisTemplate.opsForValue().get(placeId);
        if (cachedResponse != null) {
            // 캐시에 데이터가 있으면 이를 반환
            return Mono.just(cachedResponse);
        }
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("details/json")
                        .queryParam("fields", "name,rating,formatted_phone_number,opening_hours,website,user_ratings_total,reviews")
                        .queryParam("place_id", placeId)
                        .queryParam("language", "ko")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(PlaceReviewResponse.class)
                .doOnNext(response -> {
                    // API 호출 후 데이터를 Redis에 캐싱
                    redisTemplate.opsForValue().set(placeId, response, Duration.ofDays(7));
                });
    }

}
