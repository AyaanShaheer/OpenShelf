package com.openshelf.auth.dto;

import com.openshelf.auth.domain.Role;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        UserSummary user
) {
    public record UserSummary(Long id, String fullName, String email, Role role) {}
}
