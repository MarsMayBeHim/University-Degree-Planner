package com.degreeplanner.backend.repository;

import com.degreeplanner.backend.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {}