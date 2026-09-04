package com.example.uniqueAproovaResidency.module.auth.dto;

import com.example.uniqueAproovaResidency.module.role.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private String flatId;
    private String flatNumber;
    private Integer floor;
    private String language;
}
