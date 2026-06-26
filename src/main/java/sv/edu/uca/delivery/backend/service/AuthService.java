package sv.edu.uca.delivery.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.uca.delivery.backend.dto.AuthResponse;
import sv.edu.uca.delivery.backend.dto.LoginRequest;
import sv.edu.uca.delivery.backend.dto.RefreshTokenRequest;
import sv.edu.uca.delivery.backend.entity.RefreshToken;
import sv.edu.uca.delivery.backend.entity.RoleName;
import sv.edu.uca.delivery.backend.repository.RefreshTokenRepository;
import sv.edu.uca.delivery.backend.repository.RoleRepository;
import sv.edu.uca.delivery.backend.exception.BusinessException;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.security.JwtService;
import sv.edu.uca.delivery.backend.dto.RegisterRequest;
import sv.edu.uca.delivery.backend.dto.UserResponse;
import sv.edu.uca.delivery.backend.entity.User;
import sv.edu.uca.delivery.backend.mapper.UserMapper;
import sv.edu.uca.delivery.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Value("${app.security.refresh-token-days}")
    private long refreshTokenDays;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email is already registered");
        }
        String phone = normalizeOptional(request.getPhone());
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Phone is already registered");
        }
        RoleName requestedRole = normalizeSelfRegistrationRole(request.getRole());
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Role does not exist")));
        return issue(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailWithRole(request.email())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return issue(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenAndRevokedAtIsNull(request.refreshToken())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setRevokedAt(LocalDateTime.now());
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        token.setRevokedAt(LocalDateTime.now());
        return issue(userRepository.findByIdWithRole(token.getUser().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "User no longer exists")));
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenAndRevokedAtIsNull(request.refreshToken())
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return userMapper.toResponse(userRepository.findByIdWithRole(authenticatedUserProvider.getCurrentUserId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Authenticated user does not exist")));
    }

    private AuthResponse issue(User user) {
        RefreshToken refresh = new RefreshToken();
        refresh.setUser(user);
        refresh.setToken(UUID.randomUUID().toString() + UUID.randomUUID());
        refresh.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenDays));
        refreshTokenRepository.save(refresh);
        return new AuthResponse(jwtService.createAccessToken(user), refresh.getToken(), userMapper.toResponse(user));
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private RoleName normalizeSelfRegistrationRole(RoleName role) {
        RoleName requestedRole = role == null ? RoleName.CUSTOMER : role;
        if (requestedRole == RoleName.ADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Admin users cannot self-register");
        }
        return requestedRole;
    }
}
