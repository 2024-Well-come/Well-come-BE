package com.wellcome.WellcomeBE.domain.tripPlan.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class TripPlanDeleteRequest {
    private List<Long> deletePlanList;
}
