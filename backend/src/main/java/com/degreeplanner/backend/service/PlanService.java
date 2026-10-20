package com.degreeplanner.backend.service;

import com.degreeplanner.backend.dto.PlanDto;
import com.degreeplanner.backend.dto.PlannedCourseDto;
import com.degreeplanner.backend.dto.SemesterDto;
import com.degreeplanner.backend.model.AppUser;
import com.degreeplanner.backend.model.Course;
import com.degreeplanner.backend.model.StudentCourse;
import com.degreeplanner.backend.planner.PrereqNode;
import com.degreeplanner.backend.planner.PrereqTreeParser;
import com.degreeplanner.backend.planner.PrerequisiteEvaluator;
import com.degreeplanner.backend.repository.AppUserRepository;
import com.degreeplanner.backend.repository.CourseRepository;
import com.degreeplanner.backend.repository.StudentCourseRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlanService {

    private static final int SEMESTER_COUNT = 8;

    private final AppUserRepository users;
    private final StudentCourseRepository studentCourses;
    private final CourseRepository courses;

    public PlanService(AppUserRepository users, StudentCourseRepository studentCourses,
                       CourseRepository courses) {
        this.users = users;
        this.studentCourses = studentCourses;
        this.courses = courses;
    }

    @Transactional(readOnly = true)
    public PlanDto plan(Long userId) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getStartYear() == null || user.getStartTerm() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Set your start term first");
        }

        List<StudentCourse> rows = studentCourses.findByUserId(userId);
        Map<Long, Course> byId = courses
                .findAllById(rows.stream().map(StudentCourse::getCourseId).toList())
                .stream()
                .collect(Collectors.toMap(Course::getId, c -> c));

        // Completed and in-progress courses are available before the first semester
        Set<String> available = new HashSet<>();
        for (StudentCourse row : rows) {
            if ("COMPLETED".equals(row.getStatus()) || "IN_PROGRESS".equals(row.getStatus())) {
                available.add(code(byId.get(row.getCourseId())));
            }
        }

        int year = user.getStartYear();
        String term = "SPRING".equals(user.getStartTerm()) ? "SPRING" : "FALL";
        List<SemesterDto> semesters = new ArrayList<>();

        for (int i = 0; i < SEMESTER_COUNT; i++) {
            final int semYear = year;
            final String semTerm = term;

            List<Course> inSemester = rows.stream()
                    .filter(r -> "PLANNED".equals(r.getStatus())
                            && Objects.equals(r.getPlannedYear(), semYear)
                            && semTerm.equals(r.getPlannedTerm()))
                    .map(r -> byId.get(r.getCourseId()))
                    .sorted(Comparator.comparing(Course::getSubject).thenComparing(Course::getNumber))
                    .toList();

            BigDecimal credits = BigDecimal.ZERO;
            List<PlannedCourseDto> planned = new ArrayList<>();
            for (Course c : inSemester) {
                PrereqNode tree = PrereqTreeParser.parse(c.getPrereqTree());
                List<String> missing = PrerequisiteEvaluator.isSatisfied(tree, available)
                        ? List.of()
                        : List.copyOf(PrerequisiteEvaluator.unmetCodes(tree, available));
                planned.add(new PlannedCourseDto(c.getId(), code(c), c.getTitle(),
                        c.getCreditsMin(), c.getPrereqText(), missing));
                credits = credits.add(c.getCreditsMin());
            }

            // Courses count as prerequisites only for LATER semesters
            inSemester.forEach(c -> available.add(code(c)));
            semesters.add(new SemesterDto(semYear, semTerm, label(semTerm, semYear), credits, planned));

            if ("FALL".equals(term)) {
                term = "SPRING";
                year++;
            } else {
                term = "FALL";
            }
        }
        return new PlanDto(user.getStartYear(), user.getStartTerm(), semesters);
    }

    private static String code(Course c) {
        return c.getSubject() + " " + c.getNumber();
    }

    private static String label(String term, int year) {
        return term.charAt(0) + term.substring(1).toLowerCase() + " " + year;
    }
}