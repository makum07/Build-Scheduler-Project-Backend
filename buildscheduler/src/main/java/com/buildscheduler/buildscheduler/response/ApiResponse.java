package com.buildscheduler.buildscheduler.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ofSuccess(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> ofSuccess(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> ofError(String message) {
        return new ApiResponse<>(false, message, null);
    }
}