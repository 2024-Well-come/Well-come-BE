package com.wellcome.WellcomeBE.domain.tripPlan.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TripPlanCreateRequest {
    @NotBlank(message = "여행 계획의 이름은 필수입니다.")
    private String name;

    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate tripStartDate;

    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate tripEndDate;
}
