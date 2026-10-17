package com.degreeplanner.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "program")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "degree_type")
    private String degreeType;

    @Column(name = "catalog_year")
    private String catalogYear;

    @Column(name = "total_credits")
    private int totalCredits;

    protected Program() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDegreeType() { return degreeType; }
    public String getCatalogYear() { return catalogYear; }
    public int getTotalCredits() { return totalCredits; }
}