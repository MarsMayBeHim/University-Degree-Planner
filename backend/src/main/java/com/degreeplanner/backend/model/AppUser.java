package com.degreeplanner.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "program_id")
    private Long programId;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "start_term")
    private String startTerm;

    protected AppUser() {}

    public AppUser(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Long getProgramId() { return programId; }
    public Integer getStartYear() { return startYear; }
    public String getStartTerm() { return startTerm; }

    public void setProgramId(Long programId) { this.programId = programId; }
    public void setStartYear(Integer startYear) { this.startYear = startYear; }
    public void setStartTerm(String startTerm) { this.startTerm = startTerm; }
}