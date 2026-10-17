package com.degreeplanner.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    private String number;
    private String title;

    @Column(name = "credits_min")
    private BigDecimal creditsMin;

    @Column(name = "prereq_text")
    private String prereqText;

    protected Course() {}

    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getNumber() { return number; }
    public String getTitle() { return title; }
    public BigDecimal getCreditsMin() { return creditsMin; }
    public String getPrereqText() { return prereqText; }
}