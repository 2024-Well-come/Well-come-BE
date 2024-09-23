package com.wellcome.WellcomeBE.domain.community.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.List;

@Getter
public class CommunityReviewRequest {

    private Long planId;

    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @NotBlank(message = "여행 전체 후기는 필수 입력값입니다.")
    private String content;

    private List<UserReview> reviewList;

    @Getter
    public static class UserReview {
        private Long wellnessInfoId;

        @Min(value = 0, message = "평점은 0이상의 정수여야 합니다.")
        @Max(value = 5, message = "평점은 5이하의 정수여야 합니다.")
        private Integer rating;

        private String review;
    }

}
