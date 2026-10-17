package com.degreeplanner.backend.service;

import com.degreeplanner.backend.dto.*;
import com.degreeplanner.backend.model.*;
import com.degreeplanner.backend.repository.*;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgramService {

    private final ProgramRepository programs;
    private final RequirementGroupRepository groups;

    public ProgramService(ProgramRepository programs, RequirementGroupRepository groups) {
        this.programs = programs;
        this.groups = groups;
    }

    public List<ProgramDto> listPrograms() {
        return programs.findAll().stream()
                .map(p -> new ProgramDto(p.getId(), p.getName(), p.getDegreeType(),
                        p.getCatalogYear(), p.getTotalCredits()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RequirementGroupDto> requirementTree(Long programId) {
        List<RequirementGroup> all = groups.findByProgramIdOrderBySortOrder(programId);
        return all.stream()
                .filter(g -> g.getParentId() == null)
                .map(g -> toDto(g, all))
                .toList();
    }

    private RequirementGroupDto toDto(RequirementGroup g, List<RequirementGroup> all) {
        List<RequirementGroupDto> children = all.stream()
                .filter(c -> g.getId().equals(c.getParentId()))
                .map(c -> toDto(c, all))
                .toList();
        List<CourseDto> courses = g.getCourses().stream()
                .sorted(Comparator.comparing(Course::getSubject).thenComparing(Course::getNumber))
                .map(CourseDto::from)
                .toList();
        return new RequirementGroupDto(g.getId(), g.getName(), g.getRuleType(),
                g.getMinCourses(), g.getMinCredits(), courses, children);
    }
}