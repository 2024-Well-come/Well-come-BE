package com.wellcome.WellcomeBE.domain.review.controller;

import com.wellcome.WellcomeBE.domain.review.dto.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.review.service.GoogleMapInfoService2;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class GoogleMapInfoController {

    private final GoogleMapInfoService2 googlePlaceService2;

    // 리뷰
    @GetMapping("/{place}/details")
    public Mono<PlaceReviewResponse> getPlaceDetails(@PathVariable String place) {
        return googlePlaceService2.getPlaceDetails(place);
    }

    // placeId 병렬
    @GetMapping("/place/id")
    public Mono<Void> getPlacePlaceId(){
        return Mono.fromFuture(googlePlaceService2.processWellnessInfo());
    }
}
