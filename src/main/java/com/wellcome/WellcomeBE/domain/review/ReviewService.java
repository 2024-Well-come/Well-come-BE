package com.wellcome.WellcomeBE.domain.review;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ReviewService {

    @Value("${google_api.key}")
    private String apiKey;

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
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("details/json")
                        .queryParam("fields", "name,rating,formatted_phone_number,opening_hours,reviews")
                        .queryParam("place_id", placeId)
                        .queryParam("language", "ko")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(PlaceReviewResponse.class);
    }

}
