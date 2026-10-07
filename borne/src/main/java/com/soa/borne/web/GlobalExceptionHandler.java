package com.soa.borne.web;

import com.soa.borne.application.BorneNotFoundException;
import com.soa.borne.domain.InvalidBorneTransitionException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BorneNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse notFound(BorneNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidBorneTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse invalidTransition(InvalidBorneTransitionException exception,
                                           HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse badRequest(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ErrorResponse error(HttpStatus status, String message) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message);
    }
}
