package com.wellcome.WellcomeBE.global.exception;

public class GoogleApiException extends RuntimeException {
    private final String status;
    private final String errorMessage;

    public GoogleApiException(String status, String errorMessage) {
        super("Google API Error - status: " + status + ", message: " + errorMessage);
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return errorMessage != null ? errorMessage : super.getMessage();
    }
}