package sv.edu.uca.delivery.backend.auth.service;

import sv.edu.uca.delivery.backend.auth.dto.request.LoginRequest;
import sv.edu.uca.delivery.backend.auth.dto.response.LoginResponse;
import sv.edu.uca.delivery.backend.user.dto.request.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}