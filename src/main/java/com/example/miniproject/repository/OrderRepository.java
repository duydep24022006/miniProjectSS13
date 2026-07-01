package com.example.miniproject.repository;

import com.example.miniproject.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * @EntityGraph: Lấy Order + OrderItems + Product của mỗi item
     * trong 1 câu SELECT duy nhất – tránh N+1 Query (NFR-03)
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findByUserId(Long userId);

    // Dashboard: Tổng doanh thu các đơn hàng đã thanh toán
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'PAID'")
    BigDecimal getTotalRevenue();

    // Dashboard: Số đơn hàng đang chờ xử lý
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'")
    Long countPendingOrders();

    // Dashboard: Tổng số đơn hàng
    @Query("SELECT COUNT(o) FROM Order o")
    Long countAllOrders();
}
