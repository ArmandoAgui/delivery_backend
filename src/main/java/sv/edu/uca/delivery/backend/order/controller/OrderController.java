package sv.edu.uca.delivery.backend.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.common.pagination.PageResponse;
import sv.edu.uca.delivery.backend.common.pagination.PaginationUtils;
import sv.edu.uca.delivery.backend.order.dto.request.CreateOrderFromCartRequest;
import sv.edu.uca.delivery.backend.order.dto.response.OrderResponse;
import sv.edu.uca.delivery.backend.order.dto.response.OrderTrackingResponse;
import sv.edu.uca.delivery.backend.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Pedidos desde carrito, estados, historial y tracking REST.")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear pedido desde carrito")
    public OrderResponse create(@RequestBody @Valid CreateOrderFromCartRequest request) {
        return orderService.createFromCart(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar pedido por ID")
    public OrderResponse get(@PathVariable UUID id) {
        return orderService.get(id);
    }

    @GetMapping("/my-history")
    @Operation(summary = "Historial de pedidos del cliente")
    public List<OrderResponse> myHistory() {
        return orderService.myHistory();
    }

    @GetMapping("/my-history/page")
    public PageResponse<OrderResponse> myHistoryPaged(Pageable pageable) {
        return PaginationUtils.toPage(orderService.myHistory(), pageable);
    }

    @GetMapping("/restaurant")
    @Operation(summary = "Pedidos del restaurante propio")
    public List<OrderResponse> restaurantOrders() {
        return orderService.restaurantOrders();
    }

    @GetMapping("/restaurant/page")
    public PageResponse<OrderResponse> restaurantOrdersPaged(Pageable pageable) {
        return PaginationUtils.toPage(orderService.restaurantOrders(), pageable);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pedido", description = "Solo cliente propietario o ADMIN; permitido antes de confirmacion.")
    public OrderResponse cancel(@PathVariable UUID id) {
        return orderService.cancel(id);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirmar pedido", description = "Solo ADMIN o restaurante propietario.")
    public OrderResponse confirm(@PathVariable UUID id) {
        return orderService.confirm(id);
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Rechazar pedido", description = "Solo ADMIN o restaurante propietario.")
    public OrderResponse reject(@PathVariable UUID id) {
        return orderService.reject(id);
    }

    @GetMapping("/{id}/tracking")
    @Operation(summary = "Tracking REST del pedido", description = "Consulta periodica/polling. No usa WebSockets.")
    public OrderTrackingResponse tracking(@PathVariable UUID id) {
        return orderService.tracking(id);
    }

    @GetMapping("/{id}/invoice")
    @Operation(summary = "Descargar factura", description = "Genera una factura HTML descargable para pedidos del cliente.")
    public ResponseEntity<String> invoice(@PathVariable UUID id) {
        String invoice = orderService.invoiceHtml(id);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"factura-" + id + ".html\"")
                .body(invoice);
    }
}
