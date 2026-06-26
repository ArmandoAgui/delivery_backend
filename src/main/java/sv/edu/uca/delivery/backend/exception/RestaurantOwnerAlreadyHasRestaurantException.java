package sv.edu.uca.delivery.backend.exception;

public class RestaurantOwnerAlreadyHasRestaurantException extends RuntimeException {

    public RestaurantOwnerAlreadyHasRestaurantException() {
        super("Restaurant owner already has a restaurant");
    }
}
