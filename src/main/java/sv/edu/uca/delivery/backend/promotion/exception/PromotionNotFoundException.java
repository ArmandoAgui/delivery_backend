package sv.edu.uca.delivery.backend.promotion.exception;

public class PromotionNotFoundException extends RuntimeException {

    public PromotionNotFoundException() {
        super("Promotion not found");
    }
}