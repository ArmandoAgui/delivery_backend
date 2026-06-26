package sv.edu.uca.delivery.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.uca.delivery.backend.dto.AddCartItemRequest;
import sv.edu.uca.delivery.backend.dto.CartResponse;
import sv.edu.uca.delivery.backend.dto.UpdateCartItemRequest;
import sv.edu.uca.delivery.backend.service.CartService;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Carrito activo del cliente y calculo de subtotal.")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Consultar carrito activo")
    public CartResponse getCart() {
        return cartService.getCart();
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agregar producto al carrito")
    public CartResponse addItem(@RequestBody @Valid AddCartItemRequest request) {
        return cartService.addItem(request);
    }

    @PatchMapping("/items/{id}")
    @Operation(summary = "Actualizar cantidad de item")
    public CartResponse updateItem(@PathVariable UUID id, @RequestBody @Valid UpdateCartItemRequest request) {
        return cartService.updateItem(id, request);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar item del carrito")
    public void removeItem(@PathVariable UUID id) {
        cartService.removeItem(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Vaciar carrito")
    public void clearCart() {
        cartService.clearCart();
    }
}
