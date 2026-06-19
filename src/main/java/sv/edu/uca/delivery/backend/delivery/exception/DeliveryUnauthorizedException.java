package sv.edu.uca.delivery.backend.delivery.exception;

import org.springframework.http.HttpStatus;

public class DeliveryUnauthorizedException extends DeliveryException {

    public DeliveryUnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
