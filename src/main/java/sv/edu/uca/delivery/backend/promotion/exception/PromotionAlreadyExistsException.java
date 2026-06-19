package sv.edu.uca.delivery.backend.promotion.exception;

public class PromotionAlreadyExistsException extends RuntimeException {

    public PromotionAlreadyExistsException() {
        super("Restaurant already has an active promotion");
    }
}