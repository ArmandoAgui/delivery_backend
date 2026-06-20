package sv.edu.uca.delivery.backend.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.auth.dto.request.LoginRequest;
import sv.edu.uca.delivery.backend.auth.dto.response.LoginResponse;
import sv.edu.uca.delivery.backend.auth.service.AuthService;
import sv.edu.uca.delivery.backend.user.dto.request.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public void register(
            @RequestBody @Valid RegisterRequest request
    ) {
        authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody @Valid LoginRequest request
    ) {
        return authService.login(request);
    }
}