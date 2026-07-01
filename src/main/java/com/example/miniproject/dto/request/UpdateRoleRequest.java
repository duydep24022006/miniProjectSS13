package com.example.miniproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotBlank(message = "Role không được để trống")
    private String roleName; // VD: ROLE_STAFF, ROLE_MANAGER
}
