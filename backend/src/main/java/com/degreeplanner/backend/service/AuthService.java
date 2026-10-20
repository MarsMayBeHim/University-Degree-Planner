package com.degreeplanner.backend.service;

import com.degreeplanner.backend.dto.AuthResponse;
import com.degreeplanner.backend.dto.LoginRequest;
import com.degreeplanner.backend.dto.ProfileRequest;
import com.degreeplanner.backend.dto.RegisterRequest;
import com.degreeplanner.backend.dto.UserDto;
import com.degreeplanner.backend.model.AppUser;
import com.degreeplanner.backend.repository.AppUserRepository;
import com.degreeplanner.backend.repository.ProgramRepository;
import com.degreeplanner.backend.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AppUserRepository users;
    private final ProgramRepository programs;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthService(AppUserRepository users, ProgramRepository programs,
                       PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.programs = programs;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        AppUser user = users.save(new AppUser(email, encoder.encode(req.password())));
        return new AuthResponse(tokens.create(user), UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        AppUser user = users.findByEmailIgnoreCase(req.email().trim()).orElse(null);
        if (user == null || !encoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return new AuthResponse(tokens.create(user), UserDto.from(user));
    }

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        return UserDto.from(find(userId));
    }

    @Transactional
    public UserDto updateProfile(Long userId, ProfileRequest req) {
        if (!programs.existsById(req.programId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Program not found");
        }
        AppUser user = find(userId);
        user.setProgramId(req.programId());
        user.setStartYear(req.startYear());
        user.setStartTerm(req.startTerm());
        return UserDto.from(user);
    }

    private AppUser find(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }
}