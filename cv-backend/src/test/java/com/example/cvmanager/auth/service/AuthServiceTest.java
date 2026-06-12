package com.example.cvmanager.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.cvmanager.auth.config.AuthProperties;
import com.example.cvmanager.auth.dto.LoginRequest;
import com.example.cvmanager.auth.security.JwtService;
import com.example.cvmanager.common.exception.BadRequestException;
import com.example.cvmanager.user.dto.UserCreateRequest;
import com.example.cvmanager.user.model.UserAccount;
import com.example.cvmanager.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                new JwtService(new AuthProperties(
                        "cv-manager",
                        "change-this-development-secret-key-to-at-least-32-bytes",
                        15)));
    }

    @Test
    void loginAcceptsOriginalPasswordAgainstStoredHash() {
        UserAccount user = new UserAccount(
                "alice@example.com",
                "Alice Student",
                passwordEncoder.encode("user123"),
                false);

        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com"))
                .thenReturn(Optional.of(user));

        var response = authService.login(new LoginRequest("alice@example.com", "user123"));

        assertEquals("alice@example.com", response.email());
        assertEquals("Alice Student", response.displayName());
        assertEquals("USER", response.role());
        assertFalse(response.admin());
        assertNotNull(response.token());
    }

    @Test
    void loginRejectsWrongPassword() {
        UserAccount user = new UserAccount(
                "alice@example.com",
                "Alice Student",
                passwordEncoder.encode("user123"),
                false);

        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                BadRequestException.class,
                () -> authService.login(new LoginRequest("alice@example.com", "wrong-password")));
    }

    @Test
    void registerRejectsDuplicateActiveEmail() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com"))
                .thenReturn(Optional.of(new UserAccount(
                        "alice@example.com",
                        "Alice Student",
                        passwordEncoder.encode("user123"),
                        false)));

        assertThrows(
                BadRequestException.class,
                () -> authService.register(new UserCreateRequest(
                        "Alice@example.com",
                        "Alice New",
                        "user123")));
    }

    @Test
    void registerAllowsEmailWhenOnlyDeletedAccountMatches() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("alice@example.com"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(new UserCreateRequest(
                "Alice@example.com",
                "Alice New",
                "user123"));

        assertEquals("alice@example.com", response.email());
        assertEquals("Alice New", response.displayName());
        assertFalse(response.admin());
        assertNotNull(response.token());
    }
}
