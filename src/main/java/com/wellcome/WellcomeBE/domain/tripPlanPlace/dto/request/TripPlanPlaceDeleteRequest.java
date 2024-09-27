package com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class TripPlanPlaceDeleteRequest {

    private static final String message = "삭제할 여행지 식별자 정보가 없습니다.";

    @NotNull(message = message)
    @Size(min = 1, message = message)
    private List<Long> deletePlanPlaceIdList;
}
