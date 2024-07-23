package com.wellcome.WellcomeBE.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Thema {

    FOOD("푸드"),
    STAY("스테이"),
    BEAUTY_SPA("뷰티&스파"),
    HEALING_MEDITATION("힐링&명상"),
    NATURE("자연");

    private final String name;

}
