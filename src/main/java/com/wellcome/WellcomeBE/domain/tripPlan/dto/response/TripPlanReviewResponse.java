package com.wellcome.WellcomeBE.domain.tripPlan.dto.response;

import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class TripPlanReviewResponse {
    private long totalCount;
    private int pageNum;
    private boolean hasPrevious;
    private boolean hasNext;
    private List<TripPlanReviewItem> data;

    public static TripPlanReviewResponse from(
            long totalCount, int pageNum, boolean hasPrevious, boolean hasNext,List<TripPlanReviewItem> data
    ){
        return TripPlanReviewResponse.builder()
                .totalCount(totalCount)
                .pageNum(pageNum)
                .hasPrevious(hasPrevious)
                .hasNext(hasNext)
                .data(data)
                .build();
    }

    @Getter
    @Builder
    public static class TripPlanReviewItem {
        // 식별자
        private Long planId;
        // 사진 정보
        private String representativeImage;
        // 시작시간
        // 종료시간
        private String tripStartDate;
        private String tripEndDate;
        // 폴더명
        private String name;
        // 저장된 장소 수
        private int savedNum;

        // 활성화 여부
        private boolean isActive;

        public static TripPlanReviewItem from(TripPlan tripPlan,int savedNum) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            return TripPlanReviewItem.builder()
                    .planId(tripPlan.getId())
                    .representativeImage(tripPlan.getTripPlanPlaces().isEmpty() ? null : tripPlan.getTripPlanPlaces().get(0).getWellnessInfo().getThumbnailUrl())
                    .tripStartDate(tripPlan.getStartDate() != null ? tripPlan.getStartDate().format(formatter) : null)
                    .tripEndDate(tripPlan.getEndDate() != null ? tripPlan.getEndDate().format(formatter) : null)
                    .name(tripPlan.getTitle())
                    .savedNum(savedNum)
                    .isActive(tripPlan.getIsActive())
                    .build();
        }
    }

}
