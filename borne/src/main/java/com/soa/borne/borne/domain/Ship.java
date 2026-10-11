package com.soa.borne.borne.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "ships")
public class Ship {
    @Id
    @Column(nullable = false, updatable = false, length = 100)
    private String id;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Ship() {}
    public Ship(String id, String name) {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ship id and name must not be blank");
        }
        this.id = id;
        this.name = name;
        this.createdAt = Instant.now();
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}
