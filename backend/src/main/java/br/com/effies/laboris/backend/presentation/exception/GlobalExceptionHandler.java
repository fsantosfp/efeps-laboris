package br.com.effies.laboris.backend.presentation.exception;

import br.com.effies.laboris.backend.presentation.dto.ApiErrorDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handlerValidationErrors(
        MethodArgumentNotValidException ex, HttpServletRequest request){

        String message = ex.getBindingResult().getFieldError().getDefaultMessage();

        ApiErrorDto error = new ApiErrorDto(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            message,
            request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorDto> handlerBusinessRuleErrors(RuntimeException ex, HttpServletRequest request){
        ApiErrorDto error = new ApiErrorDto(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Business Rule Violation",
            ex.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handlerNotFoundErrors(EntityNotFoundException ex, HttpServletRequest request){
        ApiErrorDto error = new ApiErrorDto(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage(),
            request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    public ResponseEntity<ApiErrorDto> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request){
        ApiErrorDto error = new ApiErrorDto(
            Instant.now(),
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "Acesso Negado.",
            request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorDto> handleMethodValidation(HandlerMethodValidationException ex, HttpServletRequest request) {

        String message = ex.getValueResults().stream()
            .map(result -> {
                return result.getResolvableErrors().getFirst().getDefaultMessage();
            })
            .collect(Collectors.joining(", "));

        ApiErrorDto error = new ApiErrorDto(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            message,
            request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorDto> handleTypeMismatch(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("O parâmetro '%s' recebeu um valor inválido.", ex.getParameterName());

        ApiErrorDto error = new ApiErrorDto(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Parameter Type",
            message,
            request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String requiredType = (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "desconhecido";
        String message = String.format("O parâmetro '%s' recebeu um valor inválido. O tipo esperado é '%s'.", ex.getName(), requiredType);

        ApiErrorDto error = new ApiErrorDto(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Parameter Type",
            message,
            request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handlerGenericErrors(Exception ex, HttpServletRequest request){
        ApiErrorDto error = new ApiErrorDto(
            Instant.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "Ocorreu um erro inesperado no servidor.",
            request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
