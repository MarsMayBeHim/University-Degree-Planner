package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.GenerateRequest;
import com.degreeplanner.backend.dto.GenerationResultDto;
import com.degreeplanner.backend.dto.PlanDto;
import com.degreeplanner.backend.security.CurrentUser;
import com.degreeplanner.backend.service.PlanGenerationService;
import com.degreeplanner.backend.service.PlanService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/plan")
public class PlanController {

    private static final int DEFAULT_MIN_CREDITS = 12;
    private static final int DEFAULT_MAX_CREDITS = 16;

    private final PlanService planService;
    private final PlanGenerationService generationService;

    public PlanController(PlanService planService, PlanGenerationService generationService) {
        this.planService = planService;
        this.generationService = generationService;
    }

    @GetMapping
    public PlanDto plan(@AuthenticationPrincipal Jwt jwt) {
        return planService.plan(CurrentUser.id(jwt));
    }

    @PostMapping("/generate")
    public GenerationResultDto generate(@AuthenticationPrincipal Jwt jwt,
                                        @RequestBody(required = false) GenerateRequest request) {
        int min = request == null || request.minCredits() == null
                ? DEFAULT_MIN_CREDITS
                : request.minCredits();
        int max = request == null || request.maxCredits() == null
                ? DEFAULT_MAX_CREDITS
                : request.maxCredits();
        Integer fromYear = request == null ? null : request.fromYear();
        String fromTerm = request == null ? null : request.fromTerm();
        return generationService.generate(CurrentUser.id(jwt), min, max, fromYear, fromTerm);
    }
}