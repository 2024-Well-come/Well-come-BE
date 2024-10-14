package com.wellcome.WellcomeBE.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DayOfWeekType {
    SUNDAY("SUNDAY", "일요일", 0),
    MONDAY("MONDAY", "월요일", 1),
    TUESDAY("TUESDAY", "화요일", 2),
    WEDNESDAY("WEDNESDAY", "수요일", 3),
    THURSDAY("THURSDAY", "목요일", 4),
    FRIDAY("FRIDAY", "금요일", 5),
    SATURDAY("SATURDAY", "토요일", 6);

    private final String englishName;
    private final String koreanName;
    private final int index;

    public static DayOfWeekType fromIndex(int index) {
        for (DayOfWeekType day : values()) {
            if (day.getIndex() == index) {
                return day;
            }
        }
        throw new IllegalArgumentException("Invalid day index: " + index);
    }
}