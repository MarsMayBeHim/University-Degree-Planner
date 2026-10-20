package com.degreeplanner.backend.service;

import com.degreeplanner.backend.dto.ProgressDto;
import com.degreeplanner.backend.model.AppUser;
import com.degreeplanner.backend.model.Course;
import com.degreeplanner.backend.model.Program;
import com.degreeplanner.backend.model.StudentCourse;
import com.degreeplanner.backend.planner.GroupProgress;
import com.degreeplanner.backend.planner.ProgressCalculator;
import com.degreeplanner.backend.repository.AppUserRepository;
import com.degreeplanner.backend.repository.CourseRepository;
import com.degreeplanner.backend.repository.ProgramRepository;
import com.degreeplanner.backend.repository.StudentCourseRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProgressService {

    private final AppUserRepository users;
    private final ProgramRepository programs;
    private final ProgramService programService;
    private final StudentCourseRepository studentCourses;
    private final CourseRepository courses;

    public ProgressService(AppUserRepository users, ProgramRepository programs,
                           ProgramService programService, StudentCourseRepository studentCourses,
                           CourseRepository courses) {
        this.users = users;
        this.programs = programs;
        this.programService = programService;
        this.studentCourses = studentCourses;
        this.courses = courses;
    }

    @Transactional(readOnly = true)
    public ProgressDto progress(Long userId) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getProgramId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a program first");
        }
        Program program = programs.findById(user.getProgramId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program not found"));

        List<StudentCourse> rows = studentCourses.findByUserId(userId);
        Map<Long, Course> byId = courses
                .findAllById(rows.stream().map(StudentCourse::getCourseId).toList())
                .stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        Set<String> completedCodes = new HashSet<>();
        BigDecimal completedCredits = BigDecimal.ZERO;
        BigDecimal plannedCredits = BigDecimal.ZERO;
        for (StudentCourse row : rows) {
            Course course = byId.get(row.getCourseId());
            if ("COMPLETED".equals(row.getStatus())) {
                completedCodes.add(course.getSubject() + " " + course.getNumber());
                completedCredits = completedCredits.add(course.getCreditsMin());
            } else {
                plannedCredits = plannedCredits.add(course.getCreditsMin());
            }
        }

        List<GroupProgress> groups = programService.requirementTree(program.getId()).stream()
                .map(g -> ProgressCalculator.evaluate(g, completedCodes))
                .toList();
        boolean allMet = !groups.isEmpty() && groups.stream().allMatch(GroupProgress::done);

        return new ProgressDto(program.getId(), program.getName(), program.getCatalogYear(),
                program.getTotalCredits(), completedCredits, plannedCredits, allMet, groups);
    }
}