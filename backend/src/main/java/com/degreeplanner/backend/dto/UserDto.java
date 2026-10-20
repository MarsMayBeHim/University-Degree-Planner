package com.degreeplanner.backend.dto;

import com.degreeplanner.backend.model.AppUser;

public record UserDto(Long id, String email, Long programId, Integer startYear, String startTerm) {

    public static UserDto from(AppUser u) {
        return new UserDto(u.getId(), u.getEmail(), u.getProgramId(), u.getStartYear(), u.getStartTerm());
    }
}