package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.GenerateRequest;
import com.degreeplanner.backend.dto.GenerationResultDto;
import com.degreeplanner.backend.dto.PlanDto;
import com.degreeplanner.backend.service.PlanGenerationService;
import com.degreeplanner.backend.service.PlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/plan")
public class PlanController {

    // Temporary: replaced by the logged-in user in Part 7
    private static final Long TEST_USER_ID = 1L;
    private static final int DEFAULT_MAX_CREDITS = 12;

    private final PlanService planService;
    private final PlanGenerationService generationService;

    public PlanController(PlanService planService, PlanGenerationService generationService) {
        this.planService = planService;
        this.generationService = generationService;
    }

    @GetMapping
    public PlanDto plan() {
        return planService.plan(TEST_USER_ID);
    }

    @PostMapping("/generate")
    public GenerationResultDto generate(@RequestBody(required = false) GenerateRequest request) {
        int maxCredits = request == null || request.maxCredits() == null
                ? DEFAULT_MAX_CREDITS
                : request.maxCredits();
        return generationService.generate(TEST_USER_ID, maxCredits);
    }
}