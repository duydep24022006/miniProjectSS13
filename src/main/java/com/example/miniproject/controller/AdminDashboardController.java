package com.example.miniproject.controller;

import com.example.miniproject.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Dashboard Controller – UC-07 (Xem báo cáo doanh thu)
 * Endpoint: GET /api/v1/admin/dashboard/**
 * Quyền: ROLE_MANAGER only
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminService adminService;

    /**
     * GET /api/v1/admin/dashboard
     * Truy xuất dữ liệu thống kê: tổng doanh thu, số đơn hàng, số sản phẩm
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }
}
