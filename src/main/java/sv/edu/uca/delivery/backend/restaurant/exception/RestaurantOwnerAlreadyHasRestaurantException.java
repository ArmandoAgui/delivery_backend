package sv.edu.uca.delivery.backend.restaurant.exception;

public class RestaurantOwnerAlreadyHasRestaurantException extends RuntimeException {

    public RestaurantOwnerAlreadyHasRestaurantException() {
        super("Restaurant owner already has a restaurant");
    }
}
