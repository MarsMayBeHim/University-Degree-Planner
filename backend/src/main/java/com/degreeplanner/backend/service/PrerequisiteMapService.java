package com.degreeplanner.backend.service;

import com.degreeplanner.backend.dto.PrereqCourseDto;
import com.degreeplanner.backend.dto.RequirementGroupDto;
import com.degreeplanner.backend.model.Course;
import com.degreeplanner.backend.planner.PrereqTreeParser;
import com.degreeplanner.backend.repository.CourseRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PrerequisiteMapService {

    private final ProgramService programService;
    private final CourseRepository courses;

    public PrerequisiteMapService(ProgramService programService, CourseRepository courses) {
        this.programService = programService;
        this.courses = courses;
    }

    @Transactional(readOnly = true)
    public List<PrereqCourseDto> forProgram(Long programId) {
        List<RequirementGroupDto> tree = programService.requirementTree(programId);
        if (tree.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Program not found");
        }
        Set<Long> ids = new LinkedHashSet<>();
        collect(tree, ids);

        return courses.findAllById(ids).stream()
                .sorted(Comparator.comparing(Course::getSubject).thenComparing(Course::getNumber))
                .map(c -> new PrereqCourseDto(c.getId(), c.getSubject() + " " + c.getNumber(),
                        c.getTitle(), c.getCreditsMin(), c.getPrereqText(),
                        PrereqTreeParser.parse(c.getPrereqTree())))
                .toList();
    }

    private static void collect(List<RequirementGroupDto> groups, Set<Long> ids) {
        for (RequirementGroupDto g : groups) {
            g.courses().forEach(c -> ids.add(c.id()));
            collect(g.children(), ids);
        }
    }
}