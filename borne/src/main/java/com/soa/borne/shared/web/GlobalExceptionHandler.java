package com.soa.borne.shared.web;

import com.soa.borne.borne.application.BorneNotFoundException;
import com.soa.borne.borne.domain.InvalidBorneTransitionException;
import com.soa.borne.shared.error.ReservationNotFoundException;
import com.soa.borne.shared.error.ShipNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({BorneNotFoundException.class, ShipNotFoundException.class, ReservationNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ErrorResponse notFound(BorneNotFoundException e) { return error(HttpStatus.NOT_FOUND, e.getMessage()); }
    @ExceptionHandler({IllegalStateException.class, InvalidBorneTransitionException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    ErrorResponse conflict(Exception e) { return error(HttpStatus.CONFLICT, e.getMessage()); }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse badRequest(Exception e) { return error(HttpStatus.BAD_REQUEST, "Invalid request: " + e.getMessage()); }
    private ErrorResponse error(HttpStatus status, String message) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message);
    }
}
