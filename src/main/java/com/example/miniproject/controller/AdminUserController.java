package com.example.miniproject.controller;

import com.example.miniproject.dto.request.UpdateRoleRequest;
import com.example.miniproject.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin User Controller – UC-08 (Cấp quyền nhân viên)
 * Endpoint: PUT /api/v1/admin/users/**
 * Quyền: ROLE_MANAGER only
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminService adminService;

    /**
     * PUT /api/v1/admin/users/{userId}/role
     * Cấp/đổi quyền (Role) của nhân viên
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long userId,
                                                  @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(adminService.updateUserRole(userId, request.getRoleName()));
    }
}
