package com.degreeplanner.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "program_id")
    private Long programId;

    protected AppUser() {}

    public Long getId() { return id; }
    public Long getProgramId() { return programId; }
}