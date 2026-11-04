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
@Table(name = "target")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Target {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID targetId;
    @Column(nullable = false)
    private BigDecimal targetExpense;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;

    // Automatically sets startDate to today if left blank during creation
    @PrePersist
    public void prePersist() {
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }

}
