package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.SaveStudentCourseRequest;
import com.degreeplanner.backend.dto.StudentCourseDto;
import com.degreeplanner.backend.service.StudentCourseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/courses")
public class StudentCourseController {

    // Temporary: replaced by the logged-in user in Part 7
    private static final Long TEST_USER_ID = 1L;

    private final StudentCourseService service;

    public StudentCourseController(StudentCourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudentCourseDto> list() {
        return service.list(TEST_USER_ID);
    }

    @PostMapping
    public StudentCourseDto save(@Valid @RequestBody SaveStudentCourseRequest request) {
        return service.save(TEST_USER_ID, request);
    }

    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long courseId) {
        service.remove(TEST_USER_ID, courseId);
    }
}