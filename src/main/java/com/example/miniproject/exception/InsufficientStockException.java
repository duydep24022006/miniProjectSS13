package com.example.miniproject.exception;

/**
 * Ném ra khi sản phẩm hết hàng hoặc không đủ số lượng tồn kho
 * Luồng ngoại lệ UC-05 – Trigger Rollback @Transactional
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName) {
        super("Sản phẩm [" + productName + "] đã hết hàng hoặc không đủ số lượng tồn kho");
    }
}
