package com.wellcome.WellcomeBE.global.type;

import lombok.Getter;

import java.util.List;

@Getter
public class CategoryDetail {

    private final String cat1;
    private final String cat2;
    private final String cat3;

    public static final List<CategoryDetail> CATEGORY_PARAM_LIST = List.of(
            // 관광지
            new CategoryDetail("A01", "A0101"), // 자연 - 자연관광지
            new CategoryDetail("A02", "A0201", "A02010400"), // 인문 - 역사관광지 - 고택
            new CategoryDetail("A02", "A0201", "A02010800"), // 인문 - 역사관광지 - 사찰
            new CategoryDetail("A02", "A0202"), // 인문 - 휴양관광지
            new CategoryDetail("A02", "A0203", "A02030100"), // 인문 - 체험관광지 - 농.산.어촌 체험
            new CategoryDetail("A02", "A0203", "A02030200"), // 인문 - 체험관광지 - 전통체험
            new CategoryDetail("A02", "A0203", "A02030300"), // 인문 - 체험관광지 - 산사체험
            // TODO 이색체험, 이색거리 개별 추가 필요
            //new CategoryDetail("A02", "A0203", "A02030400"), // 인문 - 체험관광지 - 이색체험
            //new CategoryDetail("A02", "A0203", "A02030500"), // 인문 - 체험관광지 - 이색거리

            // 축제/공연/행사
            new CategoryDetail("A02", "A0207"), // 축제
            new CategoryDetail("A02", "A0208", "A02080100"), // 공연/행사 - 전통공연

            // 숙박
            new CategoryDetail("B02", "B0201", "B02011200"), // 홈스테이
            new CategoryDetail("B02", "B0201", "B02011600"), // 한옥
            // TODO 웰니스 프로그램 있는 곳 개별 추가 필요

            // 음식점
            new CategoryDetail("A05", "A0502", "A05020100"), // 한식
            new CategoryDetail("A05", "A0502", "A05020900") // 카페/전통찻집
    );

    private CategoryDetail(String cat1, String cat2, String cat3) {
        this.cat1 = cat1;
        this.cat2 = cat2;
        this.cat3 = cat3;
    }

    private CategoryDetail(String cat1, String cat2) {
        this(cat1, cat2, null);
    }

    private CategoryDetail(String cat1) {
        this(cat1, null, null);
    }

}
