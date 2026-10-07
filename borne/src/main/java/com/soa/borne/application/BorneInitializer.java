package com.soa.borne.application;

import com.soa.borne.domain.Borne;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BorneInitializer implements CommandLineRunner {

    private final BorneStore repository;
    private final String borneId;
    private final double batteryCapacityKwh;

    public BorneInitializer(BorneStore repository,
                            @Value("${borne.id:borne-1}") String borneId,
                            @Value("${borne.battery-capacity-kwh:50}") double batteryCapacityKwh) {
        this.repository = repository;
        this.borneId = borneId;
        this.batteryCapacityKwh = batteryCapacityKwh;
    }

    @Override
    public void run(String... args) {
        if (!repository.existsById(borneId)) {
            repository.save(new Borne(borneId, batteryCapacityKwh));
        }
    }
}
