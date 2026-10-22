package com.degreeplanner.backend.service;

import com.degreeplanner.backend.dto.GenerationResultDto;
import com.degreeplanner.backend.dto.RequirementGroupDto;
import com.degreeplanner.backend.model.AppUser;
import com.degreeplanner.backend.model.Course;
import com.degreeplanner.backend.model.StudentCourse;
import com.degreeplanner.backend.planner.PlanCourse;
import com.degreeplanner.backend.planner.PlanGenerator;
import com.degreeplanner.backend.planner.PrereqTreeParser;
import com.degreeplanner.backend.planner.RequirementSelector;
import com.degreeplanner.backend.repository.AppUserRepository;
import com.degreeplanner.backend.repository.CourseRepository;
import com.degreeplanner.backend.repository.StudentCourseRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlanGenerationService {

    private static final int SEMESTER_COUNT = 8;
    private static final Set<String> TAKEN = Set.of("COMPLETED", "IN_PROGRESS");

    private final AppUserRepository users;
    private final ProgramService programService;
    private final StudentCourseRepository studentCourses;
    private final CourseRepository courses;

    public PlanGenerationService(AppUserRepository users, ProgramService programService,
                                 StudentCourseRepository studentCourses, CourseRepository courses) {
        this.users = users;
        this.programService = programService;
        this.studentCourses = studentCourses;
        this.courses = courses;
    }

    @Transactional
    public GenerationResultDto generate(Long userId, int minCredits, int maxCredits) {
        if (maxCredits < 3 || maxCredits > 24) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "maxCredits must be between 3 and 24");
        }
        if (minCredits < 0 || minCredits > maxCredits) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minCredits must be between 0 and maxCredits");
        }
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getProgramId() == null || user.getStartYear() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a program and start term first");
        }
        String startTerm = "SPRING".equals(user.getStartTerm()) ? "SPRING" : "FALL";

        List<RequirementGroupDto> tree = programService.requirementTree(user.getProgramId());
        Set<Long> treeIds = new HashSet<>();
        collectIds(tree, treeIds);

        List<StudentCourse> rows = studentCourses.findByUserId(userId);
        Set<Long> allIds = new HashSet<>(treeIds);
        rows.forEach(r -> allIds.add(r.getCourseId()));
        Map<Long, Course> byId = new HashMap<>();
        courses.findAllById(allIds).forEach(c -> byId.put(c.getId(), c));

        Set<String> done = new HashSet<>();
        for (StudentCourse r : rows) {
            if (TAKEN.contains(r.getStatus())) {
                done.add(code(byId.get(r.getCourseId())));
            }
        }

        Map<Long, Set<String>> offerings = loadOfferings();
        Map<String, PlanCourse> known = new LinkedHashMap<>();
        for (Long id : treeIds) {
            Course c = byId.get(id);
            known.put(code(c), new PlanCourse(c.getId(), code(c), c.getCreditsMin().intValue(),
                    PrereqTreeParser.parse(c.getPrereqTree()), offerings.getOrDefault(id, Set.of())));
        }

        Set<String> chosen = RequirementSelector.select(tree, known, done);
        List<PlanCourse> needed = chosen.stream().map(known::get).toList();

        PlanGenerator.Result result;
        try {
            result = PlanGenerator.generate(needed, done,
            new PlanGenerator.Settings(user.getStartYear(), startTerm, SEMESTER_COUNT, minCredits, maxCredits));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        }

        // Replace the old plan. Completed and in-progress rows are left alone.
        studentCourses.deleteAll(rows.stream().filter(r -> "PLANNED".equals(r.getStatus())).toList());
        studentCourses.flush();
        for (PlanGenerator.Placement p : result.placements()) {
            StudentCourse sc = new StudentCourse(userId, known.get(p.code()).courseId());
            sc.setStatus("PLANNED");
            sc.setPlannedYear(p.year());
            sc.setPlannedTerm(p.term());
            studentCourses.save(sc);
        }

        int semestersUsed = result.placements().stream()
                .mapToInt(p -> p.semesterIndex() + 1).max().orElse(0);
        List<GenerationResultDto.Unplaced> unplaced = result.unplaced().stream()
                .map(u -> new GenerationResultDto.Unplaced(u.code(), u.reason()))
                .toList();
        List<GenerationResultDto.LightSemester> light = result.lightSemesters().stream()
                .map(s -> new GenerationResultDto.LightSemester(label(s.term(), s.year()), s.credits()))
                .toList();
        return new GenerationResultDto(result.placements().size(), semestersUsed, unplaced, light);
    }

    private Map<Long, Set<String>> loadOfferings() {
        Map<Long, Set<String>> map = new HashMap<>();
        for (Object[] row : courses.findAllOfferings()) {
            map.computeIfAbsent(((Number) row[0]).longValue(), k -> new HashSet<>()).add((String) row[1]);
        }
        return map;
    }

    private static void collectIds(List<RequirementGroupDto> groups, Set<Long> ids) {
        for (RequirementGroupDto g : groups) {
            g.courses().forEach(c -> ids.add(c.id()));
            collectIds(g.children(), ids);
        }
    }

    private static String code(Course c) {
        return c.getSubject() + " " + c.getNumber();
    }
    private static String label(String term, int year) {
        return term.charAt(0) + term.substring(1).toLowerCase() + " " + year;
    }
}