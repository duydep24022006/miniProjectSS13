package com.example.miniproject.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Response trả về sau khi đăng nhập thành công (UC-03)
 * Chứa JWT Token, username và danh sách Roles
 */
@Data
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private List<String> roles;

    public AuthResponse(String token, String username, List<String> roles) {
        this.token = token;
        this.username = username;
        this.roles = roles;
    }
}
