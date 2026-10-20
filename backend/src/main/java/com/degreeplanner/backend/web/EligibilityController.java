package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.EligibilityDto;
import com.degreeplanner.backend.service.EligibilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class EligibilityController {

    // Temporary: replaced by the logged-in user in Part 7
    private static final Long TEST_USER_ID = 1L;

    private final EligibilityService service;

    public EligibilityController(EligibilityService service) {
        this.service = service;
    }

    @GetMapping("/{id}/eligibility")
    public EligibilityDto eligibility(@PathVariable Long id) {
        return service.check(TEST_USER_ID, id);
    }
}