package com.soa.borne.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "bornes")
public class Borne {

    private static final double EPSILON = 0.000001d;

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 100)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BorneStatus status;

    @Column(name = "energy_kwh", nullable = false)
    private double energyKwh;

    @Column(name = "power_kw", nullable = false)
    private double powerKw;

    @Column(name = "battery_capacity_kwh", nullable = false)
    private double batteryCapacityKwh;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "charging_started_at")
    private Instant chargingStartedAt;

    @Column(name = "last_energy_update_at", nullable = false)
    private Instant lastEnergyUpdateAt;

    @Version
    private long version;

    protected Borne() {
    }

    public Borne(String id, double batteryCapacityKwh) {
        this(id, batteryCapacityKwh, 7.2, Instant.now());
    }

    public Borne(String id, double batteryCapacityKwh, double powerKw, Instant now) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Borne id must not be blank");
        }
        if (batteryCapacityKwh <= 0) {
            throw new IllegalArgumentException("Battery capacity must be positive");
        }
        if (powerKw <= 0) {
            throw new IllegalArgumentException("Power must be positive");
        }
        this.id = id;
        this.status = BorneStatus.AVAILABLE;
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.powerKw = powerKw;
        this.createdAt = now;
        this.updatedAt = now;
        this.lastEnergyUpdateAt = now;
    }

    public void reserve() {
        requireStatus(BorneStatus.AVAILABLE, "Only an available borne can be reserved");
        status = BorneStatus.RESERVED;
        touch();
    }

    public void release() {
        requireStatus(BorneStatus.RESERVED, "Only a reserved borne can be released");
        status = BorneStatus.AVAILABLE;
        touch();
    }

    public void startCharging(Instant now) {
        requireStatus(BorneStatus.RESERVED, "Only a reserved borne can start charging");
        chargingStartedAt = now;
        lastEnergyUpdateAt = now;
        status = BorneStatus.CHARGING;
        touch(now);
    }

    public void stopCharging() {
        requireStatus(BorneStatus.CHARGING, "Only a charging borne can stop charging");
        status = BorneStatus.AVAILABLE;
        chargingStartedAt = null;
        touch();
    }

    public void markOutOfService() {
        status = BorneStatus.OUT_OF_SERVICE;
        chargingStartedAt = null;
        touch();
    }

    public void reactivate() {
        requireStatus(BorneStatus.OUT_OF_SERVICE, "Only an out-of-service borne can be reactivated");
        status = BorneStatus.AVAILABLE;
        lastEnergyUpdateAt = Instant.now();
        touch();
    }

    public void applyChargingEnergy(Instant now, double kilowatts) {
        requireStatus(BorneStatus.CHARGING, "Energy can only be updated while charging");
        if (now.isBefore(lastEnergyUpdateAt)) {
            return;
        }
        if (kilowatts < 0) {
            throw new IllegalArgumentException("Charging rate must not be negative");
        }
        double elapsedHours = Duration.between(lastEnergyUpdateAt, now).toMillis() / 3_600_000d;
        energyKwh = Math.min(batteryCapacityKwh, energyKwh + elapsedHours * kilowatts);
        lastEnergyUpdateAt = now;
        touch(now);
        if (batteryCapacityKwh - energyKwh <= EPSILON) {
            energyKwh = batteryCapacityKwh;
            stopCharging();
        }
    }

    private void touch() {
        touch(Instant.now());
    }

    private void touch(Instant now) {
        updatedAt = now;
    }

    private void requireStatus(BorneStatus expected, String message) {
        if (status != expected) {
            throw new InvalidBorneTransitionException(message + " (current status: " + status + ")");
        }
    }

    public String getId() {
        return id;
    }

    public BorneStatus getStatus() {
        return status;
    }

    public double getEnergyKwh() {
        return energyKwh;
    }

    public double getPowerKw() {
        return powerKw;
    }

    public double getBatteryCapacityKwh() {
        return batteryCapacityKwh;
    }

    public Instant getChargingStartedAt() {
        return chargingStartedAt;
    }

    public Instant getLastEnergyUpdateAt() {
        return lastEnergyUpdateAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
