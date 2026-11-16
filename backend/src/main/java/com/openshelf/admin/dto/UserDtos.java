package com.openshelf.admin.dto;

import com.openshelf.auth.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserDtos {
    public record UserResponse(Long id, String fullName, String email, Role role, boolean active) {}

    public record CreateUserRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotNull Role role,
            Boolean active
    ) {}

    public record UpdateUserRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            String password,
            @NotNull Role role,
            @NotNull Boolean active
    ) {}
}
