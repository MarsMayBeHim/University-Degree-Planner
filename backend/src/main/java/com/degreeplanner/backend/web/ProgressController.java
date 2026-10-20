package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.ProgressDto;
import com.degreeplanner.backend.security.CurrentUser;
import com.degreeplanner.backend.service.ProgressService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/progress")
public class ProgressController {

    private final ProgressService service;

    public ProgressController(ProgressService service) {
        this.service = service;
    }

    @GetMapping
    public ProgressDto progress(@AuthenticationPrincipal Jwt jwt) {
        return service.progress(CurrentUser.id(jwt));
    }
}