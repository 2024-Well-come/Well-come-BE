package com.wellcome.WellcomeBE.global.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Thema {

    FOOD("푸드"),
    STAY("스테이"),
    BEAUTY_SPA("뷰티&스파"),
    HEALING_MEDITATION("힐링&명상"),
    NATURE("자연"),
    NONE("없음"); // TODO 테마 모두 분류 후 삭제 예정

    private final String name;

    public static Thema getThemaType(String cat1, String cat2, String cat3){
        switch (cat1){
            case "A01":
                return NATURE;

            case "A02":
                if(cat2.equals("A0201")){
                    return NATURE;
                } else if (cat2.equals("A0202")) {
                    if (cat3.equals("A02020300") || cat3.equals("A02020400")) {
                        return BEAUTY_SPA;
                    }
                    return HEALING_MEDITATION;
                } else if (cat2.equals("A0203") || cat2.equals("A0207") || cat2.equals("A0208")) {
                    return HEALING_MEDITATION;
                }
                break;

            case "B02":
                return STAY;

            case "A05":
                return FOOD;
        }

        return NONE;
    }

    public static List<Thema> getThemaList() {
        return Arrays.stream(Thema.values())
                .filter(thema -> !thema.equals(NONE)) // NONE 제외
                .collect(Collectors.toList());
    }


}
