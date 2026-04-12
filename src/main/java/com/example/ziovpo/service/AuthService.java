package com.example.ziovpo.service;

import com.example.ziovpo.config.JwtTokenUtils;
import com.example.ziovpo.model.SessionStatus;
import com.example.ziovpo.model.UserSession;
import com.example.ziovpo.model.Users;
import com.example.ziovpo.repository.UserSessionRepository;
import com.example.ziovpo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final UserSessionRepository sessionRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh.expiration.days}")
    private long refreshTokenExpirationDays;

    public record AuthResponse(String accessToken, String refreshToken) {}

    public AuthResponse register(Users user) {
        if (usersRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username taken");
        }
        validatePasswordStrength(user.getPassword());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) user.setRole("ROLE_USER");
        Users savedUser = usersRepository.save(user);

        return generateTokensAndSession(savedUser);
    }

    public AuthResponse login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        Users user = usersRepository.findByUsername(username).orElseThrow();
        return generateTokensAndSession(user);
    }

    public AuthResponse refresh(String oldRefreshToken) {
        UserSession session = sessionRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new NoSuchElementException("Session not found"));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new SecurityException("Token revoked or expired");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            sessionRepository.save(session);
            throw new SecurityException("Token expired");
        }

        session.setStatus(SessionStatus.REVOKED);
        sessionRepository.save(session);

        return generateTokensAndSession(session.getUser());
    }

    private AuthResponse generateTokensAndSession(Users user) {
        String accessToken = jwtTokenUtils.generateToken(user);
        String refreshToken = jwtTokenUtils.generateRefreshToken(user);

        UserSession session = UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .status(SessionStatus.ACTIVE)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS))
                .build();

        sessionRepository.save(session);

        return new AuthResponse(accessToken, refreshToken);
    }
    private void validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым!");
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException("Пароль должен быть не менее 8 символов!");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Пароль должен содержать хотя бы одну цифру!");
        }

        if (!password.matches(".*[a-zA-Z].*")) {
            throw new IllegalArgumentException("Пароль должен содержать хотя бы одну букву!");
        }
    }
}
