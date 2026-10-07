package com.ghost.sacred_library.exception;

import com.ghost.sacred_library.dto.ErrorResponse;
import com.ghost.sacred_library.service.AuthService.DuplicateAccountException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    org.springframework.http.ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) details.putIfAbsent(error.getField(), error.getDefaultMessage());
        return response(HttpStatus.BAD_REQUEST, "Request validation failed.", details);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    org.springframework.http.ResponseEntity<ErrorResponse> badRequest(Exception ex) {
        String message = ex instanceof IllegalArgumentException ? ex.getMessage() : "Request body or parameters are invalid.";
        return response(HttpStatus.BAD_REQUEST, message, Map.of());
    }
    @ExceptionHandler(DuplicateAccountException.class)
    org.springframework.http.ResponseEntity<ErrorResponse> duplicate(DuplicateAccountException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    org.springframework.http.ResponseEntity<ErrorResponse> constraint(DataIntegrityViolationException ex) {
        return response(HttpStatus.CONFLICT, "Username, email, or role already exists.", Map.of());
    }
    @ExceptionHandler(BadCredentialsException.class)
    org.springframework.http.ResponseEntity<ErrorResponse> badCredentials(BadCredentialsException ex) {
        return response(HttpStatus.UNAUTHORIZED, "Username/email or password is incorrect.", Map.of());
    }
    private org.springframework.http.ResponseEntity<ErrorResponse> response(HttpStatus status, String message, Map<String, String> details) {
        return org.springframework.http.ResponseEntity.status(status).body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, details));
    }
}
