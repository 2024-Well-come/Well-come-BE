package com.wellcome.WellcomeBE.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {

    /// TODO Custom ErrorCode 를 추가해 주세요
    // Common (1xxx)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1001, "서버 내부에 오류가 있습니다."),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, 1002, "잘못된 입력값입니다."),

    // 한국관광공사 API (2xxx)
    TOUR_API_RESPONSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 2001, "한국관광공사 API 호출 오류"),
    TOUR_API_XML_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 2002, "XML 파싱 중 오류가 발생했습니다."),
    TOUR_API_JSON_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 2003, "JSON 파싱 중 오류가 발생했습니다."),
    TOUR_API_IMG_S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 2004, "한국관광공사 API 이미지 S3 업로드 오류가 발생했습니다."),

    // 구글맵 API (3xxx)


    // Member (4xxx)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, 4001, "해당하는 멤버를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, 4002, "해당 요청에 대해 권한이 없습니다."),

    // WellnessInfo (5xxx)
    WELLNESS_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, 5001,"해당하는 웰니스 정보를 찾을 수 없습니다."),

    // TripPlan (6xxx)
    TRIP_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, 6001,"해당하는 여행 폴더를 찾을 수 없습니다."),


    // TripPlanPlace (7xxx)
    TRIP_PLAN_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, 7001,"해당하는 여행지 정보를 찾을 수 없습니다."),
    TRIP_PLAN_PLACE_NOT_IN_FOLDER(HttpStatus.BAD_REQUEST, 7002, "해당 여행지가 요청된 폴더에 속하지 않습니다."),
    TRIP_PLAN_PLACE_DUPLICATION(HttpStatus.BAD_REQUEST,7003, "여행 폴더 내 해당 여행지가 이미 추가되어 있습니다. 중복 불가"),


    // Community (8xxx)
    COMMUNITY_ALREADY_EXISTS(HttpStatus.CONFLICT, 8001, "해당 여행 폴더에 대한 후기 게시글이 이미 존재합니다."),
    IMG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, 8002, "이미지 업로드 제한 개수를 초과했습니다."),
    IMG_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 8003, "이미지 업로드 중 오류가 발생했습니다."),
    COMMUNITY_NOT_FOUND(NOT_FOUND,8004,"커뮤니티 게시글을 찾을 수 없습니다."),


    // Auth (9xxx)
    TOKEN_MISSING(UNAUTHORIZED, 9001, "토큰이 누락되었습니다."),
    REFRESH_TOKEN_EXPIRED(UNAUTHORIZED, 9002, "인증이 만료되었습니다. 다시 로그인하세요."),
    AUTHENTICATION_NOT_FOUND(UNAUTHORIZED, 9003, "인증 정보를 찾을 수 없습니다."),
    KAKAO_LOGIN_CLIENT_ERROR(UNAUTHORIZED, 9004, "카카오 로그인 API 호출 오류 (Client Error)"),

    // Support (10xxx)
    SUPPORT_COMMUNITY_CNT(BAD_REQUEST,100001, "COMMUNITY 타입은 커뮤니티 값이 필요합니다."),
    SUPPORT_TRIP_PLAN_PLACE(BAD_REQUEST,100002,"TRIP_PLAN_PLACE 타입은 ID 리스트가 필요합니다." ),
    SUPPORT_TYPE_MISMATCH(BAD_REQUEST,100003, "유효하지 않은 타입입니다."),
    SUPPORT_NOT_FOUND(NOT_FOUND,100004,"추천 내용을 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

}
