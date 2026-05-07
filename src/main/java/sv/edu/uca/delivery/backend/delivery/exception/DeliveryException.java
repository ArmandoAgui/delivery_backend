package sv.edu.uca.delivery.backend.delivery.exception;

import org.springframework.http.HttpStatus;

public class DeliveryException extends RuntimeException {

    private final HttpStatus status;

    public DeliveryException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
