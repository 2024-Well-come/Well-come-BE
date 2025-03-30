package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class WellnessInfoListRequest {

    private List<Thema> themaList;
    private List<Sigungu> sigunguList;

    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate tripStartDate;

    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate tripEndDate;

    @Builder
    private WellnessInfoListRequest(
            List<Thema> themaList, List<Sigungu> sigunguList,
            LocalDate tripStartDate, LocalDate tripEndDate
    ){
        this.themaList = themaList;
        this.sigunguList = sigunguList;
        this.tripStartDate = tripStartDate;
        this.tripEndDate = tripEndDate;
    }

    public static WellnessInfoListRequest create(
            List<Thema> themaList, List<Sigungu> sigunguList,
            LocalDate tripStartDate, LocalDate tripEndDate
    ){
        return WellnessInfoListRequest.builder()
                .themaList(themaList)
                .sigunguList(sigunguList)
                .tripStartDate(tripStartDate)
                .tripEndDate(tripEndDate)
                .build();
    }

}
