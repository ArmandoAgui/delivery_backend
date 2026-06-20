package sv.edu.uca.delivery.backend.auth.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException() {
        super("User already exists");
    }
}