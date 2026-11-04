package com.drjcodes.subadvise_api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.drjcodes.subadvise_api.repository.ExpenseRepository;
import com.drjcodes.subadvise_api.repository.SubscriptionRepository;
import com.drjcodes.subadvise_api.repository.TargetRepository;

@Service
public class DatabasePurgeService {

    private final ExpenseRepository expenseRepository;
    private final TargetRepository targetRepository;
    private final SubscriptionRepository subscriptionRepository;

    public DatabasePurgeService(
            ExpenseRepository expenseRepository,
            TargetRepository targetRepository,
            SubscriptionRepository subscriptionRepository) {
        this.expenseRepository = expenseRepository;
        this.targetRepository = targetRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public void purgeSubscriptionsAndTargets() {
        purgeTargets();
        purgeSubscriptions();
    }

    @Transactional
    public void purgeSubscriptions() {
        subscriptionRepository.deleteAllInBatch();
    }

    @Transactional
    public void purgeTargets() {
        expenseRepository.deleteAllInBatch();
        targetRepository.deleteAllInBatch();
    }
}
