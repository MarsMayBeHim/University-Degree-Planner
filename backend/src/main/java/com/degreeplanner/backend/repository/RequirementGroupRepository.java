package com.degreeplanner.backend.repository;

import com.degreeplanner.backend.model.RequirementGroup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequirementGroupRepository extends JpaRepository<RequirementGroup, Long> {
    List<RequirementGroup> findByProgramIdOrderBySortOrder(Long programId);
}