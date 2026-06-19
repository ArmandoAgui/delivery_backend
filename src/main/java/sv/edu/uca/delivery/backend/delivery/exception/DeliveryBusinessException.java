package sv.edu.uca.delivery.backend.delivery.exception;

import org.springframework.http.HttpStatus;

public class DeliveryBusinessException extends DeliveryException {

    public DeliveryBusinessException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
