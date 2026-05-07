package sv.edu.uca.delivery.backend.delivery.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "sv.edu.uca.delivery.backend.delivery")
public class DeliveryExceptionHandler {

    @ExceptionHandler(DeliveryException.class)
    public ResponseEntity<DeliveryErrorResponse> handleDeliveryException(
            DeliveryException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(exception.getStatus())
                .body(error(exception.getStatus(), exception.getMessage(), request.getRequestURI(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DeliveryErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(error(HttpStatus.BAD_REQUEST, "Invalid request data", request.getRequestURI(), errors));
    }

    private DeliveryErrorResponse error(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        return DeliveryErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }
}
