package sv.edu.uca.delivery.backend.service;

import sv.edu.uca.delivery.backend.dto.UserResponse;
import sv.edu.uca.delivery.backend.dto.RegisterRequest;
import sv.edu.uca.delivery.backend.dto.UpdateUserRequest;

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
