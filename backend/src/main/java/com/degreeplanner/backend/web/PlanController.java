package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.PlanDto;
import com.degreeplanner.backend.service.PlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/plan")
public class PlanController {

    // Temporary: replaced by the logged-in user in Part 7
    private static final Long TEST_USER_ID = 1L;

    private final PlanService service;

    public PlanController(PlanService service) {
        this.service = service;
    }

    @GetMapping
    public PlanDto plan() {
        return service.plan(TEST_USER_ID);
    }
}