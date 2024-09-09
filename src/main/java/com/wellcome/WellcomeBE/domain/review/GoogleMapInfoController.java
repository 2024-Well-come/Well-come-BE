package com.wellcome.WellcomeBE.domain.review;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class GoogleMapInfoController {

    private final GoogleMapInfoService googlePlaceService;

    @GetMapping("/{place}/details")
    public Mono<PlaceReviewResponse> getPlaceDetails(@PathVariable String place) {
        return googlePlaceService.getPlaceDetails(place);
    }

    @GetMapping("/place/id")
    public void getPlacePlaceId(){
        googlePlaceService.processWellnessInfo();
    }
}
