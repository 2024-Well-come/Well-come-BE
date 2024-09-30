package com.wellcome.WellcomeBE.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 강원도 시군구 코드
 */
@Getter
@RequiredArgsConstructor
public enum Sigungu {

    GANGNEUNG("강릉시", 1, 92, 131),
    GOSEONG("고성군", 2, 85, 145),
    DONGHAE("동해시", 3, 97, 127),
    SAMCHEOK("삼척시", 4, 98, 125),
    SOKCHO("속초시", 5, 87, 141),
    YANGGU("양구군", 6, 77, 139),
    YANGYANG("양양군",7, 88, 138),
    YEONGWOL("영월군", 8, 86, 119),
    WONJU("원주시", 9, 76, 122),
    INJE("인제군", 10, 80, 138),
    JEONGSEON("정선군", 11, 89, 123),
    CHEORWON("철원군", 12, 65, 139),
    CHUNCHEON("춘천시", 13, 73, 134),
    TAEBAEK("태백시", 14, 95, 119),
    PYEONGCHANG("평창군", 15, 84, 123),
    HONGCHEON("홍천군", 16, 75, 130),
    HWACHEON("화천군", 17, 72, 139),
    HOENGSEONG("횡성군", 18, 77, 125);

    private final String name;
    private final int code;

    private final int nx; // 날씨 예보지점 X좌표
    private final int ny; // 날씨 예보지점 Y좌표

    public static Sigungu getSigunguType(int code){
        return Arrays.stream(Sigungu.values())
                .filter(sigungu -> sigungu.getCode() == code)
                .findFirst()
                .orElseThrow(); // 예외 처리 필요
    }

}
