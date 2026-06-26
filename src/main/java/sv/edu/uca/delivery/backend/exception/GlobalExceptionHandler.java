package sv.edu.uca.delivery.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import sv.edu.uca.delivery.backend.exception.CategoryAlreadyExistsException;
import sv.edu.uca.delivery.backend.exception.CategoryNotFoundException;
import sv.edu.uca.delivery.backend.exception.ComplaintException;
import sv.edu.uca.delivery.backend.exception.DeliveryException;
import sv.edu.uca.delivery.backend.exception.ProductNotFoundException;
import sv.edu.uca.delivery.backend.exception.PromotionAlreadyExistsException;
import sv.edu.uca.delivery.backend.exception.PromotionDateInvalidException;
import sv.edu.uca.delivery.backend.exception.PromotionNotFoundException;
import sv.edu.uca.delivery.backend.exception.RestaurantNotFoundException;
import sv.edu.uca.delivery.backend.exception.RestaurantOwnerAlreadyHasRestaurantException;
import sv.edu.uca.delivery.backend.exception.RestaurantOwnerNotFoundException;
import sv.edu.uca.delivery.backend.exception.RestaurantScheduleInvalidException;
import sv.edu.uca.delivery.backend.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        return buildResponse(exception.getStatus(), exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(DeliveryException.class)
    ResponseEntity<ApiErrorResponse> handleDeliveryException(
            DeliveryException exception,
            HttpServletRequest request
    ) {
        return buildResponse(exception.getStatus(), exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(ComplaintException.class)
    ResponseEntity<ApiErrorResponse> handleComplaintException(
            ComplaintException exception,
            HttpServletRequest request
    ) {
        return buildResponse(exception.getStatus(), exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleRestaurantNotFoundException(
            RestaurantNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(RestaurantOwnerNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleRestaurantOwnerNotFoundException(
            RestaurantOwnerNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(RestaurantOwnerAlreadyHasRestaurantException.class)
    ResponseEntity<ApiErrorResponse> handleRestaurantOwnerAlreadyHasRestaurantException(
            RestaurantOwnerAlreadyHasRestaurantException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(RestaurantScheduleInvalidException.class)
    ResponseEntity<ApiErrorResponse> handleRestaurantScheduleInvalidException(
            RestaurantScheduleInvalidException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler({
            CategoryNotFoundException.class,
            ProductNotFoundException.class,
            PromotionNotFoundException.class,
            UserNotFoundException.class
    })
    ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler({
            CategoryAlreadyExistsException.class,
            PromotionAlreadyExistsException.class
    })
    ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(PromotionDateInvalidException.class)
    ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", request.getRequestURI(), List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Endpoint not found", request.getRequestURI(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request payload", request.getRequestURI(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<String> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request parameters", request.getRequestURI(), details);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error",
                request.getRequestURI(),
                List.of(exception.getClass().getSimpleName())
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path,
            List<String> details
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(response);
    }
}
