package com.buildscheduler.buildscheduler.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    // Convenience static factory method
    public static <T> ApiResponse<T> ofSuccess(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> ofFailure(String message) {
        return new ApiResponse<>(false, message, null);
    }
}