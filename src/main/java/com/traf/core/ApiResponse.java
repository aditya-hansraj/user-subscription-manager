package com.traf.core;

public class ApiResponse<T> {
    private final int status;
    private final T data;
    private final String error;

    private ApiResponse(int status, T data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public int getStatus() { return status; }
    public T getData() { return data; }
    public String getError() { return error; }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data, null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }
}
