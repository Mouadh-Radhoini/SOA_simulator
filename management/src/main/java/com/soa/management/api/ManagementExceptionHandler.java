package com.soa.management.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class ManagementExceptionHandler {
    public record ErrorResponse(Instant timestamp, int status, String error, String message) {}
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse notFound(IllegalArgumentException exception) {
        return new ErrorResponse(Instant.now(), 404, "Not Found", exception.getMessage());
    }
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse conflict(IllegalStateException exception) {
        return new ErrorResponse(Instant.now(), 409, "Conflict", exception.getMessage());
    }
}
