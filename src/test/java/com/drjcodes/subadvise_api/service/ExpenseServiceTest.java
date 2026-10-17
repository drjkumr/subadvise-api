package com.drjcodes.subadvise_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.drjcodes.subadvise_api.dto.ExpenseColorThresholdsDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.enums.ExpenseColor;
import com.drjcodes.subadvise_api.mapper.ExpenseMapper;
import com.drjcodes.subadvise_api.mapper.TargetMapper;
import com.drjcodes.subadvise_api.model.Expense;
import com.drjcodes.subadvise_api.model.Target;
import com.drjcodes.subadvise_api.repository.ExpenseColorThresholdRepository;
import com.drjcodes.subadvise_api.repository.ExpenseRepository;
import com.drjcodes.subadvise_api.repository.TargetRepository;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private TargetRepository targetRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private TargetMapper targetMapper;

    @Mock
    private ExpenseColorThresholdRepository thresholdRepository;

    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        expenseService = new ExpenseService(
                expenseRepository,
                targetRepository,
                expenseMapper,
                targetMapper,
                thresholdRepository);
    }

    @Test
    void monthlyReportIncludesLeapDayAndSeparatesCurrencyTotals() {
        Expense largeUsdExpense = expense("75.00", Currency.USD, "2024-02-29");
        Expense smallUsdExpense = expense("25.00", Currency.USD, "2024-02-28");
        Expense eurExpense = expense("20.00", Currency.EUR, "2024-02-29");
        when(expenseRepository.findByTarget_EndDateBetween(
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29)))
                .thenReturn(List.of(largeUsdExpense, smallUsdExpense, eurExpense));
        when(thresholdRepository.findAll()).thenReturn(List.of());

        var report = expenseService.getMonthlyExpenses(2024, 2);

        assertEquals(new BigDecimal("100.00"), report.getTotalsByCurrency().get(Currency.USD));
        assertEquals(new BigDecimal("20.00"), report.getTotalsByCurrency().get(Currency.EUR));
        assertEquals(new BigDecimal("75.00"), report.getExpenses().get(0).getPercentageOfTotal());
        assertEquals(ExpenseColor.RED, report.getExpenses().get(0).getColor());
        assertEquals(new BigDecimal("25.00"), report.getExpenses().get(1).getPercentageOfTotal());
        assertEquals(ExpenseColor.YELLOW, report.getExpenses().get(1).getColor());
        verify(expenseRepository).findByTarget_EndDateBetween(
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));
    }

    @Test
    void setColorThresholdsRejectsThresholdsThatDoNotDescend() {
        ExpenseColorThresholdsDto thresholds = new ExpenseColorThresholdsDto();
        thresholds.setRed(new BigDecimal("20"));
        thresholds.setOrange(new BigDecimal("30"));
        thresholds.setYellow(new BigDecimal("15"));
        thresholds.setLime(new BigDecimal("5"));
        thresholds.setGreen(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class, () -> expenseService.setColorThresholds(thresholds));
    }

    private Expense expense(String amount, Currency currency, String endDate) {
        return Expense.builder()
                .actualExpense(new BigDecimal(amount))
                .currency(currency)
                .target(Target.builder()
                        .targetId(java.util.UUID.randomUUID())
                        .endDate(LocalDate.parse(endDate))
                        .build())
                .build();
    }
}