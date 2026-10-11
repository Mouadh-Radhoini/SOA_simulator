package com.soa.borne.ship.infrastructure.persistence;

import com.soa.borne.borne.domain.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaShipRepository extends JpaRepository<Ship, String> {}
