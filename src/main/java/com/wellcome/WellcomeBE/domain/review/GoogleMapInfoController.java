package com.wellcome.WellcomeBE.domain.review;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class GoogleMapInfoController {

    private final GoogleMapInfoService googlePlaceService;

    @GetMapping("/place/details")
    public Mono<PlaceReviewResponse> getPlaceDetails() {
        return googlePlaceService.getPlaceDetails("ChIJn5peWMrlYTURS0SwuW8GLpQ");
    }

    @GetMapping("/place/id")
    public void getPlacePlaceId(){
        googlePlaceService.processWellnessInfo();
    }
}
