package com.example.miniproject.service;

import com.example.miniproject.dto.request.CartItemRequest;
import com.example.miniproject.dto.response.CartResponse;
import com.example.miniproject.entity.*;
import com.example.miniproject.exception.ResourceNotFoundException;
import com.example.miniproject.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Xem giỏ hàng – @EntityGraph trong CartRepository tránh N+1 (NFR-03)
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(String username) {
        User user = getUser(username);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));
        return toResponse(cart);
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm đã có → cộng thêm số lượng
     */
    @Transactional
    public CartResponse addItem(String username, CartItemRequest request) {
        User user = getUser(username);
        Product product = productRepository.findByIdAndDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", request.getProductId()));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            // Cộng thêm số lượng nếu đã có
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem.get());
        } else {
            // Thêm mới
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
        }

        return toResponse(cartRepository.findByUserId(user.getId()).orElseThrow());
    }

    /**
     * Cập nhật số lượng item trong giỏ
     */
    @Transactional
    public CartResponse updateItem(String username, Long itemId, CartItemRequest request) {
        User user = getUser(username);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item không tồn tại với ID: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item không thuộc về người dùng này");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return toResponse(cartRepository.findByUserId(user.getId()).orElseThrow());
    }

    /**
     * Xóa item khỏi giỏ hàng
     */
    @Transactional
    public void removeItem(String username, Long itemId) {
        User user = getUser(username);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tồn tại"));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item không tồn tại với ID: " + itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item không thuộc về người dùng này");
        }

        cartItemRepository.delete(item);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại: " + username));
    }

    private CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setItems(cart.getCartItems().stream().map(item -> {
            CartResponse.CartItemResponse itemResp = new CartResponse.CartItemResponse();
            itemResp.setItemId(item.getId());
            itemResp.setProductId(item.getProduct().getId());
            itemResp.setProductName(item.getProduct().getName());
            itemResp.setQuantity(item.getQuantity());
            itemResp.setUnitPrice(item.getProduct().getPrice());
            itemResp.setSubtotal(item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemResp;
        }).collect(Collectors.toList()));
        return response;
    }
}
