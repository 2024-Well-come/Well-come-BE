package com.wellcome.WellcomeBE.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.IMG_UPLOAD_ERROR;
import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Custom Exception
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest request) {
        log.error("*** Custom Exception - url: {} ({}), httpStatus: {}, errorCode: {}, errorMessage: {}",
                request.getRequestURL(), request.getMethod(), e.getCustomErrorCode().getHttpStatus(), e.getCustomErrorCode().getCode(), e.getMessage());
        return buildResponseEntity(e.getCustomErrorCode(), e.getMessage());
    }

    // @Valid Exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("*** Validation Exception - url: {} ({}), errorMessage: {}",
                request.getRequestURL(), request.getMethod(), e.getMessage());

        // 유효성 검증 실패한 필드 리스트
        List<FieldError> fieldErrorList = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrorList.stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildResponseEntity(CustomErrorCode.INVALID_VALUE, errorMessage);
    }

    // MultipartFile 파일 크기 제한 초과 Exception
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.error("*** MaxUploadSizeExceeded Exception - url: {} ({}), errorMessage: {}",
                request.getRequestURL(), request.getMethod(), e.getMessage());
        return buildResponseEntity(IMG_UPLOAD_ERROR, "파일 크기가 허용된 최대 용량을 초과했습니다.");
    }

    // 기타 Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception e, HttpServletRequest request) {
        log.error("*** Common Exception - url: {} ({}), errorMessage: {}",
                request.getRequestURL(), request.getMethod(), e.getMessage());
        return buildResponseEntity(INTERNAL_SERVER_ERROR);
    }

    // CompletionException / ExecutionException
    @ExceptionHandler({CompletionException.class, ExecutionException.class})
    public ResponseEntity<ErrorResponse> handleAsyncException(Throwable ex, HttpServletRequest request) {
        // 래핑된 예외 벗기기
        Throwable actual = unwrap(ex);

        // Custom Exception 처리
        if (actual instanceof CustomException ce) {
            return handleCustomException(ce, request);
        }

        // 그 외 예외는 공통 서버 에러로 처리
        return handleAllException(new Exception(actual), request);
    }


    private Throwable unwrap(Throwable t) {
        while (t instanceof CompletionException || t instanceof ExecutionException) {
            if (t.getCause() == null) break;
            t = t.getCause();
        }
        return t;
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(CustomErrorCode customErrorCode, String errorMessage) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(customErrorCode.getCode())
                .message(errorMessage)
                .build();

        return ResponseEntity
                .status(customErrorCode.getHttpStatus())
                .body(errorResponse);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(CustomErrorCode customErrorCode) {
        return buildResponseEntity(customErrorCode, customErrorCode.getMessage());
    }

}
