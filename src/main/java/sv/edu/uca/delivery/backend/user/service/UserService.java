package sv.edu.uca.delivery.backend.user.service;

import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse findById(UUID id);
}