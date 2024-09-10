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

    GANGNEUNG("강릉시", 1),
    GOSEONG("고성군", 2),
    DONGHAE("동해시", 3),
    SAMCHEOK("삼척시", 4),
    SOKCHO("속초시", 5),
    YANGGU("양구군", 6),
    YANGYANG("양양군", 7),
    YEONGWOL("영월군", 8),
    WONJU("원주시", 9),
    INJE("인제군", 10),
    JEONGSEON("정선군", 11),
    CHEORWON("철원군", 12),
    CHUNCHEON("춘천시", 13),
    TAEBAEK("태백시", 14),
    PYEONGCHANG("평창군", 15),
    HONGCHEON("홍천군", 16),
    HWACHEON("화천군", 17),
    HOENGSEONG("횡성군", 18);

    private final String name;
    private final int code;

    public static Sigungu getSigunguType(int code){
        return Arrays.stream(Sigungu.values())
                .filter(sigungu -> sigungu.getCode() == code)
                .findFirst()
                .orElseThrow(); // 예외 처리 필요
    }


}
