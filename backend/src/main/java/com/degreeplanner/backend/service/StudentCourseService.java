package com.degreeplanner.backend.service;

import com.degreeplanner.backend.dto.SaveStudentCourseRequest;
import com.degreeplanner.backend.dto.StudentCourseDto;
import com.degreeplanner.backend.model.Course;
import com.degreeplanner.backend.model.StudentCourse;
import com.degreeplanner.backend.repository.CourseRepository;
import com.degreeplanner.backend.repository.StudentCourseRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentCourseService {

    private static final Set<String> TERMS = Set.of("FALL", "SPRING", "SUMMER");

    private final StudentCourseRepository studentCourses;
    private final CourseRepository courses;

    public StudentCourseService(StudentCourseRepository studentCourses, CourseRepository courses) {
        this.studentCourses = studentCourses;
        this.courses = courses;
    }

    @Transactional(readOnly = true)
    public List<StudentCourseDto> list(Long userId) {
        List<StudentCourse> rows = studentCourses.findByUserId(userId);
        Map<Long, Course> byId = courses
                .findAllById(rows.stream().map(StudentCourse::getCourseId).toList())
                .stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        return rows.stream()
                .map(r -> toDto(r, byId.get(r.getCourseId())))
                .sorted(Comparator.comparing(StudentCourseDto::code))
                .toList();
    }

    @Transactional
    public StudentCourseDto save(Long userId, SaveStudentCourseRequest req) {
        Course course = courses.findById(req.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (req.plannedTerm() != null && !TERMS.contains(req.plannedTerm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "plannedTerm must be FALL, SPRING or SUMMER");
        }
        if ("PLANNED".equals(req.status()) && (req.plannedYear() == null || req.plannedTerm() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PLANNED courses need plannedYear and plannedTerm");
        }

        StudentCourse row = studentCourses.findByUserIdAndCourseId(userId, req.courseId())
                .orElseGet(() -> new StudentCourse(userId, req.courseId()));
        row.setStatus(req.status());
        row.setPlannedYear(req.plannedYear());
        row.setPlannedTerm(req.plannedTerm());
        return toDto(studentCourses.save(row), course);
    }

    @Transactional
    public void remove(Long userId, Long courseId) {
        studentCourses.deleteByUserIdAndCourseId(userId, courseId);
    }

    private StudentCourseDto toDto(StudentCourse r, Course c) {
        return new StudentCourseDto(r.getCourseId(), c.getSubject() + " " + c.getNumber(),
                c.getTitle(), c.getCreditsMin(), r.getStatus(), r.getPlannedYear(), r.getPlannedTerm());
    }
}