package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.internal;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import lombok.Getter;

@Getter
public class WellnessInfoWithLiked {
    private WellnessInfo wellnessInfo;
    private boolean liked;

    public WellnessInfoWithLiked(WellnessInfo wellnessInfo, boolean liked) {
        this.wellnessInfo = wellnessInfo;
        this.liked = liked;
    }
}