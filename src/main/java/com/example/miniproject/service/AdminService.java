package com.example.miniproject.service;

import com.example.miniproject.entity.Role;
import com.example.miniproject.entity.User;
import com.example.miniproject.exception.ResourceNotFoundException;
import com.example.miniproject.repository.OrderRepository;
import com.example.miniproject.repository.ProductRepository;
import com.example.miniproject.repository.RoleRepository;
import com.example.miniproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * Cấp/đổi quyền nhân viên – UC-08 (chỉ MANAGER)
     */
    @Transactional
    public String updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));

        Role.RoleName roleNameEnum;
        try {
            // Hỗ trợ cả "STAFF" lẫn "ROLE_STAFF"
            String normalizedName = roleName.toUpperCase();
            if (!normalizedName.startsWith("ROLE_")) {
                normalizedName = "ROLE_" + normalizedName;
            }
            roleNameEnum = Role.RoleName.valueOf(normalizedName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Role không hợp lệ: " + roleName +
                    ". Các giá trị hợp lệ: ROLE_CUSTOMER, ROLE_STAFF, ROLE_MANAGER");
        }

        Role role = roleRepository.findByName(roleNameEnum)
                .orElseThrow(() -> new ResourceNotFoundException("Role không tồn tại: " + roleName));

        user.setRoles(Set.of(role));
        userRepository.save(user);

        return "Đã cập nhật quyền của người dùng '" + user.getUsername() + "' thành " + roleNameEnum.name();
    }

    /**
     * Báo cáo doanh thu – UC-07 (chỉ MANAGER)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        Long totalOrders = orderRepository.countAllOrders();
        Long pendingOrders = orderRepository.countPendingOrders();
        Long totalProducts = productRepository.count();

        dashboard.put("totalRevenue", totalRevenue);
        dashboard.put("totalOrders", totalOrders);
        dashboard.put("pendingOrders", pendingOrders);
        dashboard.put("totalProducts", totalProducts);
        dashboard.put("reportGeneratedAt", LocalDateTime.now().toString());
        dashboard.put("currency", "VND");

        return dashboard;
    }
}
