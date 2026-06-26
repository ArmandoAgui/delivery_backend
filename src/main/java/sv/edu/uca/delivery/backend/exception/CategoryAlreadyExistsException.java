package sv.edu.uca.delivery.backend.exception;

public class CategoryAlreadyExistsException extends RuntimeException {

    public CategoryAlreadyExistsException() {
        super("Category already exists for this restaurant");
    }
}