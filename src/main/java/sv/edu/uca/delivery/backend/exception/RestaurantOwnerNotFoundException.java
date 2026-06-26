package sv.edu.uca.delivery.backend.exception;

public class RestaurantOwnerNotFoundException extends RuntimeException {

    public RestaurantOwnerNotFoundException() {
        super("Restaurant owner not found");
    }
}
