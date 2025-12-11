package com.ureka.techpost.global.apiPayload;
/**
 * @file ApiReponse.java
 @author 장예찬
 @version 1.0
 @since 2025-12-05
 @description 이 파일은 API 응답을 통일하는 클래스입니다.
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ureka.techpost.global.apiPayload.code.BaseCode;
import com.ureka.techpost.global.apiPayload.code.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("isSuccess")
    private boolean isSuccess;
    private String code;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

     //성공한 경우 응답 생성 -> 200
    public static <T> ApiResponse<T> onSuccess(T result){
        return new ApiResponse<>(true, SuccessStatus._OK.getCode() , SuccessStatus._OK.getMessage(), result);
    }

    // Created 응답 생성 -> 201
    public static <T> ApiResponse<T> onCreated(T result) {
        return of(SuccessStatus._CREATED, null);
    }

    // 커스텀 성공 응답 -> ex) 201 created, 202 Accepted, 204 No content
    public static <T> ApiResponse<T> of(BaseCode code, T result){
            return new ApiResponse<>(true, code.getReasonHttpStatus().getCode() , code.getReasonHttpStatus().getMessage(), result);
    }

    // 실패한 경우 응답 생성
    public static <T> ApiResponse<T> onFailure(String code, String message, T data){
        return new ApiResponse<>(false, code, message, data);
    }
}
