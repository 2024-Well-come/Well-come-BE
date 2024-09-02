package com.wellcome.WellcomeBE.global.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
public class ErrorResponse {

    private final int errorCode;
    private final String message;

}
