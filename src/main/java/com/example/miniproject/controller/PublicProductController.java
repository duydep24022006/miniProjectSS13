package com.example.miniproject.controller;

import com.example.miniproject.dto.response.ProductResponse;
import com.example.miniproject.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public Product Controller – UC-01 (Xem & Tìm kiếm sản phẩm)
 * Endpoint: GET /api/v1/public/products/**
 * Quyền: ANONYMOUS, ALL – không cần token
 * Cache: Redis (NFR-02)
 */
@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    /**
     * GET /api/v1/public/products?page=0&size=10&keyword=phone
     * Xem danh sách hoặc tìm kiếm sản phẩm
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (keyword != null && !keyword.isBlank()) {
            return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
        }
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    /**
     * GET /api/v1/public/products/{id}
     * Xem chi tiết sản phẩm
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
