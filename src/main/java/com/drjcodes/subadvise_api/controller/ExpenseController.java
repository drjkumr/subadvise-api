package com.drjcodes.subadvise_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.drjcodes.subadvise_api.dto.ExpenseColorThresholdsDto;
import com.drjcodes.subadvise_api.dto.ExpenseRequestDto;
import com.drjcodes.subadvise_api.dto.ExpenseResponseDto;
import com.drjcodes.subadvise_api.dto.MonthlyExpenseReportDto;
import com.drjcodes.subadvise_api.dto.TargetRequestDto;
import com.drjcodes.subadvise_api.dto.TargetResponseDto;
import com.drjcodes.subadvise_api.service.ExpenseService;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/targets")
    @ResponseStatus(HttpStatus.CREATED)
    public TargetResponseDto setTargetExpense(@RequestBody TargetRequestDto requestDto) {
        return expenseService.setTargetExpense(requestDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponseDto createExpense(@RequestBody ExpenseRequestDto requestDto) {
        return expenseService.createExpense(requestDto);
    }

    @GetMapping("/monthly")
    public MonthlyExpenseReportDto getMonthlyExpenses(
            @RequestParam int year,
            @RequestParam int month) {
        return expenseService.getMonthlyExpenses(year, month);
    }

    @GetMapping("/color-thresholds")
    public ExpenseColorThresholdsDto getColorThresholds() {
        return expenseService.getColorThresholds();
    }

    @PutMapping("/color-thresholds")
    public ExpenseColorThresholdsDto setColorThresholds(@RequestBody ExpenseColorThresholdsDto thresholds) {
        return expenseService.setColorThresholds(thresholds);
    }
}