package sv.edu.uca.delivery.backend.order.entity;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    CREATED,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}
