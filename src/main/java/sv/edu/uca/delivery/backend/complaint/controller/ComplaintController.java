package sv.edu.uca.delivery.backend.complaint.controller;

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
import sv.edu.uca.delivery.backend.complaint.dto.ComplaintResponse;
import sv.edu.uca.delivery.backend.complaint.dto.CreateComplaintRequest;
import sv.edu.uca.delivery.backend.complaint.dto.UpdateComplaintStatusRequest;
import sv.edu.uca.delivery.backend.complaint.entity.ComplaintStatus;
import sv.edu.uca.delivery.backend.complaint.service.ComplaintService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComplaintResponse createComplaint(@Valid @RequestBody CreateComplaintRequest request) {
        return complaintService.createComplaint(request);
    }

    @GetMapping
    public List<ComplaintResponse> listComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) UUID orderId
    ) {
        return complaintService.listComplaints(status, orderId);
    }

    @GetMapping("/{id}")
    public ComplaintResponse getComplaint(@PathVariable UUID id) {
        return complaintService.getComplaint(id);
    }

    @PatchMapping("/{id}/status")
    public ComplaintResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateComplaintStatusRequest request
    ) {
        return complaintService.updateStatus(id, request);
    }
}
