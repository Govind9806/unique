package com.example.uniqueAproovaResidency.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    private String emailOrPhone;
    private String mobileNumber;
    private String phone;
    private String email;
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    public String getIdentifier() {
        if (emailOrPhone != null && !emailOrPhone.isBlank()) return emailOrPhone.trim();
        if (mobileNumber != null && !mobileNumber.isBlank()) return mobileNumber.trim();
        if (phone != null && !phone.isBlank()) return phone.trim();
        if (email != null && !email.isBlank()) return email.trim();
        if (username != null && !username.isBlank()) return username.trim();
        return "";
    }
}
