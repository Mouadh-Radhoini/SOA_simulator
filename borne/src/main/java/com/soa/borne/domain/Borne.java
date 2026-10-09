package com.soa.borne.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "bornes")
public class Borne {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 100)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BorneType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BorneStatus status;

    @Column(nullable = false)
    private double consumption;

    @Column(name = "power_kw")
    private Double powerKw;

    @Column(name = "flow_rate_lpm")
    private Double flowRateLitersPerMinute;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Borne() {
    }

    public Borne(String id, BorneType type, double rate) {
        this(id, type, rate, Instant.now());
    }

    public Borne(String id, BorneType type, double rate, Instant now) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Borne id must not be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Borne type must not be null");
        }
        if (Double.isNaN(rate) || Double.isInfinite(rate) || rate <= 0) {
            throw new IllegalArgumentException("Borne rate must be positive");
        }
        this.id = id;
        this.type = type;
        this.status = BorneStatus.AVAILABLE;
        this.consumption = 0;
        if (type == BorneType.ELECTRICITY) {
            this.powerKw = rate;
        } else {
            this.flowRateLitersPerMinute = rate;
        }
        this.createdAt = now;
        this.updatedAt = now;
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

    public void startUse() {
        requireStatus(BorneStatus.RESERVED, "Only a reserved borne can start being used");
        status = BorneStatus.IN_USE;
        touch();
    }

    public void stopUse() {
        requireStatus(BorneStatus.IN_USE, "Only an in-use borne can stop being used");
        status = BorneStatus.AVAILABLE;
        touch();
    }

    public void markOutOfService() {
        if (status == BorneStatus.OUT_OF_SERVICE) {
            throw new InvalidBorneTransitionException("Borne is already out of service");
        }
        status = BorneStatus.OUT_OF_SERVICE;
        touch();
    }

    public void reactivate() {
        requireStatus(BorneStatus.OUT_OF_SERVICE,
                "Only an out-of-service borne can be reactivated");
        status = BorneStatus.AVAILABLE;
        touch();
    }

    public void consume(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Consumption must not be negative");
        }
        consumption += amount;
        touch();
    }

    private void requireStatus(BorneStatus expected, String message) {
        if (status != expected) {
            throw new InvalidBorneTransitionException(message + " (current status: " + status + ")");
        }
    }

    private void touch() {
        updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public BorneType getType() { return type; }
    public BorneStatus getStatus() { return status; }
    public double getConsumption() { return consumption; }
    public Double getPowerKw() { return powerKw; }
    public Double getFlowRateLitersPerMinute() { return flowRateLitersPerMinute; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
