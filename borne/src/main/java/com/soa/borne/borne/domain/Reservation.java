package com.soa.borne.borne.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ship_id", nullable = false)
    private Ship ship;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "borne_id", nullable = false)
    private Borne borne;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReservationStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "started_at")
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "electricity_kwh", nullable = false, precision = 19, scale = 6)
    private BigDecimal electricityKwh = BigDecimal.ZERO;
    @Column(name = "water_liters", nullable = false, precision = 19, scale = 3)
    private BigDecimal waterLiters = BigDecimal.ZERO;
    @Column(name = "electricity_cost", nullable = false, precision = 19, scale = 6)
    private BigDecimal electricityCost = BigDecimal.ZERO;
    @Column(name = "water_cost", nullable = false, precision = 19, scale = 6)
    private BigDecimal waterCost = BigDecimal.ZERO;
    @Column(name = "total_cost", nullable = false, precision = 19, scale = 6)
    private BigDecimal totalCost = BigDecimal.ZERO;
    @Version
    private long version;

    protected Reservation() {}
    public Reservation(Ship ship, Borne borne) {
        this.id = UUID.randomUUID();
        this.ship = ship;
        this.borne = borne;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = Instant.now();
    }
    public void start() {
        require(ReservationStatus.RESERVED, "Only a reserved reservation can start");
        borne.startUse();
        status = ReservationStatus.IN_USE;
        startedAt = Instant.now();
    }
    public void consume(BigDecimal electricity, BigDecimal water) {
        require(ReservationStatus.IN_USE, "Only an in-use reservation can consume");
        if (electricity == null || water == null || electricity.signum() < 0 || water.signum() < 0) {
            throw new IllegalArgumentException("Consumption values must not be negative");
        }
        electricityKwh = electricityKwh.add(electricity);
        waterLiters = waterLiters.add(water);
        borne.addConsumption(electricity, water);
    }
    public void complete(BigDecimal electricityCost, BigDecimal waterCost) {
        require(ReservationStatus.IN_USE, "Only an in-use reservation can complete");
        this.electricityCost = money(electricityCost);
        this.waterCost = money(waterCost);
        this.totalCost = money(this.electricityCost.add(this.waterCost));
        status = ReservationStatus.COMPLETED;
        completedAt = Instant.now();
        borne.release();
    }
    public void cancel() {
        if (status != ReservationStatus.RESERVED) throw new IllegalStateException("Only a reserved reservation can be cancelled");
        status = ReservationStatus.CANCELLED;
        completedAt = Instant.now();
        borne.release();
    }
    private void require(ReservationStatus expected, String message) {
        if (status != expected) throw new IllegalStateException(message + " (current status: " + status + ")");
    }
    private static BigDecimal money(BigDecimal value) {
        return value.setScale(6, RoundingMode.HALF_UP);
    }
    public UUID getId() { return id; }
    public Ship getShip() { return ship; }
    public Borne getBorne() { return borne; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public BigDecimal getElectricityKwh() { return electricityKwh; }
    public BigDecimal getWaterLiters() { return waterLiters; }
    public BigDecimal getElectricityCost() { return electricityCost; }
    public BigDecimal getWaterCost() { return waterCost; }
    public BigDecimal getTotalCost() { return totalCost; }
}
