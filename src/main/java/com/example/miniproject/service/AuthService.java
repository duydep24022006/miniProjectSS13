package com.example.miniproject.service;

import com.example.miniproject.dto.request.LoginRequest;
import com.example.miniproject.dto.request.UserRegisterDTO;
import com.example.miniproject.dto.response.AuthResponse;
import com.example.miniproject.entity.Cart;
import com.example.miniproject.entity.Role;
import com.example.miniproject.entity.User;
import com.example.miniproject.exception.ResourceNotFoundException;
import com.example.miniproject.repository.CartRepository;
import com.example.miniproject.repository.RoleRepository;
import com.example.miniproject.repository.UserRepository;
import com.example.miniproject.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Đăng ký tài khoản – UC-02
     * Mật khẩu được BCrypt hash trước khi lưu (NFR-04)
     */
    @Transactional
    public String register(UserRegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Tên tài khoản '" + dto.getUsername() + "' đã tồn tại");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' đã được sử dụng");
        }

        // Lấy role mặc định CUSTOMER cho người dùng mới
        Role customerRole = roleRepository.findByName(Role.RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ROLE_CUSTOMER chưa được khởi tạo. Vui lòng khởi động lại ứng dụng."));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // BCrypt hash (NFR-04)
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRoles(Set.of(customerRole));

        User savedUser = userRepository.save(user);

        // Tự động tạo giỏ hàng cho người dùng mới
        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);

        return "Đăng ký tài khoản thành công! Chào mừng " + dto.getUsername();
    }

    /**
     * Đăng nhập – UC-03
     * Sử dụng AuthenticationManager để xác thực, sinh JWT Token chứa Roles (SRS 3.2.1)
     */
    public AuthResponse login(LoginRequest request) {
        // AuthenticationManager xác thực username/password – SRS UC-03 bước 2
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails); // Sinh JWT với Roles claim – bước 4

        List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());

        return new AuthResponse(token, userDetails.getUsername(), roles);
    }
}
