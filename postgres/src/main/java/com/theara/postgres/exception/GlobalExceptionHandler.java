package com.theara.postgres.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theara.postgres.model.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<?>> handleCustomException(CustomException ex) {
        return new ResponseEntity<>(
                new BaseResponse<>(ex.getStatusCode(), ex.getErrorMessage(), null),
                HttpStatus.valueOf(ex.getStatusCode())
        );
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<BaseResponse<?>> handleHttpClientErrorException(HttpClientErrorException ex) {
        String errorMessage = "";
        try {

            String responseBody = ex.getResponseBodyAsString();
            if(!responseBody.isEmpty()){
                Map<String, Object> errorDetails = new ObjectMapper().readValue(responseBody, Map.class);
                errorMessage = (String) errorDetails.getOrDefault("errorMessage", errorMessage);
            }else{
                if(ex.getStatusCode()==HttpStatus.UNAUTHORIZED){
                    errorMessage="Please check Username and Password";
                }
            }

        } catch (Exception e) {
            e.getStackTrace();
            // Log error parsing response
        }

        HttpStatus statusCode = (HttpStatus) ex.getStatusCode();
        return new ResponseEntity<>(
                new BaseResponse<>(statusCode.value(), errorMessage, null),
                statusCode
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> handleGenericException(Exception ex) {
        return new ResponseEntity<>(
                new BaseResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred. Please try again later.", null),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
