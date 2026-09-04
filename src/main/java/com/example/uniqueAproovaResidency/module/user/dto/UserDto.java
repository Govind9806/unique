package com.example.uniqueAproovaResidency.module.user.dto;

import com.example.uniqueAproovaResidency.module.role.Role;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private String flatId;
    private String flatNumber;
    private String status;
    private String language;
    private LocalDateTime createdAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .flatId(user.getFlat() != null ? user.getFlat().getId() : null)
                .flatNumber(user.getFlat() != null ? user.getFlat().getFlatNumber() : null)
                .status(user.getStatus())
                .language(user.getLanguage())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
