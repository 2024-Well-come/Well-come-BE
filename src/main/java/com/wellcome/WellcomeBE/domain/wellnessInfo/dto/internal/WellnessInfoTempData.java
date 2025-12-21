package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WellnessInfoTempData {
    private final String contentId;
    private final String thumbnailUrl;
    private final String parentId;
}
