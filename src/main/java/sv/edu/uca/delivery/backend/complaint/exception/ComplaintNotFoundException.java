package sv.edu.uca.delivery.backend.complaint.exception;

import org.springframework.http.HttpStatus;

public class ComplaintNotFoundException extends ComplaintException {

    public ComplaintNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
