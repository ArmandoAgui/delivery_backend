package sv.edu.uca.delivery.backend.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.review.dto.CreateReviewRequest;
import sv.edu.uca.delivery.backend.review.dto.ReviewResponse;
import sv.edu.uca.delivery.backend.review.service.ReviewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Calificaciones de restaurantes y repartidores sobre pedidos entregados.")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear calificacion", description = "Registra una calificacion asociada a un pedido entregado.")
    public ReviewResponse create(@RequestBody @Valid CreateReviewRequest request) {
        return reviewService.create(request);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Calificaciones por restaurante", description = "Lista reviews publicas de un restaurante.")
    public List<ReviewResponse> byRestaurant(@PathVariable UUID restaurantId) {
        return reviewService.byRestaurant(restaurantId);
    }

    @GetMapping("/delivery/{deliveryUserId}")
    @Operation(summary = "Calificaciones por repartidor", description = "Lista reviews asociadas a entregas de un repartidor.")
    public List<ReviewResponse> byDeliveryUser(@PathVariable UUID deliveryUserId) {
        return reviewService.byDeliveryUser(deliveryUserId);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Calificaciones por producto", description = "Lista reviews publicas de un platillo o producto.")
    public List<ReviewResponse> byProduct(@PathVariable UUID productId) {
        return reviewService.byProduct(productId);
    }
}
