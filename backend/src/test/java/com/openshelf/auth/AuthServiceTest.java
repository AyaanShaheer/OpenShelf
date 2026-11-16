package com.openshelf.auth;

import com.openshelf.auth.domain.Role;
import com.openshelf.auth.domain.User;
import com.openshelf.auth.dto.LoginRequest;
import com.openshelf.auth.repository.UserRepository;
import com.openshelf.auth.security.JwtService;
import com.openshelf.auth.service.AuthService;
import com.openshelf.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;
    @Mock private UserDetails principal;

    private AuthService service;

    private void setUp() {
        service = new AuthService(authenticationManager, jwtService, userRepository, 3600000L);
    }

    @Test
    void rejectsBadCredentials() {
        setUp();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        assertThatThrownBy(() -> service.login(new LoginRequest("admin@example.edu", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void returnsTokenAndUserOnSuccessfulLogin() {
        setUp();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUsername()).thenReturn("admin@example.edu");
        User user = new User();
        user.setId(1L);
        user.setFullName("Admin");
        user.setEmail("admin@example.edu");
        user.setRole(Role.ADMIN);
        user.setActive(true);
        when(userRepository.findByEmailIgnoreCase("admin@example.edu")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        var response = service.login(new LoginRequest("admin@example.edu", "secret"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().role()).isEqualTo(Role.ADMIN);
        assertThat(response.expiresInMs()).isEqualTo(3600000L);
    }

    @Test
    void rejectsInactiveAccount() {
        setUp();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getUsername()).thenReturn("inactive@example.edu");
        User user = new User();
        user.setId(2L);
        user.setEmail("inactive@example.edu");
        user.setRole(Role.CATALOGUER);
        user.setActive(false);
        when(userRepository.findByEmailIgnoreCase("inactive@example.edu")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest("inactive@example.edu", "secret")))
                .isInstanceOf(BadRequestException.class);
    }
}
