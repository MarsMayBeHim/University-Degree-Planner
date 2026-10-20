package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.ProgressDto;
import com.degreeplanner.backend.service.ProgressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/progress")
public class ProgressController {

    // Temporary: replaced by the logged-in user in Part 7
    private static final Long TEST_USER_ID = 1L;

    private final ProgressService service;

    public ProgressController(ProgressService service) {
        this.service = service;
    }

    @GetMapping
    public ProgressDto progress() {
        return service.progress(TEST_USER_ID);
    }
}