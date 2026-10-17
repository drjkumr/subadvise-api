package com.drjcodes.subadvise_api.model;

import com.drjcodes.subadvise_api.enums.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID subscriptionId;
    @Column(nullable = false)
    private String subscriptionName;
    private LocalDate subscriptionDate;
    private LocalDate renewalDate;

    @Column(nullable = false)
    private BigDecimal subscriptionPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    // Automatically sets subscriptionDate to today if left blank during creation
    @PrePersist
    public void prePersist() {
        if (this.subscriptionDate == null) {
            this.subscriptionDate = LocalDate.now();
        }
    }
}