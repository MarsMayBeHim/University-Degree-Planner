package com.degreeplanner.backend.service;

import com.degreeplanner.backend.dto.EligibilityDto;
import com.degreeplanner.backend.model.Course;
import com.degreeplanner.backend.model.StudentCourse;
import com.degreeplanner.backend.planner.PrereqNode;
import com.degreeplanner.backend.planner.PrereqTreeParser;
import com.degreeplanner.backend.planner.PrerequisiteEvaluator;
import com.degreeplanner.backend.repository.CourseRepository;
import com.degreeplanner.backend.repository.StudentCourseRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EligibilityService {

    private final CourseRepository courses;
    private final StudentCourseRepository studentCourses;

    public EligibilityService(CourseRepository courses, StudentCourseRepository studentCourses) {
        this.courses = courses;
        this.studentCourses = studentCourses;
    }

    @Transactional(readOnly = true)
    public EligibilityDto check(Long userId, Long courseId) {
        Course course = courses.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Set<String> completed = completedCodes(userId);
        PrereqNode tree = PrereqTreeParser.parse(course.getPrereqTree());
        boolean eligible = PrerequisiteEvaluator.isSatisfied(tree, completed);
        List<String> unmet = eligible
                ? List.of()
                : List.copyOf(PrerequisiteEvaluator.unmetCodes(tree, completed));

        return new EligibilityDto(course.getId(), course.getSubject() + " " + course.getNumber(),
                eligible, course.getPrereqText(), unmet);
    }

    /** Only COMPLETED courses count as satisfying prerequisites. */
    public Set<String> completedCodes(Long userId) {
        List<Long> ids = studentCourses.findByUserId(userId).stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .map(StudentCourse::getCourseId)
                .toList();
        return courses.findAllById(ids).stream()
                .map(c -> c.getSubject() + " " + c.getNumber())
                .collect(Collectors.toSet());
    }
}