package com.example.cvmanager.user.controller;

import com.example.cvmanager.auth.security.AuthenticatedUser;
import com.example.cvmanager.common.exception.UnauthorizedException;
import com.example.cvmanager.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDeleteOwnAccount(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            throw new UnauthorizedException("Authentication is required", "AUTH_REQUIRED");
        }

        userService.softDeleteOwnAccount(user.userId());
    }
}
