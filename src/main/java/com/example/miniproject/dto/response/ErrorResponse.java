package com.example.miniproject.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Cấu trúc JSON lỗi chuẩn hóa của TechNova – theo SRS Section 6.3
 * Áp dụng cho tất cả HTTP Status Code lỗi (400, 401, 403, 404, 500)
 */
@Data
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'+00:00'")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
