package sv.edu.uca.delivery.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ComplaintException extends RuntimeException {

    private final HttpStatus status;

    public ComplaintException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
