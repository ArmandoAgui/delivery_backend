package sv.edu.uca.delivery.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.dto.AuthResponse;
import sv.edu.uca.delivery.backend.dto.LoginRequest;
import sv.edu.uca.delivery.backend.dto.RefreshTokenRequest;
import sv.edu.uca.delivery.backend.service.AuthService;
import sv.edu.uca.delivery.backend.dto.RegisterRequest;
import sv.edu.uca.delivery.backend.dto.UserResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro, login, refresh token, logout y perfil autenticado.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar cliente", description = "Crea un usuario CUSTOMER y devuelve access token, refresh token y perfil.")
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Autentica email/password y devuelve JWT y refresh token.")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Rota el refresh token y emite un nuevo access token.")
    public AuthResponse refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cerrar sesion", description = "Revoca el refresh token actual.")
    public void logout(@RequestBody @Valid RefreshTokenRequest request) {
        authService.logout(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil autenticado", description = "Devuelve el usuario asociado al JWT actual.")
    public UserResponse me() {
        return authService.me();
    }
}
