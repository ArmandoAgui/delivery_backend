package sv.edu.uca.delivery.backend.auth.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException() {
        super("Role not found");
    }
}