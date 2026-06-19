package sv.edu.uca.delivery.backend.user.service;

import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;
import sv.edu.uca.delivery.backend.user.dto.request.RegisterRequest;
import sv.edu.uca.delivery.backend.user.dto.request.UpdateUserRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse findById(UUID id);

    UserResponse create(RegisterRequest request);

    UserResponse update(UUID id, UpdateUserRequest request);

    UserResponse updateProfile(UUID id, UpdateUserRequest request);

    void deactivate(UUID id);

    UserResponse activate(UUID id);
}
