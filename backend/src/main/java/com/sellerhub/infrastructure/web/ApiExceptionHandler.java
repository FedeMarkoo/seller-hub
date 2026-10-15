package com.sellerhub.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import com.sellerhub.infrastructure.security.ForbiddenException;
import com.sellerhub.infrastructure.security.UnauthorizedException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> notFound(NoSuchElementException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, Object>> badRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, exception instanceof MethodArgumentNotValidException
                ? "Los datos enviados no son válidos" : exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> unauthorized() {
        return error(HttpStatus.UNAUTHORIZED, "Sesión requerida");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> forbidden() {
        return error(HttpStatus.FORBIDDEN, "No tenés permisos para este recurso");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> missingHeader() {
        return error(HttpStatus.UNAUTHORIZED, "Sesión requerida");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalidArgument(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", message,
                "timestamp", Instant.now().toString()));
    }
}
