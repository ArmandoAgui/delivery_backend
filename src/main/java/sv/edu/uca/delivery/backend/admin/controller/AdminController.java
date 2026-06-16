package sv.edu.uca.delivery.backend.admin.controller;

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
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/commissions")
    @ResponseStatus(HttpStatus.CREATED)
    public CommissionResponse createCommission(@RequestBody @Valid CommissionRequest request) {
        return adminService.createCommission(request);
    }

    @GetMapping("/commissions")
    public List<CommissionResponse> listCommissions() {
        return adminService.listCommissions();
    }
}
