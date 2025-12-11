package com.ureka.techpost.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class ErrorResponse {

    private final HttpStatus status;
    private final String code;
    private final String message;

    public static ResponseEntity<ErrorResponse> fromException(CustomException e) {
        String message = e.getErrorCode().getMessage();
        if (e.getInfo() != null) {
            message += " " + e.getInfo();
        }
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.builder()
                        .status(e.getErrorCode().getStatus())
                        .code(e.getErrorCode().name())
                        .message(message)
                        .build());
    }
}
