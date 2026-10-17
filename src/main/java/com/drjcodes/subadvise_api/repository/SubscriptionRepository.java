package com.drjcodes.subadvise_api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.drjcodes.subadvise_api.model.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findBySubscriptionDate(LocalDate subscriptionDate);

    List<Subscription> findByRenewalDate(LocalDate renewalDate);

    List<Subscription> findByRenewalDateBetween(LocalDate startDate, LocalDate endDate);

    List<Subscription> findBySubscriptionDateBetween(LocalDate startDate, LocalDate endDate);

}
