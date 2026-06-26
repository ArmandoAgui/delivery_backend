package sv.edu.uca.delivery.backend.controller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sv.edu.uca.delivery.backend.dto.AssignDeliveryRequest;
import sv.edu.uca.delivery.backend.dto.DeliveryResponse;
import sv.edu.uca.delivery.backend.dto.UpdateDeliveryStatusRequest;
import sv.edu.uca.delivery.backend.entity.DeliveryStatus;
import sv.edu.uca.delivery.backend.service.DeliveryService;
import sv.edu.uca.delivery.backend.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeliveryController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryService deliveryService;

    @Test
    void assignDeliveryReturnsCreatedResponse() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        UUID deliveryUserId = UUID.randomUUID();
        DeliveryResponse response = response(assignmentId, orderId, deliveryUserId, DeliveryStatus.ASSIGNED);

        when(deliveryService.assignDelivery(any(AssignDeliveryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/deliveries/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s"}
                                """.formatted(orderId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(assignmentId.toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.deliveryUserId").value(deliveryUserId.toString()))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        verify(deliveryService).assignDelivery(new AssignDeliveryRequest(orderId));
    }

    @Test
    void assignDeliveryReturnsBadRequestWhenOrderIdIsMissing() throws Exception {
        mockMvc.perform(post("/api/deliveries/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request payload"));

        verify(deliveryService, never()).assignDelivery(any());
    }

    @Test
    void getMyOrdersReturnsAssignedOrders() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID deliveryUserId = UUID.randomUUID();

        when(deliveryService.getMyOrders()).thenReturn(List.of(
                response(assignmentId, orderId, deliveryUserId, DeliveryStatus.PICKED_UP)
        ));

        mockMvc.perform(get("/api/deliveries/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(assignmentId.toString()))
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$[0].deliveryUserId").value(deliveryUserId.toString()))
                .andExpect(jsonPath("$[0].status").value("PICKED_UP"));

        verify(deliveryService).getMyOrders();
    }

    @Test
    void updateStatusReturnsUpdatedDelivery() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID deliveryUserId = UUID.randomUUID();
        DeliveryResponse response = response(assignmentId, orderId, deliveryUserId, DeliveryStatus.ON_THE_WAY);

        when(deliveryService.updateStatus(eq(assignmentId), any(UpdateDeliveryStatusRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/deliveries/{id}/status", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ON_THE_WAY"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(assignmentId.toString()))
                .andExpect(jsonPath("$.status").value("ON_THE_WAY"));

        verify(deliveryService).updateStatus(
                assignmentId,
                new UpdateDeliveryStatusRequest(DeliveryStatus.ON_THE_WAY)
        );
    }

    @Test
    void updateStatusReturnsBadRequestWhenStatusIsMissing() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/deliveries/{id}/status", assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request payload"));

        verify(deliveryService, never()).updateStatus(any(), any());
    }

    private DeliveryResponse response(
            UUID assignmentId,
            UUID orderId,
            UUID deliveryUserId,
            DeliveryStatus status
    ) {
        return new DeliveryResponse(
                assignmentId,
                orderId,
                deliveryUserId,
                "Repartidor Demo",
                status,
                OrderStatus.READY_FOR_PICKUP,
                LocalDateTime.of(2026, 5, 8, 18, 0),
                null,
                null,
                LocalDateTime.of(2026, 5, 8, 18, 0)
        );
    }
}
