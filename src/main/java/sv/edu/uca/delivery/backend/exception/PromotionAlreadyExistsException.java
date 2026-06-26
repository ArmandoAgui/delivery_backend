package sv.edu.uca.delivery.backend.exception;

public class PromotionAlreadyExistsException extends RuntimeException {

    public PromotionAlreadyExistsException() {
        super("Restaurant already has an active promotion");
    }
}