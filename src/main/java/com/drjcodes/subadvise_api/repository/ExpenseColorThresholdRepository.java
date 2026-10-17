package com.drjcodes.subadvise_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drjcodes.subadvise_api.enums.ExpenseColor;
import com.drjcodes.subadvise_api.model.ExpenseColorThreshold;

public interface ExpenseColorThresholdRepository extends JpaRepository<ExpenseColorThreshold, ExpenseColor> {
}