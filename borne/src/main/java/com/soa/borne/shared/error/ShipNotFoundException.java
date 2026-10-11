package com.soa.borne.shared.error;

public class ShipNotFoundException extends RuntimeException {
    public ShipNotFoundException(String id) { super("Ship '" + id + "' was not found"); }
}
