package com.soa.borne.application;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.time.Instant;

@Service
public class BorneService {

    private final BorneStore repository;
    private final Clock clock;
    private final double chargingRateKw;

    @Autowired
    public BorneService(BorneStore repository,
                        @Value("${borne.charging-rate-kw:7.2}") double chargingRateKw) {
        this(repository, Clock.systemUTC(), chargingRateKw);
    }

    BorneService(BorneStore repository, Clock clock, double chargingRateKw) {
        this.repository = repository;
        this.clock = clock;
        this.chargingRateKw = chargingRateKw;
    }

    @Transactional(readOnly = true)
    public List<Borne> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Borne findById(String id) {
        return repository.findById(id).orElseThrow(() -> new BorneNotFoundException(id));
    }

    @Transactional
    public Borne create(String id, double batteryCapacityKwh, double powerKw) {
        if (repository.existsById(id)) {
            throw new IllegalArgumentException("A borne with id '" + id + "' already exists");
        }
        return repository.save(new Borne(id, batteryCapacityKwh, powerKw, clock.instant()));
    }

    @Transactional
    public Borne updateStatus(String id, BorneStatus target) {
        return switch (target) {
            case AVAILABLE -> releaseOrReactivate(id);
            case RESERVED -> reserve(id);
            case CHARGING -> startCharging(id);
            case OUT_OF_SERVICE -> markOutOfService(id);
        };
    }

    private Borne releaseOrReactivate(String id) {
        Borne borne = findById(id);
        if (borne.getStatus() == BorneStatus.OUT_OF_SERVICE) {
            return reactivate(id);
        }
        return release(id);
    }

    @Transactional
    public Borne reserve(String id) {
        Borne borne = findById(id);
        borne.reserve();
        return repository.save(borne);
    }

    @Transactional
    public Borne release(String id) {
        Borne borne = findById(id);
        borne.release();
        return repository.save(borne);
    }

    @Transactional
    public Borne startCharging(String id) {
        Borne borne = findById(id);
        borne.startCharging(clock.instant());
        return repository.save(borne);
    }

    @Transactional
    public Borne stopCharging(String id) {
        Borne borne = findById(id);
        borne.applyChargingEnergy(clock.instant(), chargingRateKw);
        if (borne.getStatus() == BorneStatus.CHARGING) {
            borne.stopCharging();
        }
        return repository.save(borne);
    }

    @Transactional
    public Borne markOutOfService(String id) {
        Borne borne = findById(id);
        borne.markOutOfService();
        return repository.save(borne);
    }

    @Transactional
    public Borne reactivate(String id) {
        Borne borne = findById(id);
        borne.reactivate();
        return repository.save(borne);
    }

    @Transactional
    @Scheduled(fixedDelayString = "${borne.charging-update-ms:60000}")
    public void updateChargingEnergy() {
        Instant now = clock.instant();
        repository.findByStatus(BorneStatus.CHARGING).forEach(borne -> {
            borne.applyChargingEnergy(now, chargingRateKw);
            repository.save(borne);
        });
    }
}
