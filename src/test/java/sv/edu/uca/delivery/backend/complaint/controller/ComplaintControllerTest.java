package sv.edu.uca.delivery.backend.complaint.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sv.edu.uca.delivery.backend.common.exception.GlobalExceptionHandler;
import sv.edu.uca.delivery.backend.complaint.dto.ComplaintResponse;
import sv.edu.uca.delivery.backend.complaint.dto.CreateComplaintRequest;
import sv.edu.uca.delivery.backend.complaint.dto.UpdateComplaintStatusRequest;
import sv.edu.uca.delivery.backend.complaint.entity.ComplaintStatus;
import sv.edu.uca.delivery.backend.complaint.service.ComplaintService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ComplaintControllerTest {

    private static final UUID COMPLAINT_ID = UUID.fromString("018f0000-0000-7000-8000-000000000020");
    private static final UUID ORDER_ID = UUID.fromString("018f0000-0000-7000-8000-000000000010");
    private static final UUID CUSTOMER_ID = UUID.fromString("018f0000-0000-7000-8000-000000000003");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ComplaintService complaintService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        complaintService = mock(ComplaintService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ComplaintController(complaintService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void postCreatesComplaint() throws Exception {
        var request = new CreateComplaintRequest(ORDER_ID, "Missing item", "A drink was missing");
        when(complaintService.createComplaint(request)).thenReturn(response(ComplaintStatus.OPEN));

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(COMPLAINT_ID.toString()))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void getListsComplaints() throws Exception {
        when(complaintService.listComplaints(ComplaintStatus.OPEN, ORDER_ID))
                .thenReturn(List.of(response(ComplaintStatus.OPEN)));

        mockMvc.perform(get("/api/complaints")
                        .param("status", "OPEN")
                        .param("orderId", ORDER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(ORDER_ID.toString()));
    }

    @Test
    void patchUpdatesComplaintStatus() throws Exception {
        var request = new UpdateComplaintStatusRequest(ComplaintStatus.IN_PROGRESS);
        when(complaintService.updateStatus(COMPLAINT_ID, request))
                .thenReturn(response(ComplaintStatus.IN_PROGRESS));

        mockMvc.perform(patch("/api/complaints/{id}/status", COMPLAINT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    private ComplaintResponse response(ComplaintStatus status) {
        return new ComplaintResponse(
                COMPLAINT_ID,
                ORDER_ID,
                CUSTOMER_ID,
                status,
                "Missing item",
                "A drink was missing",
                null,
                null,
                null
        );
    }
}
