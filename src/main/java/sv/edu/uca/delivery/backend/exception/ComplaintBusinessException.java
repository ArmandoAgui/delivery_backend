package sv.edu.uca.delivery.backend.exception;

import org.springframework.http.HttpStatus;

public class ComplaintBusinessException extends ComplaintException {

    public ComplaintBusinessException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
