package com.example.miniproject.controller;

import com.example.miniproject.dto.request.CartItemRequest;
import com.example.miniproject.dto.response.CartResponse;
import com.example.miniproject.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Cart Controller – UC-04 (Quản lý giỏ hàng)
 * Endpoint: /api/v1/customer/cart/**
 * Quyền: ROLE_CUSTOMER
 */
@RestController
@RequestMapping("/api/v1/customer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** GET /api/v1/customer/cart – Xem giỏ hàng */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    /** POST /api/v1/customer/cart – Thêm sản phẩm vào giỏ */
    @PostMapping
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userDetails.getUsername(), request));
    }

    /** PUT /api/v1/customer/cart/{itemId} – Sửa số lượng */
    @PutMapping("/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(userDetails.getUsername(), itemId, request));
    }

    /** DELETE /api/v1/customer/cart/{itemId} – Xóa item khỏi giỏ */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        cartService.removeItem(userDetails.getUsername(), itemId);
        return ResponseEntity.noContent().build();
    }
}
