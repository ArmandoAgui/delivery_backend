package sv.edu.uca.delivery.backend.order.entity;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    WAITING_FOR_DRIVER,
    NO_DRIVER_AVAILABLE,
    PREPARING,
    READY_FOR_PICKUP,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}
