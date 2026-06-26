package sv.edu.uca.delivery.backend.exception;

public class PromotionDateInvalidException extends RuntimeException {

    public PromotionDateInvalidException(String message) {
        super(message);
    }
}