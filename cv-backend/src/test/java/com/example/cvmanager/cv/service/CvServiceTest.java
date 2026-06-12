package com.example.cvmanager.cv.service;

import static org.mockito.Mockito.*;

import com.example.cvmanager.auth.security.AuthenticatedUser;
import com.example.cvmanager.common.security.AdminAccessService;
import com.example.cvmanager.cv.mapper.CvMapper;
import com.example.cvmanager.cv.repository.CvRepository;
import com.example.cvmanager.user.repository.UserRepository;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CvServiceTest {

    private CvRepository cvRepository;
    private UserRepository userRepository;
    private AuthenticatedUser user;
    private CvService cvService;

    @BeforeEach
    void setUp() {
        cvRepository = mock(CvRepository.class);
        userRepository = mock(UserRepository.class);
        user = new AuthenticatedUser(1L, "alice@example.com", "Alice Student", "USER");

        cvService = new CvService(
                cvRepository,
                userRepository,
                mock(CvMapper.class),
                mock(AdminAccessService.class));
    }

    @Test
    void listCvsForUserReturnsOnlyOwnedActiveListItems() {
        when(cvRepository.findActiveListItemsByOwner(1L)).thenReturn(List.of());

        cvService.listCvs(user);

        verify(cvRepository).findActiveListItemsByOwner(1L);
        verify(cvRepository, never()).findActiveListItems();
        verifyNoInteractions(userRepository);
    }

    @Test
    void listCvsForAdminReturnsAllActiveListItems() {
        AuthenticatedUser admin = new AuthenticatedUser(2L, "admin@example.com", "Admin", "ADMIN");
        when(cvRepository.findActiveListItems()).thenReturn(List.of());

        cvService.listCvs(admin);

        verify(cvRepository).findActiveListItems();
        verify(cvRepository, never()).findActiveListItemsByOwner(anyLong());
        verifyNoInteractions(userRepository);
    }
}
