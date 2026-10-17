package com.degreeplanner.backend.repository;

import com.degreeplanner.backend.model.StudentCourse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    List<StudentCourse> findByUserId(Long userId);
    Optional<StudentCourse> findByUserIdAndCourseId(Long userId, Long courseId);
    void deleteByUserIdAndCourseId(Long userId, Long courseId);
}