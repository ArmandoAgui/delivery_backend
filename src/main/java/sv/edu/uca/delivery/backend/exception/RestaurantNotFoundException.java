package sv.edu.uca.delivery.backend.exception;

public class RestaurantNotFoundException extends RuntimeException {

    public RestaurantNotFoundException() {
        super("Restaurant not found");
    }
}