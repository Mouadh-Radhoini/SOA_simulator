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
import com.soa.borne.mqtt.MqttEventPublisher;

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
    private final MqttEventPublisher mqtt;

    public ReservationService(JpaReservationRepository reservations, JpaShipRepository ships,
                              JpaBorneRepository bornes, PricingService pricing, MqttEventPublisher mqtt) {
        this.reservations = reservations; this.ships = ships; this.bornes = bornes; this.pricing = pricing; this.mqtt = mqtt;
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
            mqtt.enqueue("RESERVATION_CREATED", borneId, java.util.Map.of("reservationId", reservation.getId(),
                    "shipId", shipId), reservation.getId());
            publishStatus(reservation, "RESERVED");
            return reservation;
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("Borne is already reserved", exception);
        }
    }
    @Transactional(readOnly = true) public Reservation find(UUID id) {
        return reservations.findById(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }
    @Transactional public Reservation start(UUID id) { return update(id, Reservation::start, "IN_USE"); }
    @Transactional public Reservation cancel(UUID id) { return update(id, Reservation::cancel, "AVAILABLE"); }
    @Transactional public Reservation consume(UUID id, BigDecimal electricity, BigDecimal water) {
        Reservation reservation = locked(id);
        reservation.consume(electricity, water);
        bornes.save(reservation.getBorne());
        Reservation saved = reservations.save(reservation);
        mqtt.enqueue("CONSUMPTION_RECORDED", saved.getBorne().getId(), java.util.Map.of("reservationId", saved.getId(),
                "shipId", saved.getShip().getId(), "electricityKwh", electricity, "waterLiters", water), saved.getId());
        return saved;
    }
    @Transactional public Reservation complete(UUID id) {
        Reservation reservation = locked(id);
        reservation.complete(pricing.electricityCost(reservation.getElectricityKwh()),
                pricing.waterCost(reservation.getWaterLiters()));
        bornes.save(reservation.getBorne());
        Reservation saved = reservations.save(reservation);
        mqtt.enqueue("RESERVATION_COMPLETED", saved.getBorne().getId(), java.util.Map.of("reservationId", saved.getId(),
                "shipId", saved.getShip().getId(), "totalCost", saved.getTotalCost()), saved.getId());
        publishStatus(saved, "AVAILABLE");
        return saved;
    }
    private Reservation update(UUID id, java.util.function.Consumer<Reservation> action, String status) {
        Reservation reservation = locked(id);
        action.accept(reservation);
        bornes.save(reservation.getBorne());
        Reservation saved = reservations.save(reservation);
        publishStatus(saved, status);
        return saved;
    }
    private void publishStatus(Reservation reservation, String status) {
        mqtt.enqueue("BORNE_STATUS_CHANGED", reservation.getBorne().getId(),
                java.util.Map.of("status", status, "reservationId", reservation.getId()), reservation.getId());
    }
    private Reservation locked(UUID id) {
        return reservations.findByIdForUpdate(id).orElseThrow(() -> new ReservationNotFoundException(id));
    }
}
