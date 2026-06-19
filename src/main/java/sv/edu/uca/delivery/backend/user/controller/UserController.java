package sv.edu.uca.delivery.backend.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import sv.edu.uca.delivery.backend.common.exception.BusinessException;
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
@Tag(name = "Users", description = "Gestion de usuarios, perfil autenticado y operaciones administrativas.")
public class UserController {

    private final UserService userService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene todos los usuarios registrados.")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/page")
    @Operation(summary = "Listar usuarios paginados", description = "Obtiene usuarios usando paginacion en memoria compatible con el frontend.")
    public PageResponse<UserResponse> findAllPaged(Pageable pageable) {
        return PaginationUtils.toPage(userService.findAll(), pageable);
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil autenticado", description = "Obtiene los datos del usuario autenticado por JWT.")
    public UserResponse me() {
        return userService.findById(authenticatedUserProvider.getCurrentUserId());
    }

    @PutMapping("/me")
    @Operation(summary = "Actualizar perfil autenticado", description = "Actualiza nombre, telefono u otros datos editables del usuario autenticado.")
    public UserResponse updateMe(@RequestBody @Valid UpdateUserRequest request) {
        return userService.updateProfile(authenticatedUserProvider.getCurrentUserId(), request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear usuario", description = "Crea un usuario desde administracion o integraciones internas.")
    public UserResponse create(@RequestBody @Valid RegisterRequest request) {
        throw new BusinessException(HttpStatus.FORBIDDEN, "Admin users can only search, activate, or deactivate users");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar usuario", description = "Obtiene el detalle de un usuario por identificador.")
    public UserResponse getUserById(
            @PathVariable UUID id
    ) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza los datos editables de un usuario por identificador.")
    public UserResponse update(@PathVariable UUID id, @RequestBody @Valid UpdateUserRequest request) {
        throw new BusinessException(HttpStatus.FORBIDDEN, "Admin users can only search, activate, or deactivate users");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar usuario", description = "Desactiva logicamente un usuario sin borrar su historial.")
    public void deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar usuario", description = "Reactiva logicamente un usuario desactivado.")
    public UserResponse activate(@PathVariable UUID id) {
        return userService.activate(id);
    }
}
