package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WellnessInfoGoogleReviewResponse {

    private Double rating;
    private List<GoogleReview> reviewList;

    public static WellnessInfoGoogleReviewResponse from(
            Double rating,
            List<GoogleReview> reviewList
    ){
        return WellnessInfoGoogleReviewResponse.builder()
                .rating(rating)
                .reviewList(reviewList)
                .build();
    }

    @Getter
    @Builder
    public static class GoogleReview {
        private String authorName;
        private String profilePhotoUrl;
        private Integer rating;
        private String relativeTime;
        private String text;

        public static GoogleReview from(
                PlaceReviewResponse.PlaceResult.PlaceReview review
        ){
            return GoogleReview.builder()
                    .authorName(review.getAuthor_name())
                    .profilePhotoUrl(review.getProfile_photo_url())
                    .rating(review.getRating())
                    .relativeTime(review.getRelative_time_description())
                    .text(review.getText())
                    .build();
        }
    }

}
