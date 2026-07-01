package com.example.miniproject.controller;

import com.example.miniproject.dto.response.OrderResponse;
import com.example.miniproject.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Order Controller – UC-05 (Thanh toán & Đặt hàng)
 * Endpoint: /api/v1/customer/orders/**
 * Quyền: ROLE_CUSTOMER
 */
@RestController
@RequestMapping("/api/v1/customer/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/v1/customer/orders – Tạo đơn hàng từ giỏ hàng (UC-05)
     * Trả về 201 Created khi thành công
     * Trả về 400 Bad Request nếu hết hàng (Rollback @Transactional)
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(userDetails.getUsername()));
    }

    /**
     * GET /api/v1/customer/orders – Xem lịch sử đơn hàng
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getUsername()));
    }
}
