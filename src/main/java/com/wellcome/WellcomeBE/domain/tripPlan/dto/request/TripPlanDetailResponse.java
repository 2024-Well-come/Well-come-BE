package com.wellcome.WellcomeBE.domain.tripPlan.dto.request;

import com.wellcome.WellcomeBE.domain.tripPlan.TripPlan;
import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 여행 폴더 상세 조회 Response DTO
 */
@Getter
@Builder
public class TripPlanDetailResponse {
    private List<String> themaList;
    private String tripStartDate;
    private String tripEndDate;
    private String folderName;
    private SavedWellnessInfoList wellnessInfoList;

    public static TripPlanDetailResponse from(
            List<String> themaList,
            TripPlan tripPlan,
            SavedWellnessInfoList wellnessInfoList
    ){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return TripPlanDetailResponse.builder()
                .themaList(themaList)
                .tripStartDate(tripPlan.getStartDate().format(formatter))
                .tripEndDate(tripPlan.getEndDate().format(formatter))
                .folderName(tripPlan.getTitle())
                .wellnessInfoList(wellnessInfoList)
                .build();
    }

    @Getter
    @Builder
    public static class SavedWellnessInfoList {
        private long totalCount;
        private int pageNum;
        private boolean hasPrevious;
        private boolean hasNext;
        private List<SavedWellnessInfoItem> data;

        public static SavedWellnessInfoList from(
                long totalCount, int pageNum,
                boolean hasPrevious, boolean hasNext,
                List<SavedWellnessInfoItem> data
        ){
            return SavedWellnessInfoList.builder()
                    .totalCount(totalCount)
                    .pageNum(pageNum)
                    .hasPrevious(hasPrevious)
                    .hasNext(hasNext)
                    .data(data)
                    .build();
        }

        @Getter
        @Builder
        public static class SavedWellnessInfoItem {
            private long wellnessInfoId;
            private String thumbnailUrl;
            private String title;
            private Double rating;
            private Boolean isOpen;
            private String thema;
            private String address;
            private Double mapX;
            private Double mapY;

            public static SavedWellnessInfoItem from (WellnessInfo wellnessInfo){
                return SavedWellnessInfoItem.builder()
                        .wellnessInfoId(wellnessInfo.getId())
                        .title(wellnessInfo.getTitle())
                        .rating((double) 0) // TODO 구글 리뷰
                        .isOpen(false) // TODO 구글 영업시간
                        .thema(wellnessInfo.getThema().getName())
                        .address(wellnessInfo.getAddress())
                        .mapX(wellnessInfo.getMapX())
                        .mapY(wellnessInfo.getMapY())
                        .build();
            }
        }
    }
}
