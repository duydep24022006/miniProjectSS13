package com.example.miniproject.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO đăng ký tài khoản – Ràng buộc theo SRS Section 4.2
 */
@Data
public class UserRegisterDTO {

    @NotBlank
    @Size(min = 4, max = 20, message = "Tài khoản phải từ 4 đến 20 ký tự")
    private String username;

    @NotBlank
    @Size(min = 6, message = "Mật khẩu phải chứa ít nhất 6 ký tự")
    private String password;

    @NotBlank
    @Email(message = "Định dạng Email không hợp lệ")
    private String email;

    private String phone;
}
