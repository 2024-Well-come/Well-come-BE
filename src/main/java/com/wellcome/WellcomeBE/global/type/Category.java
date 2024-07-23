package com.wellcome.WellcomeBE.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {

    TOURIST_SPOT("관광지"),
    CULTURAL_FACILITY("문화시설"),
    EVENT("행사/공연/축제"),
    TRAVEL_ROUTE("여행코스"),
    LEISURE_SPORTS("레포츠"),
    ACCOMMODATION("숙박"),
    SHOPPING("쇼핑"),
    RESTAURANT("음식점");

    private final String name;

}
