package sv.edu.uca.delivery.backend.user.service;

import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;
import sv.edu.uca.delivery.backend.user.dto.request.RegisterRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse findById(UUID id);

    UserResponse create(RegisterRequest request);

    UserResponse update(UUID id, RegisterRequest request);

    void deactivate(UUID id);
}
