package com.drjcodes.subadvise_api.model;

import java.math.BigDecimal;

import com.drjcodes.subadvise_api.enums.ExpenseColor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense_color_threshold")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseColorThreshold {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    private ExpenseColor color;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal minimumPercentage;
}