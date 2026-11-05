package com.drjcodes.subadvise_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.drjcodes.subadvise_api.dto.ExpenseColorThresholdsDto;
import com.drjcodes.subadvise_api.dto.ExpenseRequestDto;
import com.drjcodes.subadvise_api.dto.ExpenseResponseDto;
import com.drjcodes.subadvise_api.dto.MonthlyExpenseItemDto;
import com.drjcodes.subadvise_api.dto.MonthlyExpenseReportDto;
import com.drjcodes.subadvise_api.dto.TargetRequestDto;
import com.drjcodes.subadvise_api.dto.TargetResponseDto;
import com.drjcodes.subadvise_api.enums.Currency;
import com.drjcodes.subadvise_api.enums.ExpenseColor;
import com.drjcodes.subadvise_api.exception.TargetNotFoundException;
import com.drjcodes.subadvise_api.mapper.ExpenseMapper;
import com.drjcodes.subadvise_api.mapper.TargetMapper;
import com.drjcodes.subadvise_api.model.Expense;
import com.drjcodes.subadvise_api.model.ExpenseColorThreshold;
import com.drjcodes.subadvise_api.model.Target;
import com.drjcodes.subadvise_api.repository.ExpenseColorThresholdRepository;
import com.drjcodes.subadvise_api.repository.ExpenseRepository;
import com.drjcodes.subadvise_api.repository.TargetRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TargetRepository targetRepository;
    private final ExpenseMapper expenseMapper;
    private final TargetMapper targetMapper;
    private final ExpenseColorThresholdRepository thresholdRepository;

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public ExpenseService(
            ExpenseRepository expenseRepository,
            TargetRepository targetRepository,
            ExpenseMapper expenseMapper,
            TargetMapper targetMapper,
            ExpenseColorThresholdRepository thresholdRepository) {
        this.expenseRepository = expenseRepository;
        this.targetRepository = targetRepository;
        this.expenseMapper = expenseMapper;
        this.targetMapper = targetMapper;
        this.thresholdRepository = thresholdRepository;
    }

    public TargetResponseDto setTargetExpense(TargetRequestDto requestDto) {
        if (requestDto == null || requestDto.getTargetExpense() == null
                || requestDto.getTargetExpense().signum() < 0
                || requestDto.getCurrency() == null || requestDto.getEndDate() == null) {
            throw new IllegalArgumentException("Target amount, currency, and endDate are required");
        }
        if (requestDto.getStartDate() != null && requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }

        Target savedTarget = targetRepository.save(targetMapper.toEntity(requestDto));
        return targetMapper.toResponseDto(savedTarget);
    }

    public ExpenseResponseDto createExpense(ExpenseRequestDto requestDto) {
        if (requestDto == null || requestDto.getActualExpense() == null
                || requestDto.getActualExpense().signum() < 0
                || requestDto.getCurrency() == null || requestDto.getTargetId() == null) {
            throw new IllegalArgumentException("actualExpense, currency, and targetId are required");
        }
        Target target = targetRepository.findById(requestDto.getTargetId())
                .orElseThrow(() -> new TargetNotFoundException(requestDto.getTargetId()));
        if (requestDto.getCurrency() != target.getCurrency()) {
            throw new IllegalArgumentException("Expense and target currencies must match");
        }

        Expense expense = expenseMapper.toEntity(requestDto);
        expense.setTarget(target);
        return expenseMapper.toResponseDto(expenseRepository.save(expense));
    }

    public Map<Currency, BigDecimal> calculateMonthlyTotals(int year, int month) {
        return calculateTotalsByCurrency(findExpensesForMonth(year, month));
    }

    public MonthlyExpenseReportDto getMonthlyExpenses(int year, int month) {
        List<Expense> expenses = findExpensesForMonth(year, month);
        Map<Currency, BigDecimal> totals = calculateTotalsByCurrency(expenses);
        Map<ExpenseColor, BigDecimal> thresholds = getThresholdMap();
        List<MonthlyExpenseItemDto> items = new ArrayList<>();

        for (Expense expense : expenses) {
            BigDecimal total = totals.getOrDefault(expense.getCurrency(), ZERO);
            BigDecimal percentage = total.signum() == 0
                    ? ZERO
                    : expense.getActualExpense().multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP);
            MonthlyExpenseItemDto item = new MonthlyExpenseItemDto();
            item.setExpenseId(expense.getExpenseId());
            item.setTargetId(expense.getTarget().getTargetId());
            item.setActualExpense(expense.getActualExpense());
            item.setCurrency(expense.getCurrency());
            item.setPercentageOfTotal(percentage);
            item.setColor(classify(percentage, thresholds));
            items.add(item);
        }

        MonthlyExpenseReportDto report = new MonthlyExpenseReportDto();
        report.setYear(year);
        report.setMonth(month);
        report.setTotalsByCurrency(totals);
        report.setExpenses(items);
        return report;
    }

    public ExpenseColorThresholdsDto getColorThresholds() {
        return toDto(getThresholdMap());
    }

    public ExpenseColorThresholdsDto setColorThresholds(ExpenseColorThresholdsDto thresholdsDto) {
        Map<ExpenseColor, BigDecimal> thresholds = toThresholdMap(thresholdsDto);
        List<ExpenseColorThreshold> entities = thresholds.entrySet().stream()
                .map(entry -> new ExpenseColorThreshold(entry.getKey(), entry.getValue()))
                .toList();
        thresholdRepository.saveAll(entities);
        return toDto(thresholds);
    }

    private List<Expense> findExpensesForMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        return expenseRepository.findByTarget_EndDateBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    private Map<Currency, BigDecimal> calculateTotalsByCurrency(List<Expense> expenses) {
        Map<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
        for (Expense expense : expenses) {
            totals.merge(expense.getCurrency(), expense.getActualExpense(), BigDecimal::add);
        }
        return totals;
    }

    ExpenseColor classify(BigDecimal percentage, Map<ExpenseColor, BigDecimal> thresholds) {
        for (ExpenseColor color : ExpenseColor.values()) {
            if (percentage.compareTo(thresholds.get(color)) >= 0) {
                return color;
            }
        }
        return ExpenseColor.GREEN;
    }

    Map<ExpenseColor, BigDecimal> getThresholdMap() {
        Map<ExpenseColor, BigDecimal> thresholds = defaultThresholds();
        for (ExpenseColorThreshold threshold : thresholdRepository.findAll()) {
            thresholds.put(threshold.getColor(), threshold.getMinimumPercentage());
        }
        return thresholds;
    }

    private Map<ExpenseColor, BigDecimal> toThresholdMap(ExpenseColorThresholdsDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Color thresholds are required");
        }
        Map<ExpenseColor, BigDecimal> thresholds = new EnumMap<>(ExpenseColor.class);
        thresholds.put(ExpenseColor.RED, dto.getRed());
        thresholds.put(ExpenseColor.ORANGE, dto.getOrange());
        thresholds.put(ExpenseColor.YELLOW, dto.getYellow());
        thresholds.put(ExpenseColor.LIME, dto.getLime());
        thresholds.put(ExpenseColor.GREEN, dto.getGreen());

        BigDecimal previous = null;
        for (ExpenseColor color : ExpenseColor.values()) {
            BigDecimal percentage = thresholds.get(color);
            if (percentage == null || percentage.signum() < 0 || percentage.compareTo(HUNDRED) > 0) {
                throw new IllegalArgumentException("Each color threshold must be between 0 and 100");
            }
            if (previous != null && previous.compareTo(percentage) <= 0) {
                throw new IllegalArgumentException("Color thresholds must descend from red to green");
            }
            previous = percentage;
        }
        return thresholds;
    }

    private ExpenseColorThresholdsDto toDto(Map<ExpenseColor, BigDecimal> thresholds) {
        ExpenseColorThresholdsDto dto = new ExpenseColorThresholdsDto();
        dto.setRed(thresholds.get(ExpenseColor.RED));
        dto.setOrange(thresholds.get(ExpenseColor.ORANGE));
        dto.setYellow(thresholds.get(ExpenseColor.YELLOW));
        dto.setLime(thresholds.get(ExpenseColor.LIME));
        dto.setGreen(thresholds.get(ExpenseColor.GREEN));
        return dto;
    }

    private Map<ExpenseColor, BigDecimal> defaultThresholds() {
        Map<ExpenseColor, BigDecimal> thresholds = new EnumMap<>(ExpenseColor.class);
        thresholds.put(ExpenseColor.RED, new BigDecimal("50"));
        thresholds.put(ExpenseColor.ORANGE, new BigDecimal("30"));
        thresholds.put(ExpenseColor.YELLOW, new BigDecimal("15"));
        thresholds.put(ExpenseColor.LIME, new BigDecimal("5"));
        thresholds.put(ExpenseColor.GREEN, ZERO);
        return thresholds;
    }
}
