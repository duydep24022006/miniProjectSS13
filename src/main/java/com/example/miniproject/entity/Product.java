package com.example.miniproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // price > 0, precision 19 scale 2 theo SRS ERD
    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    // stock_quantity >= 0 theo SRS ERD
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    // Soft delete – xóa mềm theo SRS UC-06
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
