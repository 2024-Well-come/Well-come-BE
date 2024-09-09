package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
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

}
