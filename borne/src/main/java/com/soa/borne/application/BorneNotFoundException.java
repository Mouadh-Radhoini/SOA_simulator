package com.soa.borne.application;

public class BorneNotFoundException extends RuntimeException {

    public BorneNotFoundException(String id) {
        super("Borne '" + id + "' was not found");
    }
}
