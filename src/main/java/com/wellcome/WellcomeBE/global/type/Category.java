package com.wellcome.WellcomeBE.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Category {

    TOURIST_SPOT("관광지", 12),
    CULTURAL_FACILITY("문화시설", 14),
    EVENT("행사/공연/축제", 15),
    TRAVEL_ROUTE("여행코스", 25),
    LEISURE_SPORTS("레포츠", 28),
    ACCOMMODATION("숙박", 32),
    SHOPPING("쇼핑", 38),
    RESTAURANT("음식점", 39);

    private final String name;
    private final int code;

    public static Category getCategoryType(int code){
        return Arrays.stream(Category.values())
                .filter(category -> category.getCode() == code)
                .findFirst()
                .orElseThrow(); // 예외 처리 필요
    }

}
