package com.wellcome.WellcomeBE.global.exception;

import lombok.Getter;
@Getter
public class CustomException extends RuntimeException {
    private final CustomErrorCode customErrorCode;

    // 1. 기본 생성자 (오류 코드만)
    public CustomException(CustomErrorCode customErrorCode){
        super(customErrorCode.getMessage());
        this.customErrorCode = customErrorCode;
    }

    // 2. 메시지를 사용자 정의하는 생성자
    public CustomException(CustomErrorCode customErrorCode, String message){
        super(message);
        this.customErrorCode = customErrorCode;
    }

    // 3. 하위 예외(cause)를 포함하는 생성자
    public CustomException(CustomErrorCode customErrorCode, Throwable cause) {
        super(customErrorCode.getMessage(), cause);
        this.customErrorCode = customErrorCode;
    }

    // 4. 하위 예외와 사용자 정의 메시지를 포함하는 생성자
    public CustomException(CustomErrorCode customErrorCode, String message, Throwable cause) {
        super(message, cause);
        this.customErrorCode = customErrorCode;
    }

}