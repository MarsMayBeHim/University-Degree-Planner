package com.degreeplanner.backend.repository;

import com.degreeplanner.backend.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Long> {}