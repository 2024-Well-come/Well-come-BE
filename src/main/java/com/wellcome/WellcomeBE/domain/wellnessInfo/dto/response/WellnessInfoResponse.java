package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request.WellnessInfoListRequest;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WellnessInfoResponse {

    private List<Thema> themaList;
    private List<Sigungu> sigunguList;
    private String tripStartDate;
    private String tripEndDate;
    private WellnessInfoList wellnessInfoList;

    public static WellnessInfoResponse from (
            List<Thema> themaList, List<Sigungu> sigunguList,
            //String tripStartDate, String tripEndDate,
            WellnessInfoListRequest request,
            WellnessInfoList wellnessInfoList
    ){
        return WellnessInfoResponse.builder()
                .themaList(themaList)
                .sigunguList(sigunguList)
                .tripStartDate(request.getTripStartDate())
                .tripEndDate(request.getTripEndDate())
                .wellnessInfoList(wellnessInfoList)
                .build();
    }

    @Getter
    @Builder
    public static class WellnessInfoList {

        private long totalCount;
        private int pageNum;
        private boolean hasPrevious;
        private boolean hasNext;
        private List<WellnessInfoItem> data;

        public static WellnessInfoList from (
                long totalCount, int pageNum,
                boolean hasPrevious, boolean hasNext,
                List<WellnessInfoItem> data
        ){
            return WellnessInfoList.builder()
                    .totalCount(totalCount)
                    .pageNum(pageNum)
                    .hasPrevious(hasPrevious)
                    .hasNext(hasNext)
                    .data(data)
                    .build();
        }

        @Getter
        @Builder
        public static class WellnessInfoItem {

            private long wellnessInfoId;
            private String thumbnailUrl;
            private Boolean isLiked;
            private String thema;
            private String title;
            private String category;
            private String address;
            private Double mapX;
            private Double mapY;

            public static WellnessInfoItem from (
                    WellnessInfo wellnessInfo, boolean isLiked
            ){
                return WellnessInfoItem.builder()
                        .wellnessInfoId(wellnessInfo.getId())
                        .thumbnailUrl(wellnessInfo.getThumbnailUrl())
                        .isLiked(isLiked)
                        .thema(wellnessInfo.getThema().getName())
                        .title(wellnessInfo.getTitle())
                        .category(wellnessInfo.getCategory().getName())
                        .address(wellnessInfo.getAddress())
                        .mapX(wellnessInfo.getMapX())
                        .mapY(wellnessInfo.getMapY())
                        .build();
            }
        }
    }

}
