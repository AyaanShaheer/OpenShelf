package com.openshelf.admin;

import com.openshelf.admin.dto.UserDtos;
import com.openshelf.admin.service.AdminUserService;
import com.openshelf.auth.domain.Role;
import com.openshelf.auth.domain.User;
import com.openshelf.auth.repository.UserRepository;
import com.openshelf.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private AdminUserService service;

    @BeforeEach
    void setUp() {
        service = new AdminUserService(userRepository, passwordEncoder);
    }

    @Test
    void rejectsDuplicateEmailOnCreate() {
        when(userRepository.existsByEmailIgnoreCase("jane@example.edu")).thenReturn(true);
        UserDtos.CreateUserRequest request = new UserDtos.CreateUserRequest(
                "Jane", "jane@example.edu", "secret", Role.CATALOGUER, true);

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void preventsDeletingTheLastActiveAdmin() {
        User admin = admin(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findAll()).thenReturn(List.of(admin));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("last active admin");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void allowsDeletingAnAdminWhenAnotherAdminRemains() {
        User first = admin(1L);
        User second = admin(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(first));
        when(userRepository.findAll()).thenReturn(List.of(first, second));

        service.delete(1L);

        verify(userRepository).delete(first);
    }

    @Test
    void createsCataloguerWithHashedPassword() {
        when(userRepository.existsByEmailIgnoreCase("Jane@Example.edu")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        UserDtos.UserResponse response = service.create(new UserDtos.CreateUserRequest(
                "Jane", "Jane@Example.edu", "secret", Role.CATALOGUER, null));

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.email()).isEqualTo("jane@example.edu");
        assertThat(response.role()).isEqualTo(Role.CATALOGUER);
        assertThat(response.active()).isTrue();
    }

    private User admin(Long id) {
        User user = new User();
        user.setId(id);
        user.setFullName("Admin " + id);
        user.setEmail("admin" + id + "@example.edu");
        user.setRole(Role.ADMIN);
        user.setActive(true);
        return user;
    }
}
