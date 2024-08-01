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

    GANGNEUNG("강릉", 1),
    GOSEONG("고성", 2),
    DONGHAE("동해", 3),
    SAMCHEOK("삼척", 4),
    SOKCHO("속초", 5),
    YANGGU("양구", 6),
    YANGYANG("양양", 7),
    YEONGWOL("영월", 8),
    WONJU("원주", 9),
    INJE("인제", 10),
    JEONGSEON("정선", 11),
    CHEORWON("철원", 12),
    CHUNCHEON("춘천", 13),
    TAEBAEK("태백", 14),
    PYEONGCHANG("평창", 15),
    HONGCHEON("홍천", 16),
    HWACHEON("화천", 17),
    HOENGSEONG("횡성", 18);

    private final String name;
    private final int code;

    public static Sigungu getSigunguType(int code){
        return Arrays.stream(Sigungu.values())
                .filter(sigungu -> sigungu.getCode() == code)
                .findFirst()
                .orElseThrow(); // 예외 처리 필요
    }


}
