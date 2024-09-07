package com.wellcome.WellcomeBE.domain.tripPlan.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 여행 폴더 조회 Response DTO
 */
public class TripPlanResponse {
    // 폴더 메뉴 내부 목록 조회
    @Getter
    @Builder
    public static class TripPlanListResponse {
      private List<TripPlanItem> upcomingTripList;
      private TripPlanListItem tripPlanList;
    }

    @Getter
    @Builder
    public static class TripPlanItem {
        private Long planId;
        private String representativeImage;
        private String tripStartDate;
        private String tripEndDate;
        private String name;
        private int placeNum;

        public static TripPlanItem from(
                TripPlan tripPlan, String representativeImage, int placeNum
        ){
            return TripPlanItem.builder()
                    .planId(tripPlan.getId())
                    .representativeImage(representativeImage)
                    .tripStartDate(tripPlan.getStartDate().toString())
                    .tripEndDate(tripPlan.getStartDate().toString())
                    .name(tripPlan.getTitle())
                    .placeNum(placeNum)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TripPlanListItem {
        private long totalCount;
        private int pageNum;
        private boolean hasPrevious;
        private boolean hasNext;
        private List<TripPlanItem> data;

        public static TripPlanListItem from(
                long totalCount, int pageNum,
                boolean hasPrevious, boolean hasNext,
                List<TripPlanItem> data
        ){
            return TripPlanListItem.builder()
                    .totalCount(totalCount)
                    .pageNum(pageNum)
                    .hasPrevious(hasPrevious)
                    .hasNext(hasNext)
                    .data(data)
                    .build();
        }
    }



    // 폴더명 간단 조회
    @Getter
    @Builder
    public static class TripPlanBriefResponse {
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
                    .tripStartDate(tripPlan.getStartDate() != null ? tripPlan.getStartDate() : null)
                    .tripEndDate(tripPlan.getEndDate() != null ? tripPlan.getEndDate() : null)
                    .build();
        }
    }
}