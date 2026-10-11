package com.soa.borne.shared.error;

import java.util.UUID;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(UUID id) { super("Reservation '" + id + "' was not found"); }
}
