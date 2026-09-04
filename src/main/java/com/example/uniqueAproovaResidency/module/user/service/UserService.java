package com.example.uniqueAproovaResidency.module.user.service;

import com.example.uniqueAproovaResidency.common.BusinessRuleException;
import com.example.uniqueAproovaResidency.common.ResourceNotFoundException;
import com.example.uniqueAproovaResidency.module.flat.entity.Flat;
import com.example.uniqueAproovaResidency.module.flat.repository.FlatRepository;
import com.example.uniqueAproovaResidency.module.role.Role;
import com.example.uniqueAproovaResidency.module.user.dto.CreateUserRequest;
import com.example.uniqueAproovaResidency.module.user.dto.UserDto;
import com.example.uniqueAproovaResidency.module.user.entity.User;
import com.example.uniqueAproovaResidency.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FlatRepository flatRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("SERVICE [UserService.getAllUsers]");
        List<UserDto> users = userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
        log.info("SERVICE [UserService.getAllUsers] -> Total users found: {}", users.size());
        return users;
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(String id) {
        log.info("SERVICE [UserService.getUserById] -> Target ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        log.info("SERVICE [UserService.createUser] -> Name: {}, Phone: {}, Role: {}, Flat: {}",
                request.getName(), request.getPhone(), request.getRole(), request.getFlatId());

        String targetId = StringUtils.hasText(request.getUserId())
                ? request.getUserId().trim()
                : "user-" + (StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : UUID.randomUUID().toString());

        if (userRepository.existsById(targetId)) {
            log.warn("SERVICE [UserService.createUser] -> Target User ID {} already exists", targetId);
            throw new BusinessRuleException("USER_ID_EXISTS", "User ID already exists. Please use a different User ID.");
        }

        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            log.warn("SERVICE [UserService.createUser] -> Phone {} already exists", request.getPhone());
            throw new BusinessRuleException("USER_ID_EXISTS", "User ID already exists. Please use a different User ID.");
        }

        if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            log.warn("SERVICE [UserService.createUser] -> Email {} already exists", request.getEmail());
            throw new BusinessRuleException("USER_ID_EXISTS", "User ID already exists. Please use a different User ID.");
        }

        Flat flat = null;
        if (StringUtils.hasText(request.getFlatId())) {
            flat = flatRepository.findById(request.getFlatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Flat", "id", request.getFlatId()));

            // Strict check: Prevent assigning multiple users to the same flat unless deleted
            final String flatIdToMatch = request.getFlatId();
            List<User> existingFlatUsers = userRepository.findAll().stream()
                    .filter(u -> u.getFlat() != null && u.getFlat().getId().equalsIgnoreCase(flatIdToMatch))
                    .collect(Collectors.toList());

            if (!existingFlatUsers.isEmpty()) {
                User existing = existingFlatUsers.get(0);
                log.warn("SERVICE [UserService.createUser] -> Flat {} is already assigned to user {}", flat.getFlatNumber(), existing.getName());
                throw new BusinessRuleException(
                        "FLAT_OCCUPIED",
                        "Flat " + flat.getFlatNumber() + " already has a registered resident (" + existing.getName() + "). Please delete the existing resident before creating a new member for Flat " + flat.getFlatNumber() + "."
                );
            }
        }

        Role targetRole = request.getRole() != null ? request.getRole() : Role.FLAT_MEMBER;

        User user = User.builder()
                .id(targetId)
                .name(request.getName())
                .email(request.getEmail() != null ? request.getEmail() : request.getPhone() + "@aproova.local")
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .flat(flat)
                .role(targetRole)
                .status("ACTIVE")
                .language(request.getLanguage() != null ? request.getLanguage() : "EN")
                .build();

        User savedUser = userRepository.save(user);
        log.info("SERVICE [UserService.createUser] -> Created User successfully with ID: {}", savedUser.getId());
        return UserDto.fromEntity(savedUser);
    }

    @Transactional
    public UserDto updateUser(String id, CreateUserRequest request) {
        log.info("SERVICE [UserService.updateUser] -> User ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        if (StringUtils.hasText(request.getFlatId())) {
            Flat flat = flatRepository.findById(request.getFlatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Flat", "id", request.getFlatId()));

            final String flatIdToMatch = request.getFlatId();
            List<User> existingFlatUsers = userRepository.findAll().stream()
                    .filter(u -> u.getFlat() != null && u.getFlat().getId().equalsIgnoreCase(flatIdToMatch) && !u.getId().equals(id))
                    .collect(Collectors.toList());

            if (!existingFlatUsers.isEmpty()) {
                User existing = existingFlatUsers.get(0);
                log.warn("SERVICE [UserService.updateUser] -> Flat {} is already assigned to user {}", flat.getFlatNumber(), existing.getName());
                throw new BusinessRuleException(
                        "FLAT_OCCUPIED",
                        "Flat " + flat.getFlatNumber() + " already has a registered resident (" + existing.getName() + "). Please delete the existing resident before reassigning Flat " + flat.getFlatNumber() + "."
                );
            }
            user.setFlat(flat);
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("SERVICE [UserService.updateUser] -> Updated User ID: {}", updatedUser.getId());
        return UserDto.fromEntity(updatedUser);
    }

    @Transactional
    public UserDto assignMaintenanceRole(String flatNumber) {
        log.info("SERVICE [UserService.assignMaintenanceRole] -> Target Flat Number: {}", flatNumber);
        String cleanFlatNum = flatNumber.replaceAll("(?i)flat-", "").trim();
        String targetFlatId = "flat-" + cleanFlatNum;

        // Reset previous MAINTENANCE_FLAT users back to FLAT_MEMBER
        List<User> previousMaintUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.MAINTENANCE_FLAT || u.getRole() == Role.MAINTENANCE_MEMBER)
                .collect(Collectors.toList());

        for (User prev : previousMaintUsers) {
            log.info("SERVICE [UserService.assignMaintenanceRole] -> Reverting User {} from MAINTENANCE_FLAT to FLAT_MEMBER", prev.getId());
            prev.setRole(Role.FLAT_MEMBER);
            userRepository.save(prev);
        }

        // Assign new flat user as MAINTENANCE_FLAT
        User targetUser = userRepository.findAll().stream()
                .filter(u -> u.getFlat() != null && (cleanFlatNum.equals(u.getFlat().getFlatNumber()) || targetFlatId.equals(u.getFlat().getId())))
                .findFirst()
                .orElse(null);

        if (targetUser != null) {
            log.info("SERVICE [UserService.assignMaintenanceRole] -> Promoting User {} (Flat {}) to MAINTENANCE_FLAT", targetUser.getId(), cleanFlatNum);
            targetUser.setRole(Role.MAINTENANCE_FLAT);
            userRepository.save(targetUser);
            return UserDto.fromEntity(targetUser);
        } else {
            log.warn("SERVICE [UserService.assignMaintenanceRole] -> No existing user registered for Flat {}. Returning DTO fallback.", cleanFlatNum);
            return UserDto.builder()
                    .role(Role.MAINTENANCE_FLAT)
                    .flatNumber(cleanFlatNum)
                    .build();
        }
    }

    @Transactional
    public UserDto resetUserPassword(String targetUserId, String newPassword) {
        log.info("SERVICE [UserService.resetUserPassword] -> Target User ID: {}", targetUserId);
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", targetUserId));

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        log.info("SERVICE [UserService.resetUserPassword] -> Password reset successful for User ID: {}", updatedUser.getId());
        return UserDto.fromEntity(updatedUser);
    }

    @Transactional
    public UserDto updateUserStatus(String id, String status) {
        log.info("SERVICE [UserService.updateUserStatus] -> User ID: {}, New Status: {}", id, status);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        log.info("SERVICE [UserService.updateUserStatus] -> Status updated for User ID: {}", updatedUser.getId());
        return UserDto.fromEntity(updatedUser);
    }

    @Transactional
    public void deleteUser(String id) {
        log.info("SERVICE [UserService.deleteUser] -> Target User ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
        log.info("SERVICE [UserService.deleteUser] -> User ID {} deleted successfully", id);
    }
}
