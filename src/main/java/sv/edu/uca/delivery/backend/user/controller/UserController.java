package sv.edu.uca.delivery.backend.user.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;
import sv.edu.uca.delivery.backend.common.pagination.PageResponse;
import sv.edu.uca.delivery.backend.common.pagination.PaginationUtils;
import sv.edu.uca.delivery.backend.security.AuthenticatedUserProvider;
import sv.edu.uca.delivery.backend.user.dto.request.RegisterRequest;
import sv.edu.uca.delivery.backend.user.dto.request.UpdateUserRequest;
import sv.edu.uca.delivery.backend.user.dto.response.UserResponse;
import sv.edu.uca.delivery.backend.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/page")
    public PageResponse<UserResponse> findAllPaged(Pageable pageable) {
        return PaginationUtils.toPage(userService.findAll(), pageable);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return userService.findById(authenticatedUserProvider.getCurrentUserId());
    }

    @PutMapping("/me")
    public UserResponse updateMe(@RequestBody @Valid UpdateUserRequest request) {
        return userService.update(authenticatedUserProvider.getCurrentUserId(), request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody @Valid RegisterRequest request) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable UUID id
    ) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
    }
}
