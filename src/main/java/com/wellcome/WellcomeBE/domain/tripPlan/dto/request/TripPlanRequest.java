package com.wellcome.WellcomeBE.domain.tripPlan.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TripPlanRequest {
    private String name;
    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate tripStartDate;

    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate tripEndDate;
}
