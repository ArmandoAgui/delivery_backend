package sv.edu.uca.delivery.backend.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.payment.dto.PaypalCreateOrderRequest;
import sv.edu.uca.delivery.backend.payment.dto.PaypalOrderResponse;
import sv.edu.uca.delivery.backend.payment.service.PaypalPaymentService;

@RestController
@RequestMapping("/api/payments/paypal")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Integraciones de pago. PayPal es opcional y requiere credenciales sandbox/live.")
public class PaymentController {

    private final PaypalPaymentService paypalPaymentService;

    @PostMapping("/orders")
    @Operation(summary = "Crear orden PayPal", description = "Crea una orden PayPal por el monto calculado del checkout.")
    public PaypalOrderResponse createPaypalOrder(@RequestBody @Valid PaypalCreateOrderRequest request) {
        return paypalPaymentService.createOrder(request);
    }

    @PostMapping("/orders/{paypalOrderId}/capture")
    @Operation(summary = "Capturar orden PayPal", description = "Captura una orden PayPal previamente aprobada por el cliente.")
    public PaypalOrderResponse capturePaypalOrder(@PathVariable String paypalOrderId) {
        return paypalPaymentService.captureOrder(paypalOrderId);
    }
}
