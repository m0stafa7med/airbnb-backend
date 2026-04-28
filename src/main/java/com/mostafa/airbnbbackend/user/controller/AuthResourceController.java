package com.mostafa.airbnbbackend.user.controller;

import com.mostafa.airbnbbackend.user.dto.ReadUserDTO;
import com.mostafa.airbnbbackend.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthResourceController {

    private final UserService userService;

    public AuthResourceController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/get-authenticated-user")
    public ResponseEntity<ReadUserDTO> getAuthenticatedUser(
            @AuthenticationPrincipal Jwt jwt, @RequestParam boolean forceResync) {
        if (jwt == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        userService.syncWithIdp(jwt, forceResync);
        ReadUserDTO connectedUser = userService.getAuthenticatedUserFromSecurityContext();
        return new ResponseEntity<>(connectedUser, HttpStatus.OK);
    }
}
