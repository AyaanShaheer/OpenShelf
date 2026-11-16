package com.openshelf.admin.service;

import com.openshelf.admin.dto.UserDtos;
import com.openshelf.auth.domain.Role;
import com.openshelf.auth.domain.User;
import com.openshelf.auth.repository.UserRepository;
import com.openshelf.common.exception.ConflictException;
import com.openshelf.common.exception.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDtos.UserResponse> list() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserDtos.UserResponse create(UserDtos.CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email already in use");
        }
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(request.active() == null || request.active());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserDtos.UserResponse update(Long id, UserDtos.UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) throw new ConflictException("Email already in use");
        });
        boolean losesAdmin = user.getRole() == Role.ADMIN
                && (request.role() != Role.ADMIN || !request.active());
        if (losesAdmin && !hasAnotherActiveAdmin(user.getId())) {
            throw new ConflictException("Cannot remove the last active admin account");
        }
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setRole(request.role());
        user.setActive(request.active());
        if (StringUtils.hasText(request.password())) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN && !hasAnotherActiveAdmin(user.getId())) {
            throw new ConflictException("Cannot remove the last active admin account");
        }
        userRepository.delete(user);
    }

    private boolean hasAnotherActiveAdmin(Long excludedId) {
        return userRepository.findAll().stream()
                .anyMatch(u -> !u.getId().equals(excludedId)
                        && u.getRole() == Role.ADMIN
                        && u.isActive());
    }

    private UserDtos.UserResponse toResponse(User user) {
        return new UserDtos.UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), user.isActive());
    }
}
