package com.wellcome.WellcomeBE.domain.community.dto.response;

import com.wellcome.WellcomeBE.domain.community.Community;
import com.wellcome.WellcomeBE.domain.tripPlanPlace.TripPlanPlace;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;

import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class ReviewPostDetailResponse {
    private String nickname;
    private String createdAt;
    private String title;
    private List<String> postImgs;
    private String content;
    private boolean isSupport;
    private List<ReviewWellnessInfoItem> wellnessInfoHistory;

    public static ReviewPostDetailResponse from(Community community, boolean isSupport ,List<String> postImgs, List<ReviewWellnessInfoItem> reviewWellnessInfoItem){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        return ReviewPostDetailResponse.builder()
                .nickname(community.getMember().getNickname())
                .createdAt(formatter.format(community.getCreatedAt()))
                .title(community.getTitle())
                .postImgs(postImgs)
                .content(community.getContent())
                .isSupport(isSupport)
                .wellnessInfoHistory(reviewWellnessInfoItem)
                .build();
    }


    @Getter
    @Builder
    public static class ReviewWellnessInfoItem{
        private String originalThumbnailUrl;
        private String title;
        private String thema;
        private String address;
        private String review;
        private Integer rating;
        private boolean isSupport;
        private boolean isLiked;


        public static ReviewWellnessInfoItem from(Object[] objects) {
            // objects[0]: WellnessInfo, objects[1]: TripPlanPlace, objects[2]: isLiked, objects[3]: isSupport
            WellnessInfo wellnessInfo = (WellnessInfo) objects[0];
            TripPlanPlace tripPlanPlace = (TripPlanPlace) objects[1]; // TripPlanPlace 엔티티 추출
            boolean isLiked = (boolean) objects[2];
            boolean isSupport = (boolean) objects[3];

            return ReviewWellnessInfoItem.builder()
                    .originalThumbnailUrl(wellnessInfo.getOriginalThumbnailUrl())
                    .title(wellnessInfo.getTitle())
                    .thema(wellnessInfo.getThema().getName())
                    .address(wellnessInfo.getAddress())
                    .review(tripPlanPlace != null ? tripPlanPlace.getReview() : null) // TripPlanPlace에서 리뷰 가져오기
                    .rating(tripPlanPlace != null ? tripPlanPlace.getRating() : null) // TripPlanPlace에서 별점 가져오기
                    .isSupport(isSupport)
                    .isLiked(isLiked)
                    .build();
        }

    }

}
