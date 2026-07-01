package com.example.miniproject.controller;

import com.example.miniproject.dto.request.LoginRequest;
import com.example.miniproject.dto.request.UserRegisterDTO;
import com.example.miniproject.dto.response.AuthResponse;
import com.example.miniproject.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public Auth Controller – UC-02 (Đăng ký), UC-03 (Đăng nhập)
 * Endpoint: /api/v1/public/auth/**
 * Quyền: ANONYMOUS (không cần token)
 */
@RestController
@RequestMapping("/api/v1/public/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/public/auth/register
     * Đăng ký tài khoản mới cho Khách hàng
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegisterDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    /**
     * POST /api/v1/public/auth/login
     * Xác thực thông tin, trả về JWT Token (SRS 3.2.1)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
