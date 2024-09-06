package com.wellcome.WellcomeBE.domain.wellnessInfo.vo;

import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeWellnessInfoVo {
    private String placeId;
    private Long wellnessInfoId;
    private String thumbnailUrl;
    private String title;
    private Thema thema;
    private String address;
}
