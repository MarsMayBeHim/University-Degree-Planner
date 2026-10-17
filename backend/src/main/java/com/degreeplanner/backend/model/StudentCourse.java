package com.degreeplanner.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_course")
public class StudentCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "course_id")
    private Long courseId;

    private String status;

    @Column(name = "planned_year")
    private Integer plannedYear;

    @Column(name = "planned_term")
    private String plannedTerm;

    protected StudentCourse() {}

    public StudentCourse(Long userId, Long courseId) {
        this.userId = userId;
        this.courseId = courseId;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getCourseId() { return courseId; }
    public String getStatus() { return status; }
    public Integer getPlannedYear() { return plannedYear; }
    public String getPlannedTerm() { return plannedTerm; }

    public void setStatus(String status) { this.status = status; }
    public void setPlannedYear(Integer plannedYear) { this.plannedYear = plannedYear; }
    public void setPlannedTerm(String plannedTerm) { this.plannedTerm = plannedTerm; }
}