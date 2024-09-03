package com.wellcome.WellcomeBE.domain.tripPlan.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class TripPlanResponse {


    @Getter
    @Builder
    public static class TripPlanListResponse {
        private List<TripPlanPlaceItem> tripPlanList;
    }
    @Getter
    @Builder
    public static class TripPlanPlaceItem {
        private Long planId;

        @JsonFormat(pattern = "yyyy.MM.dd")
        private LocalDate tripStartDate;

        @JsonFormat(pattern = "yyyy.MM.dd")
        private LocalDate tripEndDate;
        private String name;

        public static TripPlanPlaceItem from(
                TripPlan tripPlan) {
            return TripPlanPlaceItem.builder()
                    .planId(tripPlan.getId())
                    .name(tripPlan.getTitle())
                    .tripStartDate(tripPlan.getStartDate())
                    .tripEndDate(tripPlan.getEndDate())
                    .build();
        }
    }
}