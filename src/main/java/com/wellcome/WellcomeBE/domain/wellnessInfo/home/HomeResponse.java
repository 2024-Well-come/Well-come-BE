package com.wellcome.WellcomeBE.domain.wellnessInfo.home;

import com.wellcome.WellcomeBE.domain.wellnessInfo.WellnessInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HomeResponse {

    private List<RandomWellnessInfo> homeWellnessInfoList;

    public static HomeResponse from(List<RandomWellnessInfo> homeWellnessInfoList){
        return HomeResponse.builder()
                .homeWellnessInfoList(homeWellnessInfoList)
                .build();
    }

    @Getter
    @Builder
    public static class RandomWellnessInfo {
        private Long wellnessInfoId;
        private String thumbnailUrl;
        private String title;
        private String summary;
        private String thema;
        private String sigungu;

        public static RandomWellnessInfo from(WellnessInfo wellnessInfo){
            return RandomWellnessInfo.builder()
                    .wellnessInfoId(wellnessInfo.getId())
                    .thumbnailUrl(wellnessInfo.getThumbnailUrl())
                    .title(wellnessInfo.getTitle())
                    .summary(wellnessInfo.getSummary())
                    .thema(wellnessInfo.getThema().getName())
                    .sigungu(wellnessInfo.getSigungu().getName())
                    .build();
        }
    }

}
