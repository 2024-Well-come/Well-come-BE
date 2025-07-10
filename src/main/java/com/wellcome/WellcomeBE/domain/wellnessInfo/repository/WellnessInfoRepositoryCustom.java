package com.wellcome.WellcomeBE.domain.wellnessInfo.repository;

import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WellnessInfoNearResponse;

import java.util.List;

public interface WellnessInfoRepositoryCustom {
    List<WellnessInfoNearResponse.WellnessNearbyDto> findNearbyWithinRadius(Double mapX, Double mapY, Long excludeId, Double radiusKm, int limit);
}
