package com.example.miniproject.repository;

import com.example.miniproject.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Chỉ lấy sản phẩm chưa bị xóa mềm (soft delete)
    Page<Product> findByDeletedFalse(Pageable pageable);

    // Tìm kiếm theo tên, case-insensitive
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

    Optional<Product> findByIdAndDeletedFalse(Long id);
}
