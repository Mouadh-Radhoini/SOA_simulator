package com.soa.borne.application;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BorneInitializer implements CommandLineRunner {

    private final BorneStore repository;
    private final String borneId;
    private final BorneType type;
    private final double rate;

    public BorneInitializer(BorneStore repository,
                            @Value("${borne.id:borne-1}") String borneId,
                            @Value("${borne.type:ELECTRICITY}") BorneType type,
                            @Value("${borne.rate:7.2}") double rate) {
        this.repository = repository;
        this.borneId = borneId;
        this.type = type;
        this.rate = rate;
    }

    @Override
    public void run(String... args) {
        if (!repository.existsById(borneId)) {
            repository.save(new Borne(borneId, type, rate));
        }
    }
}
