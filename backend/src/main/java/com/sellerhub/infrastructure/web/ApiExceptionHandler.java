package com.sellerhub.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> notFound(NoSuchElementException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, Object>> badRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, exception instanceof MethodArgumentNotValidException
                ? "La respuesta es obligatoria y debe tener menos de 1000 caracteres" : exception.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", message,
                "timestamp", Instant.now().toString()));
    }
}
