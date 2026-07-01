package com.example.miniproject.repository;

import com.example.miniproject.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Kiểm tra item đã có trong giỏ hay chưa (để update số lượng thay vì tạo trùng)
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
