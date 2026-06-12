package com.example.cvmanager.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.cvmanager.common.exception.BadRequestException;
import com.example.cvmanager.cv.repository.CvRepository;
import com.example.cvmanager.user.dto.UserCreateRequest;
import com.example.cvmanager.user.dto.UserUpdateRequest;
import com.example.cvmanager.user.model.UserAccount;
import com.example.cvmanager.user.repository.UserRepository;

class UserServiceTest {

    private UserRepository userRepository;
    private CvRepository cvRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        cvRepository = mock(CvRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, cvRepository, passwordEncoder);
    }

    @Test
    void createUserHashesPasswordAndReturnsSafeResponse() {
        when(userRepository.findByEmailIgnoreCase("carol@example.com")).thenReturn(Optional.empty());
        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.createUser(new UserCreateRequest(
                "Carol@example.com",
                "Carol Candidate",
                "carol123"));

        assertEquals("carol@example.com", response.email());
        assertEquals("Carol Candidate", response.displayName());
        assertEquals("USER", response.role());
        assertFalse(response.admin());
        assertFalse(userCaptor.getValue().isAdmin());
        assertNotEquals("carol123", userCaptor.getValue().getPassword());
        assertTrue(passwordEncoder.matches("carol123", userCaptor.getValue().getPassword()));
    }

    @Test
    void createUserRejectsDuplicateEmailIgnoringCase() {
        when(userRepository.findByEmailIgnoreCase("alice@example.com"))
                .thenReturn(Optional.of(new UserAccount("alice@example.com", "Alice Student", "hash", false)));

        assertThrows(
                BadRequestException.class,
                () -> userService.createUser(new UserCreateRequest("Alice@Example.com", "Alice Student", "user123")));
    }

    @Test
    void updateUserHashesNewPasswordWhenProvided() {
        UserAccount user = new UserAccount(
                "alice@example.com",
                "Alice Student",
                passwordEncoder.encode("old-password"),
                false);
        ReflectionTestUtils.setField(user, "id", 2L);

        when(userRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("alice.updated@example.com")).thenReturn(Optional.empty());
        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.updateUser(2L, new UserUpdateRequest(
                "Alice.Updated@example.com",
                "Alice Updated",
                "new-password",
                "USER"));

        assertEquals("alice.updated@example.com", response.email());
        assertEquals("Alice Updated", response.displayName());
        assertNotEquals("new-password", userCaptor.getValue().getPassword());
        assertTrue(passwordEncoder.matches("new-password", userCaptor.getValue().getPassword()));
    }

    @Test
    void updateUserRejectsSelfDemotionEvenWhenOtherAdminsExist() {
        UserAccount admin = new UserAccount(
                "admin@example.com",
                "Admin User",
                passwordEncoder.encode("admin123"),
                true);
        ReflectionTestUtils.setField(admin, "id", 1L);

        when(userRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));

        assertThrows(
                BadRequestException.class,
                () -> userService.updateUser(1L, new UserUpdateRequest(
                        "admin@example.com",
                        "Admin User",
                        null,
                        "USER"), 1L));
    }

    @Test
    void softDeleteUserMarksAccountAndOwnedCvsWithSameTimestamp() {
        UserAccount user = new UserAccount(
                "alice@example.com",
                "Alice Student",
                passwordEncoder.encode("user123"),
                false);
        ReflectionTestUtils.setField(user, "id", 2L);

        when(userRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.of(user));
        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        userService.softDeleteUser(2L);

        assertNotNull(userCaptor.getValue().getDeletedAt());
        verify(cvRepository).markDeletedByOwnerId(2L, userCaptor.getValue().getDeletedAt());
    }
}
