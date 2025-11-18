package com.pridebank.token.exception;

import com.pridebank.token.dto.AuthResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handle authentication failures
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<AuthResponse> handleAuthenticationFailed(
            AuthenticationFailedException ex) {
        log.error("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.builder()
                        .message(ex.getMessage())
                        .build());
    }

    /**
     * Handle Feign client errors
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<AuthResponse> handleFeignException(FeignException ex) {
        log.error("Feign client error: {}", ex.getMessage());

        HttpStatus status = HttpStatus.valueOf(ex.status());

        return ResponseEntity.status(status)
                .body(AuthResponse.builder()
                        .message("External service error: " + ex.getMessage())
                        .build());
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AuthResponse.builder()
                        .message("An unexpected error occurred")
                        .build());
    }
}