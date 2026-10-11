package com.soa.borne.borne.application;

import com.soa.borne.borne.domain.Borne;
import com.soa.borne.borne.domain.BorneRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BorneInitializer implements CommandLineRunner {
    private final BorneRepository repository;
    private final String id;
    private final double powerKw;
    private final double flowRate;
    public BorneInitializer(BorneRepository repository,
                            @Value("${borne.id:borne-1}") String id,
                            @Value("${borne.power-kw:7.2}") double powerKw,
                            @Value("${borne.water-flow-lpm:12}") double flowRate) {
        this.repository = repository; this.id = id; this.powerKw = powerKw; this.flowRate = flowRate;
    }
    @Override public void run(String... args) {
        if (!repository.existsById(id)) repository.save(new Borne(id, powerKw, flowRate));
    }
}
