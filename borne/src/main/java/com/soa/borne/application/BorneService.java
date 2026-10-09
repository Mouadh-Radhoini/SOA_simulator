package com.soa.borne.application;

import com.soa.borne.domain.Borne;
import com.soa.borne.domain.BorneStatus;
import com.soa.borne.domain.BorneType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BorneService {

    private final BorneStore repository;
    private final PricingService pricingService;

    public BorneService(BorneStore repository, PricingService pricingService) {
        this.repository = repository;
        this.pricingService = pricingService;
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
    public Borne create(String id, BorneType type, double rate) {
        if (repository.existsById(id)) {
            throw new IllegalArgumentException("A borne with id '" + id + "' already exists");
        }
        return repository.save(new Borne(id, type, rate));
    }

    @Transactional
    public Borne updateStatus(String id, BorneStatus target) {
        Borne borne = findById(id);
        return switch (target) {
            case AVAILABLE -> {
                if (borne.getStatus() == BorneStatus.RESERVED) {
                    yield release(id);
                }
                if (borne.getStatus() == BorneStatus.IN_USE) {
                    yield stop(id);
                }
                if (borne.getStatus() == BorneStatus.OUT_OF_SERVICE) {
                    yield reactivate(id);
                }
                throw new com.soa.borne.domain.InvalidBorneTransitionException(
                        "Borne is already available");
            }
            case RESERVED -> reserve(id);
            case IN_USE -> start(id);
            case OUT_OF_SERVICE -> markOutOfService(id);
        };
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
    public Borne start(String id) {
        Borne borne = findById(id);
        borne.startUse();
        return repository.save(borne);
    }

    @Transactional
    public Borne stop(String id) {
        Borne borne = findById(id);
        borne.stopUse();
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
    public Borne consume(String id, double amount) {
        Borne borne = findById(id);
        borne.consume(amount);
        return repository.save(borne);
    }

    @Transactional(readOnly = true)
    public double price(String id) {
        return pricingService.calculate(findById(id));
    }

    public PricingService pricingService() {
        return pricingService;
    }
}
