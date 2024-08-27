package com.wellcome.WellcomeBE.domain.review;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ReviewService {

    @Value("${google_api.key}")
    private String apiKey;

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient webClient;


    // 생성자 주입
    public ReviewService(WebClient googlePlaceInfoWebClient) {
        this.webClient = googlePlaceInfoWebClient;
    }

    /** placeId 찾기
     * https://maps.googleapis.com/maps/api/place/autocomplete/json?input=address&key=YOUR_API_KEY
     */

    public Mono<PlacePredictionResponse> getPlaceId(String address) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("autocomplete/json")
                        .queryParam("input",address)
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
                        .queryParam("fields", "name,rating,formatted_phone_number,opening_hours,reviews")
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
