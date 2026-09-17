package com.gestioncomercial.api.persistence;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionProbeService {

    private final PersistenceProbeRepository repository;

    public TransactionProbeService(PersistenceProbeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void savePair(String firstValue, String secondValue) {
        Instant createdAt = Instant.now();
        repository.save(new PersistenceProbe(firstValue, createdAt));
        repository.save(new PersistenceProbe(secondValue, createdAt));
    }

    @Transactional
    public void savePairAndFail(String firstValue, String secondValue) {
        savePair(firstValue, secondValue);
        repository.flush();
        throw new IllegalStateException("Intentional failure used to verify transaction rollback");
    }
}
