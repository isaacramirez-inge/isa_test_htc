package com.isa.transaction.frontend.dto;

public class ApiResponse<T> {
    
    private T data;
    private String message;
    private String code;
    
    // Constructors
    public ApiResponse() {
    }
    
    public ApiResponse(T data, String message, String code) {
        this.data = data;
        this.message = message;
        this.code = code;
    }
    
    // Static factory methods for common responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, "Operation completed successfully", "SUCCESS");
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, "SUCCESS");
    }
    
    public static <T> ApiResponse<T> success(T data, String message, String code) {
        return new ApiResponse<>(data, message, code);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, message, "ERROR");
    }
    
    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(null, message, code);
    }
    
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(null, message, "BAD_REQUEST");
    }
    
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(null, message, "NOT_FOUND");
    }
    
    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(null, message, "INTERNAL_ERROR");
    }
    
    public static <T> ApiResponse<T> validationError(String message) {
        return new ApiResponse<>(null, message, "VALIDATION_ERROR");
    }
    
    public static <T> ApiResponse<T> accepted(T data, String message) {
        return new ApiResponse<>(data, message, "ACCEPTED");
    }
    
    public static <T> ApiResponse<T> processing(T data, String message) {
        return new ApiResponse<>(data, message, "PROCESSING");
    }
    
    // Getters and setters
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    // Helper methods
    public boolean isSuccess() {
        return "SUCCESS".equals(code) || "ACCEPTED".equals(code) || "PROCESSING".equals(code);
    }
    
    public boolean isError() {
        return !isSuccess();
    }
    
    @Override
    public String toString() {
        return "ApiResponse{"
                + "data=" + data
                + ", message='" + message + "'"
                + ", code='" + code + "'"
                + "}";
    }
}
