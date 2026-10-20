package com.degreeplanner.backend.web;

import com.degreeplanner.backend.dto.SaveStudentCourseRequest;
import com.degreeplanner.backend.dto.StudentCourseDto;
import com.degreeplanner.backend.security.CurrentUser;
import com.degreeplanner.backend.service.StudentCourseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/courses")
public class StudentCourseController {

    private final StudentCourseService service;

    public StudentCourseController(StudentCourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<StudentCourseDto> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(CurrentUser.id(jwt));
    }

    @PostMapping
    public StudentCourseDto save(@AuthenticationPrincipal Jwt jwt,
                                 @Valid @RequestBody SaveStudentCourseRequest request) {
        return service.save(CurrentUser.id(jwt), request);
    }

    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal Jwt jwt, @PathVariable Long courseId) {
        service.remove(CurrentUser.id(jwt), courseId);
    }
}