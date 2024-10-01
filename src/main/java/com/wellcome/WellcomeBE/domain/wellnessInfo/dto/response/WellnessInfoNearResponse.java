package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
//v1

@Getter
@Builder
public class WellnessInfoNearResponse {
    private List<WellnessNearItem> nearbyList;

    public static WellnessInfoNearResponse from(List<WellnessNearItem> nearbyList){
        return WellnessInfoNearResponse.builder()
                .nearbyList(nearbyList)
                .build();
    }


    @Getter
    @Builder
    public static class  WellnessNearItem {
        private Long wellnessInfoId;
        private String thumbnailUrl;
        private String title;
        private String category;
        private Double rating;
        private int reviewNum;
        //private Boolean isOpen;
        private String thema;
        private String radius;

        public static WellnessNearItem form(WellnessInfo wellnessInfo, PlaceReviewResponse.PlaceResult placeDetails, double distance ) {
            // 구글맵 API 정보 (평점, 평점 수, 영업 상황)
            return WellnessNearItem.builder()
                    .wellnessInfoId(wellnessInfo.getId())
                    .thumbnailUrl(wellnessInfo.getThumbnailUrl())
                    .title(wellnessInfo.getTitle())
                    .category(wellnessInfo.getCategory().getName())
                    .rating(placeDetails != null ? placeDetails.getRating() : 0.0)
                    .reviewNum(placeDetails != null ? placeDetails.getUser_ratings_total() : 0)
                    //.isOpen(placeDetails != null ? OpeningHoursUtils.getOpenStatus(placeDetails).getIsOpen() : false)
                    .thema(wellnessInfo.getThema().getName())
                    .radius(String.format("%.1f km", distance))
                    .build();
        }
    }
}
