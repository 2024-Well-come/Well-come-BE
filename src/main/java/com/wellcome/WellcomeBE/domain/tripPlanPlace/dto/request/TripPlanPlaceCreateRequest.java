package com.wellcome.WellcomeBE.domain.tripPlanPlace.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TripPlanPlaceCreateRequest {
    @NotNull(message = "웰니스 정보 ID는 필수입니다.")
    private Long wellnessInfoId;
}
