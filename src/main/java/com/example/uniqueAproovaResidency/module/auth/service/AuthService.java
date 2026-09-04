package com.example.uniqueAproovaResidency.module.auth.service;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.auth.dto.ChangePasswordRequest;
import com.example.uniqueAproovaResidency.module.auth.dto.LoginRequest;
import com.example.uniqueAproovaResidency.module.auth.dto.LoginResponse;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import com.example.uniqueAproovaResidency.security.JwtTokenProvider;
import com.example.uniqueAproovaResidency.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier.isBlank()) {
            throw new BusinessRuleException("INVALID_CREDENTIALS", "Mobile number or email is required");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));


        return LoginResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .flatId(user.getFlat() != null ? user.getFlat().getId() : null)
                .flatNumber(user.getFlat() != null ? user.getFlat().getFlatNumber() : null)
                .floor(user.getFlat() != null ? user.getFlat().getFloor() : null)
                .language(user.getLanguage())
                .build();
    }

    @Transactional
    public void changePassword(String targetUserId, ChangePasswordRequest request, UserPrincipal currentUser) {
        // STRICT AUTHORIZATION CHECK: User can only change their own password, unless ADMIN
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!currentUser.getId().equals(targetUserId) && !isAdmin) {
            throw new BusinessRuleException("UNAUTHORIZED_PASSWORD_CHANGE", "You are not authorized to change password for another resident");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", targetUserId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessRuleException("INVALID_OLD_PASSWORD", "Current password does not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessRuleException("SAME_PASSWORD", "New password cannot be identical to the old password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
