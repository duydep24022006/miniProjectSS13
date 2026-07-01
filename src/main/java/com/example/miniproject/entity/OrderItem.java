package com.example.miniproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FetchType.LAZY – theo SRS ERD
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // FetchType.LAZY – PRODUCTS referenced (LAZY Fetch) theo SRS ERD
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // quantity > 0 theo SRS ERD
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Snapshot giá tại thời điểm đặt hàng
    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
}
