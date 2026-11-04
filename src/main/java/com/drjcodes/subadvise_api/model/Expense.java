package com.drjcodes.subadvise_api.model;

import com.drjcodes.subadvise_api.enums.Currency;
import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID expenseId;
    @Column(nullable = false)
    private BigDecimal actualExpense;
    private BigDecimal difference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @OneToOne
    @JoinColumn(name = "target_id", referencedColumnName = "targetId", nullable = false)
    private Target target;

    @PrePersist
    @PreUpdate
    public void calculateDifference() {
        if (this.actualExpense != null && this.target != null && this.target.getTargetExpense() != null) {
            if (this.currency != this.target.getCurrency()) {
                throw new IllegalStateException("Expense and target currencies must match");
            }
            this.difference = this.actualExpense.subtract(this.target.getTargetExpense());
        }
    }

}
