package com.example.uniqueAproovaResidency.module.user.dto;

import com.example.uniqueAproovaResidency.module.role.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

    private String userId; // Optional explicit User ID

    @NotBlank(message = "Name is required")
    private String name;

    private String email;

    @NotBlank(message = "Mobile number (phone) is required")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private String flatId;

    @NotNull(message = "Role is required")
    private Role role;

    private String language = "EN";
}
