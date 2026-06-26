package sv.edu.uca.delivery.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.dto.ComplaintResponse;
import sv.edu.uca.delivery.backend.dto.CreateComplaintRequest;
import sv.edu.uca.delivery.backend.dto.UpdateComplaintStatusRequest;
import sv.edu.uca.delivery.backend.entity.ComplaintStatus;
import sv.edu.uca.delivery.backend.service.ComplaintService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Reclamos de clientes, consulta administrativa y resolucion de estados.")
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear reclamo", description = "Permite al cliente autenticado crear un reclamo sobre un pedido valido.")
    public ComplaintResponse createComplaint(@Valid @RequestBody CreateComplaintRequest request) {
        return complaintService.createComplaint(request);
    }

    @GetMapping
    @Operation(summary = "Listar reclamos", description = "Lista reclamos con filtros opcionales por estado y pedido.")
    public List<ComplaintResponse> listComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) UUID orderId
    ) {
        return complaintService.listComplaints(status, orderId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar reclamo", description = "Obtiene el detalle de un reclamo por identificador.")
    public ComplaintResponse getComplaint(@PathVariable UUID id) {
        return complaintService.getComplaint(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de reclamo", description = "Permite avanzar el flujo administrativo del reclamo.")
    public ComplaintResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateComplaintStatusRequest request
    ) {
        return complaintService.updateStatus(id, request);
    }
}
