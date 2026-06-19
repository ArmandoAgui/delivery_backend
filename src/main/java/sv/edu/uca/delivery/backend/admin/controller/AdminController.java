package sv.edu.uca.delivery.backend.admin.controller;

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
import sv.edu.uca.delivery.backend.admin.dto.CommissionRequest;
import sv.edu.uca.delivery.backend.admin.dto.CommissionResponse;
import sv.edu.uca.delivery.backend.admin.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Configuracion administrativa protegida para ADMIN.")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/commissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear configuracion de comision")
    public CommissionResponse createCommission(@RequestBody @Valid CommissionRequest request) {
        return adminService.createCommission(request);
    }

    @GetMapping("/commissions")
    @Operation(summary = "Listar comisiones configuradas")
    public List<CommissionResponse> listCommissions() {
        return adminService.listCommissions();
    }
}
