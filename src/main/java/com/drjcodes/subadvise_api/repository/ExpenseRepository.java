package com.drjcodes.subadvise_api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drjcodes.subadvise_api.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByTarget_EndDateBetween(LocalDate startDate, LocalDate endDate);
}
