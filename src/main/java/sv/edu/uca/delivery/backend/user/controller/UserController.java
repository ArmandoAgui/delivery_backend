package sv.edu.uca.delivery.backend.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;
import sv.edu.uca.delivery.backend.user.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable UUID id
    ) {
        return userService.findById(id);
    }

    @GetMapping("/me/{id}")
    public UserResponse getProfile(
            @PathVariable UUID id
    ) {
        return userService.findById(id);
    }
}