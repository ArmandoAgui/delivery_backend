package sv.edu.uca.delivery.backend.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody @Valid CreateOrderFromCartRequest request) {
        return orderService.createFromCart(request);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return orderService.get(id);
    }

    @GetMapping("/my-history")
    public List<OrderResponse> myHistory() {
        return orderService.myHistory();
    }

    @GetMapping("/my-history/page")
    public PageResponse<OrderResponse> myHistoryPaged(Pageable pageable) {
        return PaginationUtils.toPage(orderService.myHistory(), pageable);
    }

    @GetMapping("/restaurant")
    public List<OrderResponse> restaurantOrders() {
        return orderService.restaurantOrders();
    }

    @GetMapping("/restaurant/page")
    public PageResponse<OrderResponse> restaurantOrdersPaged(Pageable pageable) {
        return PaginationUtils.toPage(orderService.restaurantOrders(), pageable);
    }

    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable UUID id) {
        return orderService.cancel(id);
    }

    @PatchMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable UUID id) {
        return orderService.confirm(id);
    }

    @PatchMapping("/{id}/reject")
    public OrderResponse reject(@PathVariable UUID id) {
        return orderService.reject(id);
    }

    @GetMapping("/{id}/tracking")
    public OrderTrackingResponse tracking(@PathVariable UUID id) {
        return orderService.tracking(id);
    }
}
