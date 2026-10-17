package com.degreeplanner.backend.service;

import com.degreeplanner.backend.dto.CourseDto;
import com.degreeplanner.backend.model.Course;
import com.degreeplanner.backend.repository.CourseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    public List<CourseDto> search(String q) {
        return repository.search(q.trim()).stream().map(this::toDto).toList();
    }

    public Optional<CourseDto> findById(Long id) {
        return repository.findById(id).map(this::toDto);
    }

    private CourseDto toDto(Course c) {
        return new CourseDto(c.getId(), c.getSubject() + " " + c.getNumber(),
                c.getTitle(), c.getCreditsMin(), c.getPrereqText());
    }
}