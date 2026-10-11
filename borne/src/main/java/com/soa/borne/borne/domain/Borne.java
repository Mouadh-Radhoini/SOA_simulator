package com.soa.borne.borne.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bornes")
public class Borne {
    @Id
    @Column(nullable = false, updatable = false, length = 100)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BorneStatus status;

    @Column(name = "electricity_kwh", nullable = false, precision = 19, scale = 6)
    private BigDecimal electricityKwh = BigDecimal.ZERO;

    @Column(name = "water_liters", nullable = false, precision = 19, scale = 3)
    private BigDecimal waterLiters = BigDecimal.ZERO;

    @Column(name = "power_kw", nullable = false)
    private double powerKw;

    @Column(name = "flow_rate_lpm", nullable = false)
    private double flowRateLitersPerMinute;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Borne() {
    }

    public Borne(String id, double powerKw, double flowRateLitersPerMinute) {
        this(id, powerKw, flowRateLitersPerMinute, Instant.now());
    }

    public Borne(String id, double powerKw, double flowRateLitersPerMinute, Instant now) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Borne id must not be blank");
        validatePositive(powerKw, "Power");
        validatePositive(flowRateLitersPerMinute, "Water flow");
        if (now == null) throw new IllegalArgumentException("Borne timestamp must not be null");
        this.id = id;
        this.status = BorneStatus.AVAILABLE;
        this.powerKw = powerKw;
        this.flowRateLitersPerMinute = flowRateLitersPerMinute;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void reserve() {
        require(BorneStatus.AVAILABLE, "Only an available borne can be reserved");
        status = BorneStatus.RESERVED;
        touch();
    }

    public void startUse() {
        require(BorneStatus.RESERVED, "Only a reserved borne can start service");
        status = BorneStatus.IN_USE;
        touch();
    }

    public void release() {
        if (status != BorneStatus.RESERVED && status != BorneStatus.IN_USE) {
            throw invalid("Only a reserved or in-use borne can be released");
        }
        status = BorneStatus.AVAILABLE;
        touch();
    }

    public void addConsumption(BigDecimal electricity, BigDecimal water) {
        if (status != BorneStatus.IN_USE) throw invalid("Consumption requires an in-use borne");
        validateAmount(electricity, "Electricity");
        validateAmount(water, "Water");
        electricityKwh = electricityKwh.add(electricity);
        waterLiters = waterLiters.add(water);
        touch();
    }

    public void markOutOfService() {
        require(BorneStatus.AVAILABLE, "Only an available borne can be taken out of service");
        status = BorneStatus.OUT_OF_SERVICE;
        touch();
    }

    public void reactivate() {
        require(BorneStatus.OUT_OF_SERVICE, "Only an out-of-service borne can be reactivated");
        status = BorneStatus.AVAILABLE;
        touch();
    }

    private void require(BorneStatus expected, String message) {
        if (status != expected) throw invalid(message + " (current status: " + status + ")");
    }

    private InvalidBorneTransitionException invalid(String message) {
        return new InvalidBorneTransitionException(message);
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    private static void validatePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0) throw new IllegalArgumentException(name + " must be positive");
    }

    private static void validateAmount(BigDecimal value, String name) {
        if (value == null || value.signum() < 0) throw new IllegalArgumentException(name + " amount must not be negative");
    }

    public String getId() { return id; }
    public BorneStatus getStatus() { return status; }
    public BigDecimal getElectricityKwh() { return electricityKwh; }
    public BigDecimal getWaterLiters() { return waterLiters; }
    public double getPowerKw() { return powerKw; }
    public double getFlowRateLitersPerMinute() { return flowRateLitersPerMinute; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
