package com.example.miniproject.config;

import com.example.miniproject.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cấu hình bảo mật theo SRS Section 2.1 (RBAC) và Section 3.1 (API Matrix)
 * - Stateless Session (JWT)
 * - BCrypt strength=10 (NFR-04)
 * - Phân quyền chi tiết theo từng endpoint
 * - Trả về ErrorResponse chuẩn TechNova cho 401/403 (Section 6.3)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * BCryptPasswordEncoder strength=10 – NFR-04
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Spring Security 6+/7+: UserDetailsService bắt buộc truyền qua constructor
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì sử dụng Stateless JWT (SRS Section 2.1)
                .csrf(csrf -> csrf.disable())

                // Stateless – không dùng HTTP Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ===== Phân quyền RBAC theo SRS Section 3.1 API Matrix =====
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC – ANONYMOUS có thể truy cập
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/products/**").permitAll()
                        .requestMatchers("/api/v1/public/auth/**").permitAll()

                        // CUSTOMER – Giỏ hàng và đặt hàng
                        .requestMatchers("/api/v1/customer/cart/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/v1/customer/orders/**").hasRole("CUSTOMER")

                        // STAFF + MANAGER – Quản lý sản phẩm
                        .requestMatchers("/api/v1/admin/products/**").hasAnyRole("STAFF", "MANAGER")

                        // MANAGER only – Cấp quyền và dashboard
                        .requestMatchers("/api/v1/admin/users/**").hasRole("MANAGER")
                        .requestMatchers("/api/v1/admin/dashboard/**").hasRole("MANAGER")

                        // Mọi request khác phải authenticated
                        .anyRequest().authenticated()
                )

                // ===== Xử lý lỗi 401/403 theo chuẩn TechNova (SRS Section 6.3) =====
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write(buildErrorJson(
                                    401, "Unauthorized",
                                    "Tài khoản hoặc mật khẩu không chính xác",
                                    request.getRequestURI()
                            ));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write(buildErrorJson(
                                    403, "Forbidden",
                                    "Truy cập bị từ chối: Khách hàng không có quyền truy cập vào phân hệ Quản trị (Admin Portal).",
                                    request.getRequestURI()
                            ));
                        })
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Xây dựng JSON lỗi chuẩn TechNova (SRS Section 6.3) không dùng ObjectMapper
     * để tránh circular dependency
     */
    private String buildErrorJson(int status, String error, String message, String path) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'+00:00'"));
        return String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                timestamp, status, error, message, path
        );
    }
}
