package sv.edu.uca.delivery.backend.restaurant.exception;

public class RestaurantOwnerNotFoundException extends RuntimeException {

    public RestaurantOwnerNotFoundException() {
        super("Restaurant owner not found");
    }
}
