package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.PrereqCourseDto;
import com.degreeplanner.backend.dto.ProgramDto;
import com.degreeplanner.backend.dto.RequirementGroupDto;
import com.degreeplanner.backend.service.PrerequisiteMapService;
import com.degreeplanner.backend.service.ProgramService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    private final ProgramService service;
    private final PrerequisiteMapService mapService;

    public ProgramController(ProgramService service, PrerequisiteMapService mapService) {
        this.service = service;
        this.mapService = mapService;
    }

    @GetMapping
    public List<ProgramDto> list() {
        return service.listPrograms();
    }

    @GetMapping("/{id}/requirements")
    public List<RequirementGroupDto> requirements(@PathVariable Long id) {
        return service.requirementTree(id);
    }

    @GetMapping("/{id}/prerequisites")
    public List<PrereqCourseDto> prerequisites(@PathVariable Long id) {
        return mapService.forProgram(id);
    }
}