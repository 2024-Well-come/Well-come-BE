package com.wellcome.WellcomeBE.domain.tripPlan.dto.request;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TripPlanRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
