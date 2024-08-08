package com.wellcome.WellcomeBE.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {

    /// TODO Custom ErrorCode 를 추가해 주세요
    // Common (1xxx)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1001, "서버 내부에 오류가 있습니다."),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, 1002, "잘못된 입력값입니다."),

    // 한국관광공사 API (2xxx)


    // 구글맵 API (3xxx)


    // Member (4xxx)


    // WellnessInfo (5xxx)


    // TripPlan (6xxx)


    // TripPlanPlace (7xxx)


    // Community (8xxx)

    ;

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

}
