package com.ureka.techpost.global.exception;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        return ErrorResponse.fromException(e);
    }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.builder()
						.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.code("INTERNAL_SERVER_ERROR")
						.message("서버 내부 오류가 발생했습니다.")
						.build());
	}

	@ExceptionHandler(RedisConnectionFailureException.class)
	public ResponseEntity<ErrorResponse> handleRedisConnectionException(RedisConnectionFailureException e) {
		return ResponseEntity
				.status(ErrorCode.REDIS_CONNECTION_FAILURE.getStatus())
				.body(ErrorResponse.builder()
						.status(ErrorCode.REDIS_CONNECTION_FAILURE.getStatus())
						.code(ErrorCode.REDIS_CONNECTION_FAILURE.name())
						.message(ErrorCode.REDIS_CONNECTION_FAILURE.getMessage())
						.build());
	}
}
