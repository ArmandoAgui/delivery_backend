package sv.edu.uca.delivery.backend.exception;

import org.springframework.http.HttpStatus;

public class DeliveryNotFoundException extends DeliveryException {

    public DeliveryNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
