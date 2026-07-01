package com.example.miniproject.config;

import com.example.miniproject.entity.*;
import com.example.miniproject.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Khởi tạo dữ liệu mẫu cho hệ thống Smart E-Shop – phục vụ phát triển & test.
 * Sinh 3 mẫu dữ liệu cho mỗi Entity (Roles, Users, Products, Carts, Orders).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("[TechNova] Khoi tao du lieu mau he thong...");

        // 1. Sinh các Roles mặc định (nếu chưa có)
        Role roleCustomer = getOrCreateRole(Role.RoleName.ROLE_CUSTOMER);
        Role roleStaff = getOrCreateRole(Role.RoleName.ROLE_STAFF);
        Role roleManager = getOrCreateRole(Role.RoleName.ROLE_MANAGER);

        // 2. Sinh 3 Users mẫu đại diện cho 3 phân quyền (NFR-04: Mật khẩu mã hóa BCrypt)
        User customerUser = getOrCreateUser("customer", "123456", "customer@technova.com", "0912345678", roleCustomer);
        User staffUser = getOrCreateUser("staff", "123456", "staff@technova.com", "0987654321", roleStaff);
        User managerUser = getOrCreateUser("manager", "123456", "manager@technova.com", "0900000000", roleManager);

        // 3. Sinh 3 Products mẫu khác nhau
        Product product1 = getOrCreateProduct("iPhone 15 Pro Max 256GB", "Điện thoại Apple cao cấp nhất", new BigDecimal("32990000.00"), 50);
        Product product2 = getOrCreateProduct("MacBook Pro 14 M3", "Laptop Apple chuyên nghiệp hiệu năng cao", new BigDecimal("44990000.00"), 15);
        Product product3 = getOrCreateProduct("AirPods Pro 2 USB-C", "Tai nghe chống ồn chủ động tốt nhất", new BigDecimal("5590000.00"), 100);

        // 4. Sinh 3 Carts mẫu (gắn với 3 Users)
        getOrCreateCartWithItems(customerUser, product1, 2, product3, 1);
        getOrCreateCartWithItems(staffUser, product2, 1, null, 0); // Staff cũng có giỏ
        getOrCreateCartWithItems(managerUser, product3, 5, product1, 1); // Manager cũng có giỏ

        // 5. Sinh 3 Orders mẫu khác nhau (PENDING, PAID, CANCELLED) để test Dashboard (UC-07)
        createSampleOrderIfEmpty(customerUser, product1, 1, Order.OrderStatus.PAID, new BigDecimal("32990000.00"));
        createSampleOrderIfEmpty(customerUser, product2, 1, Order.OrderStatus.PENDING, new BigDecimal("44990000.00"));
        createSampleOrderIfEmpty(customerUser, product3, 2, Order.OrderStatus.CANCELLED, new BigDecimal("11180000.00"));

        log.info("Du lieu mau da khoi tao thanh cong!");
        log.info("----------------------------------------------------------------");
        log.info("TAI KHOAN MAU DE TEST REST API (Mat khau chung: 123456):");
        log.info("CUSTOMER: username: 'customer' (Quyen mua sam, gio hang, dat hang)");
        log.info("STAFF   : username: 'staff' (Quyen them/sua/xoa mem san pham)");
        log.info("MANAGER : username: 'manager' (Quyen xem doanh thu dashboard, cap quyen)");
        log.info("----------------------------------------------------------------");
    }

    private Role getOrCreateRole(Role.RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private User getOrCreateUser(String username, String rawPassword, String email, String phone, Role role) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(rawPassword)); // BCrypt encode
                    user.setEmail(email);
                    user.setPhone(phone);
                    user.setRoles(Set.of(role));
                    return userRepository.save(user);
                });
    }

    private Product getOrCreateProduct(String name, String desc, BigDecimal price, int quantity) {
        return productRepository.searchByName(name, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream().findFirst()
                .orElseGet(() -> {
                    Product product = new Product();
                    product.setName(name);
                    product.setDescription(desc);
                    product.setPrice(price);
                    product.setStockQuantity(quantity);
                    return productRepository.save(product);
                });
    }

    private void getOrCreateCartWithItems(User user, Product p1, int q1, Product p2, int q2) {
        cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);

            List<CartItem> items = new ArrayList<>();
            if (p1 != null) {
                CartItem item1 = new CartItem();
                item1.setCart(cart);
                item1.setProduct(p1);
                item1.setQuantity(q1);
                items.add(item1);
            }
            if (p2 != null) {
                CartItem item2 = new CartItem();
                item2.setCart(cart);
                item2.setProduct(p2);
                item2.setQuantity(q2);
                items.add(item2);
            }
            cart.setCartItems(items);
            return cartRepository.save(cart);
        });
    }

    private void createSampleOrderIfEmpty(User user, Product product, int quantity, Order.OrderStatus status, BigDecimal totalPrice) {
        if (orderRepository.findByUserId(user.getId()).stream().noneMatch(o -> o.getStatus() == status)) {
            Order order = new Order();
            order.setUser(user);
            order.setStatus(status);
            order.setTotalPrice(totalPrice);
            order.setCreatedAt(LocalDateTime.now().minusDays((long) (Math.random() * 10)));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPrice(product.getPrice());

            order.setOrderItems(List.of(item));
            orderRepository.save(order);
        }
    }
}
