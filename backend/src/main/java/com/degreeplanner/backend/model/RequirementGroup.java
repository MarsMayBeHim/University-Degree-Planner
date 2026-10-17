package com.degreeplanner.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "requirement_group")
public class RequirementGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "program_id")
    private Long programId;

    @Column(name = "parent_id")
    private Long parentId;

    private String name;

    @Column(name = "rule_type")
    private String ruleType;

    @Column(name = "min_courses")
    private Integer minCourses;

    @Column(name = "min_credits")
    private BigDecimal minCredits;

    @Column(name = "sort_order")
    private int sortOrder;

    @ManyToMany
    @JoinTable(name = "requirement_group_course",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses = new HashSet<>();

    protected RequirementGroup() {}

    public Long getId() { return id; }
    public Long getProgramId() { return programId; }
    public Long getParentId() { return parentId; }
    public String getName() { return name; }
    public String getRuleType() { return ruleType; }
    public Integer getMinCourses() { return minCourses; }
    public BigDecimal getMinCredits() { return minCredits; }
    public Set<Course> getCourses() { return courses; }
}