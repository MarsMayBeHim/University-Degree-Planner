package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.ProfileRequest;
import com.degreeplanner.backend.dto.UserDto;
import com.degreeplanner.backend.security.CurrentUser;
import com.degreeplanner.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final AuthService service;

    public MeController(AuthService service) {
        this.service = service;
    }

    @GetMapping
    public UserDto me(@AuthenticationPrincipal Jwt jwt) {
        return service.me(CurrentUser.id(jwt));
    }

    @PutMapping("/profile")
    public UserDto updateProfile(@AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody ProfileRequest request) {
        return service.updateProfile(CurrentUser.id(jwt), request);
    }
}