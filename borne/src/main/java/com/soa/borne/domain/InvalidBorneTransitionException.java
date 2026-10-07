package com.soa.borne.domain;

public class InvalidBorneTransitionException extends RuntimeException {

    public InvalidBorneTransitionException(String message) {
        super(message);
    }
}
