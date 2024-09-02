package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.request;

import com.wellcome.WellcomeBE.global.type.Sigungu;
import com.wellcome.WellcomeBE.global.type.Thema;
import lombok.Getter;

import java.util.List;

@Getter
public class WellnessInfoListRequest {

    private List<Thema> themaList;
    private List<Sigungu> sigunguList;
    private String tripStartDate;
    private String tripEndDate;

}
