package com.soa.borne.reservation.infrastructure.persistence;

import com.soa.borne.borne.domain.Reservation;
import com.soa.borne.borne.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface JpaReservationRepository extends JpaRepository<Reservation, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r join fetch r.borne join fetch r.ship where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") UUID id);
    boolean existsByBorne_IdAndStatusIn(String borneId, Iterable<ReservationStatus> statuses);
    boolean existsByShip_IdAndStatusIn(String shipId, Iterable<ReservationStatus> statuses);
}
