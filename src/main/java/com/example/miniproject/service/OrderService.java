package com.example.miniproject.service;

import com.example.miniproject.dto.response.OrderItemResponse;
import com.example.miniproject.dto.response.OrderResponse;
import com.example.miniproject.entity.*;
import com.example.miniproject.exception.InsufficientStockException;
import com.example.miniproject.exception.ResourceNotFoundException;
import com.example.miniproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Tạo đơn hàng từ giỏ hàng – UC-05 (Xử lý đồng thời / Concurrency)
     *
     * @Transactional đảm bảo:
     * - Nếu bất kỳ sản phẩm nào hết hàng → Rollback TOÀN BỘ (SRS 3.2.2 luồng ngoại lệ)
     * - Không lưu dữ liệu rác xuống DB
     */
    @Transactional
    public OrderResponse createOrder(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // @EntityGraph tải Cart + CartItems + Product trong 1 SELECT (NFR-03)
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống, không thể đặt hàng");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // ===== Kiểm tra tồn kho và trừ số lượng =====
        for (CartItem cartItem : cart.getCartItems()) {
            // Lock sản phẩm để tránh race condition
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Sản phẩm", cartItem.getProduct().getId()));

            // Kiểm tra stock_quantity – SRS 3.2.2 bước 3
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                // Trigger Rollback – SRS 3.2.2 luồng ngoại lệ
                throw new InsufficientStockException(product.getName());
            }

            // Trừ tồn kho: stock_quantity = stock_quantity - ordered_quantity (SRS 3.2.2 bước 4)
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Tạo OrderItem với snapshot giá tại thời điểm đặt
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice()); // Snapshot giá
            orderItems.add(orderItem);

            totalPrice = totalPrice.add(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalPrice(totalPrice);
        order.setOrderItems(orderItems);

        // Lưu đơn hàng – SRS 3.2.2 bước 5
        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt hàng thành công
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    /**
     * Xem lịch sử đơn hàng của người dùng
     * @EntityGraph trong OrderRepository tránh N+1 (NFR-03)
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        return orderRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        response.setOrderItems(order.getOrderItems().stream().map(item -> {
            OrderItemResponse itemResp = new OrderItemResponse();
            itemResp.setId(item.getId());
            itemResp.setProductId(item.getProduct().getId());
            itemResp.setProductName(item.getProduct().getName());
            itemResp.setQuantity(item.getQuantity());
            itemResp.setPrice(item.getPrice());
            return itemResp;
        }).collect(Collectors.toList()));
        return response;
    }
}
