package com.wellcome.WellcomeBE.domain.like.dto.response;

import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.vo.LikeWellnessInfoVo;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class LikedResponse {

    @Getter
    @Builder
    public static class WellnessInfoList{
        private Long wellnessInfoId;
        private String thumbnailUrl;
        private String title;
        private Double rating;
        private int reviewNum;
        private String thema;
        private String address;

        public static WellnessInfoList from(LikeWellnessInfoVo vo, PlaceReviewResponse.PlaceResult placeDetails) {
            return WellnessInfoList.builder()
                    .wellnessInfoId(vo.getWellnessInfoId())
                    .thumbnailUrl(vo.getThumbnailUrl())
                    .title(vo.getTitle())
                    .rating(placeDetails.getRating())
                    .reviewNum(placeDetails.getUser_ratings_total())
                    .thema(vo.getThema().getName())
                    .address(vo.getAddress())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class LikedList{
        private List<Thema> themaList;
        private List<WellnessInfoList> wellnessInfoList;
    }
}
