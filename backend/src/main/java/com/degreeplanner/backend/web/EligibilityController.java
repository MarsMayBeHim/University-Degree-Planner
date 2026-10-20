package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.EligibilityDto;
import com.degreeplanner.backend.security.CurrentUser;
import com.degreeplanner.backend.service.EligibilityService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class EligibilityController {

    private final EligibilityService service;

    public EligibilityController(EligibilityService service) {
        this.service = service;
    }

    @GetMapping("/{id}/eligibility")
    public EligibilityDto eligibility(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.check(CurrentUser.id(jwt), id);
    }
}