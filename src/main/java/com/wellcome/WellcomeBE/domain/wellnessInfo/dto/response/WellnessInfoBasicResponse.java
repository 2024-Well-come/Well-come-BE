package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.wellcome.WellcomeBE.domain.review.PlaceReviewResponse;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.global.OpeningHoursUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
public class WellnessInfoBasicResponse {
    private Long wellnessInfoId;
    private String thumbnailUrl;
    private List<String> imgList;
    private String title;
    private String category;
    private String address;
    private Double mapX;
    private Double mapY;
    private Boolean isLiked;
    private Boolean isOpen;
    private String openDetail;
    private String tel;
    private String website;

    public static WellnessInfoBasicResponse from(
            WellnessInfo wellness,
            List<String> wellnessInfoImg,
            PlaceReviewResponse.PlaceResult placeResult,
            boolean isLiked) {
        return WellnessInfoBasicResponse.builder()
                .wellnessInfoId(wellness.getId())
                .thumbnailUrl(wellness.getThumbnailUrl())
                .imgList(wellnessInfoImg)
                .title(wellness.getTitle())
                .category(wellness.getCategory().getName())
                .address(wellness.getAddress())
                .mapX(wellness.getMapX())
                .mapY(wellness.getMapY())
                .isLiked(isLiked)
                .isOpen(placeResult != null ? OpeningHoursUtils.getOpenStatus(placeResult).getIsOpen() : null)
                .openDetail(placeResult != null ? OpeningHoursUtils.getOpenStatus(placeResult).getOpenDetail() : "정보 없음")
                .tel(wellness.getTel())
                .website(placeResult != null ? placeResult.getWebsite() : "")
                .build();
    }

}
