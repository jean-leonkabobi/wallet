package com.kabobi.wallet.service;

import com.kabobi.wallet.dto.CategoryExpenseDTO;
import com.kabobi.wallet.dto.DashboardDTO;
import com.kabobi.wallet.dto.MonthlyEvolutionDTO;
import com.kabobi.wallet.model.Budget;
import com.kabobi.wallet.repository.BudgetRepository;
import com.kabobi.wallet.repository.ExpenseRepository;
import com.kabobi.wallet.repository.RevenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private RevenueRepository revenueRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    private static final Map<Integer, String> MONTH_NAMES = new HashMap<>();

    static {
        for (Month month : Month.values()) {
            MONTH_NAMES.put(month.getValue(), month.getDisplayName(TextStyle.FULL, Locale.FRENCH));
        }
    }

    public DashboardDTO getDashboard(int month, int year) {
        LocalDate now = LocalDate.now();
        if (month == 0) month = now.getMonthValue();
        if (year == 0) year = now.getYear();

        // Calculer les totaux du mois
        BigDecimal totalRevenuesMonth = revenueRepository.getTotalRevenueByMonthAndYear(month, year);
        BigDecimal totalExpensesMonth = expenseRepository.getTotalExpenseByMonthAndYear(month, year);

        // Calculer le solde actuel (tous les revenus - toutes les dépenses)
        BigDecimal totalAllRevenues = calculateTotalAllRevenues();
        BigDecimal totalAllExpenses = calculateTotalAllExpenses();
        BigDecimal currentBalance = totalAllRevenues.subtract(totalAllExpenses);

        // Calculer le taux d'épargne
        BigDecimal savingsRate = calculateSavingsRate(totalRevenuesMonth, totalExpensesMonth);

        // Dépenses par catégorie
        List<CategoryExpenseDTO> expensesByCategory = getExpensesByCategory(month, year, totalExpensesMonth);

        // Évolution mensuelle
        List<MonthlyEvolutionDTO> monthlyEvolution = getMonthlyEvolution(year);

        return DashboardDTO.builder()
                .currentBalance(currentBalance)
                .totalRevenuesMonth(totalRevenuesMonth)
                .totalExpensesMonth(totalExpensesMonth)
                .savingsRate(savingsRate)
                .expensesByCategory(expensesByCategory)
                .monthlyEvolution(monthlyEvolution)
                .build();
    }

    private BigDecimal calculateTotalAllRevenues() {
        List<Object[]> results = revenueRepository.findAll().stream()
                .map(r -> new Object[]{r.getAmount()})
                .collect(Collectors.toList());

        return results.stream()
                .map(obj -> (BigDecimal) obj[0])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalAllExpenses() {
        List<Object[]> results = expenseRepository.findAll().stream()
                .map(e -> new Object[]{e.getAmount()})
                .collect(Collectors.toList());

        return results.stream()
                .map(obj -> (BigDecimal) obj[0])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSavingsRate(BigDecimal revenues, BigDecimal expenses) {
        if (revenues.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal savings = revenues.subtract(expenses);
        return savings.divide(revenues, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<CategoryExpenseDTO> getExpensesByCategory(int month, int year, BigDecimal totalExpenses) {
        List<Object[]> results = expenseRepository.getExpensesGroupByCategory(month, year);

        List<CategoryExpenseDTO> categoryExpenses = new ArrayList<>();

        for (Object[] result : results) {
            String categoryName = (String) result[0];
            BigDecimal totalAmount = (BigDecimal) result[1];

            Double percentage = 0.0;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                percentage = totalAmount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            }

            categoryExpenses.add(new CategoryExpenseDTO(categoryName, totalAmount, percentage));
        }

        return categoryExpenses;
    }

    private List<MonthlyEvolutionDTO> getMonthlyEvolution(int year) {
        List<Object[]> expenseResults = expenseRepository.getMonthlyEvolution(year);

        Map<Integer, BigDecimal> expensesByMonth = new HashMap<>();
        Map<Integer, BigDecimal> revenuesByMonth = new HashMap<>();

        // Initialiser les mois
        for (int i = 1; i <= 12; i++) {
            expensesByMonth.put(i, BigDecimal.ZERO);
            revenuesByMonth.put(i, BigDecimal.ZERO);
        }

        // Remplir les dépenses
        for (Object[] result : expenseResults) {
            int month = (int) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            expensesByMonth.put(month, amount);
        }

        // Remplir les revenus (similaire à faire avec une requête)
        for (int i = 1; i <= 12; i++) {
            BigDecimal revenue = revenueRepository.getTotalRevenueByMonthAndYear(i, year);
            revenuesByMonth.put(i, revenue);
        }

        List<MonthlyEvolutionDTO> evolution = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            evolution.add(new MonthlyEvolutionDTO(
                    i,
                    MONTH_NAMES.get(i),
                    expensesByMonth.get(i),
                    revenuesByMonth.get(i)
            ));
        }

        return evolution;
    }

    public List<CategoryExpenseDTO> getExpensesByDateRange(Long categoryId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results;

        if (categoryId != null) {
            // Filtrer par catégorie et date
            results = expenseRepository.findByCategoryIdAndExpenseDateBetween(categoryId, startDate, endDate)
                    .stream()
                    .map(e -> new Object[]{e.getCategory().getName(), e.getAmount()})
                    .collect(Collectors.toList());
        } else {
            // Toutes les catégories
            results = expenseRepository.findByExpenseDateBetween(startDate, endDate)
                    .stream()
                    .map(e -> new Object[]{e.getCategory().getName(), e.getAmount()})
                    .collect(Collectors.toList());
        }

        // Grouper par catégorie
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        for (Object[] result : results) {
            String categoryName = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            categoryTotals.merge(categoryName, amount, BigDecimal::add);
        }

        BigDecimal totalExpenses = categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return categoryTotals.entrySet().stream()
                .map(entry -> {
                    Double percentage = 0.0;
                    if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = entry.getValue()
                                .divide(totalExpenses, 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                                .doubleValue();
                    }
                    return new CategoryExpenseDTO(entry.getKey(), entry.getValue(), percentage);
                })
                .collect(Collectors.toList());
    }
}