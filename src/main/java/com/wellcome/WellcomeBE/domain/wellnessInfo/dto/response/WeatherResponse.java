package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@Builder
public class WeatherResponse {

    private String temperature; // 기온
    private String skyState; // 하늘 상태
    private String precipitation; // 강수 형태
    private String hourlyPrecipitation; // 1시간 강수량

    public static WeatherResponse from (
            Map<String, String> values
    ){
        return WeatherResponse.builder()
                .temperature(values.get("T1H"))
                .skyState(SkyStateType.getNameByCode(values.get("SKY")))
                .precipitation(PrecipitationType.getNameByCode(values.get("PTY")))
                .hourlyPrecipitation(values.get("RN1"))
                .build();
    }

    // 하늘 상태
    @Getter
    @RequiredArgsConstructor
    public enum SkyStateType {
        CLEAR("맑음", "1"),
        CLOUDY("구름 많음", "3"),
        OVERCAST("흐림", "4");

        private final String name;
        private final String code;

        public static String getNameByCode(String code) {
            for (SkyStateType type : SkyStateType.values()) {
                if (type.getCode().equals(code)) {
                    return type.getName();
                }
            }
            return null;
        }
    }

    // 강수 형태
    @Getter
    @RequiredArgsConstructor
    public enum PrecipitationType {
        NONE("강수없음", "0"),
        RAIN("비", "1"),
        RAIN_SNOW("비/눈", "2"),
        SNOW("눈", "3"),
        DRIZZLE("빗방울", "5"),
        DRIZZLE_SNOW("빗방울눈날림", "6"),
        SNOW_FLURRIES("눈날림", "7");

        private final String name;
        private final String code;

        public static String getNameByCode(String code) {
            for (PrecipitationType type : PrecipitationType.values()) {
                if (type.getCode().equals(code)) {
                    return type.getName();
                }
            }
            return null;
        }
    }
}
