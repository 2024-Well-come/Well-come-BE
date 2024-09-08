package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WellnessInfoNearbyList {
    private List<WellnessNearbyDto> nearbyList;

    public static WellnessInfoNearbyList from(List<WellnessNearbyDto> nearbyList){
        return WellnessInfoNearbyList.builder()
                .nearbyList(nearbyList)
                .build();
    }
}
