package com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WeatherResponse.PrecipitationType.*;
import static com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WeatherResponse.SkyStateType.CLEAR;
import static com.wellcome.WellcomeBE.domain.wellnessInfo.dto.response.WeatherResponse.SkyStateType.OVERCAST;

@Getter
@Builder
public class WeatherResponse {

    private String temperature; // 기온
    private String state; // 날씨 상태값
    //private String skyState; // 하늘 상태
    //private String precipitation; // 강수 형태
    //private String hourlyPrecipitation; // 1시간 강수량

    public static WeatherResponse from (
            Map<String, String> values
    ){
        SkyStateType sky = SkyStateType.getTypeByCode(values.get("SKY"));
        PrecipitationType pty = PrecipitationType.getTypeByCode(values.get("PTY"));

        return WeatherResponse.builder()
                .temperature(values.get("T1H"))
                .state(getWeatherState(sky, pty))
                //.skyState(SkyStateType.getNameByCode(values.get("SKY")))
                //.precipitation(PrecipitationType.getNameByCode(values.get("PTY")))
                //.hourlyPrecipitation(values.get("RN1"))
                .build();
    }

    private static String getWeatherState(SkyStateType sky, PrecipitationType pty){

        // SkyStateType이 CLEAR인 경우: 맑음 처리
        if (sky == CLEAR){
            return CLEAR.getName();
        }

        // PrecipitationType이 NONE인 경우: 흐림 처리
        if (pty == NONE) {
            return OVERCAST.getName();
        }

        // 강수 상태에 따른 처리
        switch (pty){
            case RAIN :
            case DRIZZLE:
                return RAIN.getName(); // 비
            case RAIN_SNOW:
            case DRIZZLE_SNOW:
                return RAIN_SNOW.getName(); // 비/눈
            case SNOW:
            case SNOW_FLURRIES:
                return SNOW.getName(); // 눈
        }
        return null;
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

        public static SkyStateType getTypeByCode(String code) {
            return Arrays.stream(SkyStateType.values())
                    .filter(type -> Objects.equals(type.getCode(), code))
                    .findFirst()
                    .orElse(null);
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

        public static PrecipitationType getTypeByCode(String code) {
            return Arrays.stream(PrecipitationType.values())
                    .filter(type -> type.getCode().equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }
}
