package sv.edu.uca.delivery.backend.auth.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("Email already exists");
    }
}