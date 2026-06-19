package sv.edu.uca.delivery.backend.complaint.exception;

import org.springframework.http.HttpStatus;

public class ComplaintUnauthorizedException extends ComplaintException {

    public ComplaintUnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
