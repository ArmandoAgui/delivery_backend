package sv.edu.uca.delivery.backend.payment.dto;

public record PaypalOrderResponse(
        String id,
        String status,
        String approvalUrl
) {
}
