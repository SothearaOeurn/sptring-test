package com.theara.postgres.exception;

public class CustomException extends RuntimeException {
    private final int statusCode;
    private final String errorMessage;

    public CustomException(int statusCode, String errorMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
