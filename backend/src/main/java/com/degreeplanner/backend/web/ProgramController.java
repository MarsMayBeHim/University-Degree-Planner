package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.*;
import com.degreeplanner.backend.service.ProgramService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    private final ProgramService service;

    public ProgramController(ProgramService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProgramDto> list() {
        return service.listPrograms();
    }

    @GetMapping("/{id}/requirements")
    public List<RequirementGroupDto> requirements(@PathVariable Long id) {
        return service.requirementTree(id);
    }
}