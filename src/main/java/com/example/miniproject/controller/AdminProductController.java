package com.example.miniproject.controller;

import com.example.miniproject.dto.request.ProductDTO;
import com.example.miniproject.dto.response.ProductResponse;
import com.example.miniproject.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Product Controller – UC-06 (Thêm/Sửa/Xóa sản phẩm)
 * Endpoint: /api/v1/admin/products/**
 * Quyền: ROLE_STAFF, ROLE_MANAGER
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    /** POST /api/v1/admin/products – Thêm sản phẩm mới */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(dto));
    }

    /** PUT /api/v1/admin/products/{id} – Sửa thông tin sản phẩm */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                          @Valid @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    /** DELETE /api/v1/admin/products/{id} – Xóa mềm sản phẩm */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
