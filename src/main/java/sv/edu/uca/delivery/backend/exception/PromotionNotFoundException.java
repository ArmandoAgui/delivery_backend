package sv.edu.uca.delivery.backend.exception;

public class PromotionNotFoundException extends RuntimeException {

    public PromotionNotFoundException() {
        super("Promotion not found");
    }
}