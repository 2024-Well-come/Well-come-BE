package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.OpeningHoursUtils;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WellnessNearbyDto {
    private Long wellnessInfoId;
    private String thumbnailUrl;
    private String title;
    private String category;
    private Double rating;
    private int reviewNum;
    private Boolean isOpen;
    private String thema;
    private String radius;

    public static WellnessNearbyDto form(WellnessInfo wellnessInfo, PlaceReviewResponse.PlaceResult placeDetails, double distance ) {
        // 구글맵 API 정보 (평점, 평점 수, 영업 상황)
        Double rating = null;
        Integer reviewNum = null;
        Boolean isOpen = null;

        if (placeDetails != null) {
            rating = placeDetails.getRating();
            reviewNum = placeDetails.getUser_ratings_total();

            OpeningHoursUtils.OpenStatus openStatus = OpeningHoursUtils.getOpenStatus(placeDetails);
            isOpen = openStatus.getIsOpen();
        }

            return WellnessNearbyDto.builder()
                    .wellnessInfoId(wellnessInfo.getId())
                    .thumbnailUrl(wellnessInfo.getThumbnailUrl())
                    .title(wellnessInfo.getTitle())
                    .category(wellnessInfo.getCategory().getName())
                    .rating(rating)
                    .reviewNum(reviewNum)
                    .isOpen(isOpen)
                    .thema(wellnessInfo.getThema().getName())
                    .radius(String.format("%.1f km", distance))
                    .build();
        }
}
