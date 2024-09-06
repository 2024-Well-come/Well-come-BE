package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

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

}
