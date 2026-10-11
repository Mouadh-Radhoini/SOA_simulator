package com.soa.borne.reservation.application;

import com.soa.borne.borne.application.BorneNotFoundException;
import com.soa.borne.borne.domain.*;
import com.soa.borne.borne.infrastructure.persistence.JpaBorneRepository;
import com.soa.borne.pricing.application.PricingService;
import com.soa.borne.reservation.infrastructure.persistence.JpaReservationRepository;
import com.soa.borne.ship.infrastructure.persistence.JpaShipRepository;
import com.soa.borne.shared.error.ReservationNotFoundException;
import com.soa.borne.shared.error.ShipNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class ReservationService {
    private static final EnumSet<ReservationStatus> ACTIVE = EnumSet.of(ReservationStatus.RESERVED, ReservationStatus.IN_USE);
    private final JpaReservationRepository reservations;
    private final JpaShipRepository ships;
    private final JpaBorneRepository bornes;
    private final PricingService pricing;

    public ReservationService(JpaReservationRepository reservations, JpaShipRepository ships,
                              JpaBorneRepository bornes, PricingService pricing) {
        this.reservations = reservations; this.ships = ships; this.bornes = bornes; this.pricing = pricing;
    }
    @Transactional
    public Reservation create(String shipId, String borneId) {
        Ship ship = ships.findById(shipId).orElseThrow(() -> new ShipNotFoundException(shipId));
        Borne borne = bornes.findByIdForUpdate(borneId).orElseThrow(() -> new BorneNotFoundException(borneId));
        if (reservations.existsByShip_IdAndStatusIn(shipId, ACTIVE)) throw new IllegalStateException("Ship already has an active reservation");
        borne.reserve();
        try {
            Reservation reservation = reservations.save(new Reservation(ship, borne));
            bornes.save(borne);
            return reservation;
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("Borne is already reserved", exception);
        }
    }
    @Transactional(readOnly = true) public Reservation find(UUID id) {
        return reservations.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }
    @Transactional public Reservation start(UUID id) { return update(id, Reservation::start); }
    @Transactional public Reservation cancel(UUID id) { return update(id, Reservation::cancel); }
    @Transactional public Reservation consume(UUID id, BigDecimal electricity, BigDecimal water) {
        Reservation reservation = locked(id);
        reservation.consume(electricity, water);
        bornes.save(reservation.getBorne());
        return reservations.save(reservation);
    }
    @Transactional public Reservation complete(UUID id) {
        Reservation reservation = locked(id);
        reservation.complete(pricing.electricityCost(reservation.getElectricityKwh()),
                pricing.waterCost(reservation.getWaterLiters()));
        bornes.save(reservation.getBorne());
        return reservations.save(reservation);
    }
    private Reservation update(UUID id, java.util.function.Consumer<Reservation> action) {
        Reservation reservation = locked(id);
        action.accept(reservation);
        bornes.save(reservation.getBorne());
        return reservations.save(reservation);
    }
    private Reservation locked(UUID id) {
        return reservations.findByIdForUpdate(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }
}
