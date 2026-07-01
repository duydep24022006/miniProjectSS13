package com.example.miniproject.repository;

import com.example.miniproject.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * @EntityGraph: Lấy Cart + CartItems + Product của mỗi item
     * trong 1 câu SELECT duy nhất – tránh N+1 Query (NFR-03)
     */
    @EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
    Optional<Cart> findByUserId(Long userId);
}
