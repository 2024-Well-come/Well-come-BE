package com.wellcome.WellcomeBE.domain.tripPlan.dto.response;

import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 여행 폴더 조회 Response DTO
 */
public class TripPlanResponse {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

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
                    .tripStartDate(tripPlan.getStartDate() != null ? tripPlan.getStartDate().format(formatter) : null)
                    .tripEndDate(tripPlan.getEndDate() != null ? tripPlan.getEndDate().format(formatter) : null)
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
        private String representativeImage;
        private String tripStartDate;
        private String tripEndDate;
        private String name;
        private int placeNum;

        public static TripPlanPlaceItem from(TripPlan tripPlan) {

            return TripPlanPlaceItem.builder()
                    .planId(tripPlan.getId())
                    .representativeImage(tripPlan.getTripPlanPlaces().isEmpty() ? null : tripPlan.getTripPlanPlaces().get(0).getWellnessInfo().getThumbnailUrl())
                    .tripStartDate(tripPlan.getStartDate() != null ? tripPlan.getStartDate().format(formatter) : null)
                    .tripEndDate(tripPlan.getEndDate() != null ? tripPlan.getEndDate().format(formatter) : null)
                    .name(tripPlan.getTitle())
                    .placeNum(tripPlan.getTripPlanPlaces().size())
                    .build();
        }
    }
}