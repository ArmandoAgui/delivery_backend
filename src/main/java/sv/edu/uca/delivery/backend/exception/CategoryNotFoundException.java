package sv.edu.uca.delivery.backend.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException() {
        super("Category not found");
    }
}