package sv.edu.uca.delivery.backend.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sv.edu.uca.delivery.backend.auth.dto.request.LoginRequest;
import sv.edu.uca.delivery.backend.auth.dto.response.LoginResponse;
import sv.edu.uca.delivery.backend.auth.entity.Role;
import sv.edu.uca.delivery.backend.auth.entity.RoleName;
import sv.edu.uca.delivery.backend.auth.exception.InvalidCredentialsException;
import sv.edu.uca.delivery.backend.auth.repository.RoleRepository;
import sv.edu.uca.delivery.backend.auth.service.AuthService;
import sv.edu.uca.delivery.backend.security.jwt.JwtService;
import sv.edu.uca.delivery.backend.user.dto.request.RegisterRequest;
import sv.edu.uca.delivery.backend.user.entity.User;
import sv.edu.uca.delivery.backend.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findByName(
                request.getRole()
        ).orElseThrow(() ->
                new RuntimeException("Role not found")
        );

        User user = new User();

        user.setFirstName(
                request.getFirstName()
        );

        user.setLastName(
                request.getLastName()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setPhone(
                request.getPhone()
        );

        user.setRole(role);

        user.setActive(true);

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow(
                InvalidCredentialsException::new
        );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        String token =
                jwtService.generateToken(user);

        return new LoginResponse(
                token,
                user.getRole()
                        .getName()
                        .name(),
                user.getEmail()
        );
    }
}