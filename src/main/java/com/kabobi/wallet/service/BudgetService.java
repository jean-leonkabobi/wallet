package com.kabobi.wallet.service;

import com.kabobi.wallet.dto.BudgetAlertDTO;
import com.kabobi.wallet.dto.BudgetDTO;
import com.kabobi.wallet.dto.BudgetStatusDTO;
import com.kabobi.wallet.exception.ResourceNotFoundException;
import com.kabobi.wallet.model.Budget;
import com.kabobi.wallet.model.Category;
import com.kabobi.wallet.repository.BudgetRepository;
import com.kabobi.wallet.repository.CategoryRepository;
import com.kabobi.wallet.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    // Seuils d'alerte par défaut
    private static final double WARNING_THRESHOLD = 75.0;
    private static final double CRITICAL_THRESHOLD = 90.0;

    // Conversion Entity -> DTO
    private BudgetDTO convertToDTO(Budget budget) {
        BudgetDTO dto = new BudgetDTO();
        dto.setId(budget.getId());
        dto.setAmount(budget.getAmount());
        dto.setMonth(budget.getMonth());
        dto.setYear(budget.getYear());
        dto.setCategoryId(budget.getCategory() != null ? budget.getCategory().getId() : null);
        dto.setAlertThreshold(budget.getAlertThreshold());
        dto.setIsActive(budget.getIsActive());
        return dto;
    }

    // Conversion DTO -> Entity
    private Budget convertToEntity(BudgetDTO dto) {
        Budget budget = new Budget();
        budget.setAmount(dto.getAmount());
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());
        budget.setAlertThreshold(dto.getAlertThreshold());
        budget.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
            budget.setCategory(category);
        }

        return budget;
    }

    // CRUD Operations

    public BudgetDTO createBudget(BudgetDTO budgetDTO) {
        // Vérifier si un budget existe déjà pour ce mois/année/catégorie
        boolean exists;
        if (budgetDTO.getCategoryId() != null) {
            exists = budgetRepository.existsByMonthAndYearAndCategoryId(
                    budgetDTO.getMonth(), budgetDTO.getYear(), budgetDTO.getCategoryId());
            if (exists) {
                throw new RuntimeException("Un budget existe déjà pour cette catégorie, ce mois et cette année");
            }
        } else {
            exists = budgetRepository.existsByMonthAndYearAndCategoryIsNull(
                    budgetDTO.getMonth(), budgetDTO.getYear());
            if (exists) {
                throw new RuntimeException("Un budget global existe déjà pour ce mois et cette année");
            }
        }

        Budget budget = convertToEntity(budgetDTO);
        Budget savedBudget = budgetRepository.save(budget);
        return convertToDTO(savedBudget);
    }

    public List<BudgetDTO> getAllBudgets() {
        return budgetRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BudgetDTO getBudgetById(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        return convertToDTO(budget);
    }

    public List<BudgetDTO> getBudgetsByMonthAndYear(int month, int year) {
        return budgetRepository.findByMonthAndYearAndIsActiveTrue(month, year)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BudgetDTO updateBudget(Long id, BudgetDTO budgetDTO) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));

        budget.setAmount(budgetDTO.getAmount());
        budget.setMonth(budgetDTO.getMonth());
        budget.setYear(budgetDTO.getYear());
        budget.setAlertThreshold(budgetDTO.getAlertThreshold());
        budget.setIsActive(budgetDTO.getIsActive());

        if (budgetDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(budgetDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", budgetDTO.getCategoryId()));
            budget.setCategory(category);
        } else {
            budget.setCategory(null);
        }

        Budget updatedBudget = budgetRepository.save(budget);
        return convertToDTO(updatedBudget);
    }

    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget", "id", id);
        }
        budgetRepository.deleteById(id);
    }

    // Statut du budget

    public BudgetStatusDTO getBudgetStatus(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        BigDecimal spentAmount = calculateSpentAmount(budget);
        BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);

        double percentageUsed = 0.0;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = spentAmount
                    .divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        String status = determineStatus(percentageUsed, budget.getAlertThreshold());

        BudgetStatusDTO statusDTO = new BudgetStatusDTO();
        statusDTO.setBudgetId(budget.getId());
        statusDTO.setBudgetAmount(budget.getAmount());
        statusDTO.setSpentAmount(spentAmount);
        statusDTO.setRemainingAmount(remainingAmount);
        statusDTO.setPercentageUsed(percentageUsed);
        statusDTO.setStatus(status);

        return statusDTO;
    }

    public List<BudgetStatusDTO> getAllBudgetsStatus(int month, int year) {
        List<Budget> budgets = budgetRepository.findByMonthAndYearAndIsActiveTrue(month, year);
        List<BudgetStatusDTO> statuses = new ArrayList<>();

        for (Budget budget : budgets) {
            statuses.add(getBudgetStatus(budget.getId()));
        }

        return statuses;
    }

    // Système d'alertes

    public List<BudgetAlertDTO> checkBudgetAlerts(int month, int year) {
        List<Budget> budgets = budgetRepository.findByMonthAndYearAndIsActiveTrue(month, year);
        List<BudgetAlertDTO> alerts = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spentAmount = calculateSpentAmount(budget);

            double percentageUsed = 0.0;
            if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                percentageUsed = spentAmount
                        .divide(budget.getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .doubleValue();
            }

            // Utiliser le seuil personnalisé ou les seuils par défaut
            double threshold = budget.getAlertThreshold() != null ?
                    budget.getAlertThreshold().doubleValue() : WARNING_THRESHOLD;

            BudgetAlertDTO alert = null;

            if (percentageUsed >= 100.0) {
                alert = createAlert(budget, "EXCEEDED", percentageUsed, spentAmount);
            } else if (percentageUsed >= CRITICAL_THRESHOLD) {
                alert = createAlert(budget, "CRITICAL", percentageUsed, spentAmount);
            } else if (percentageUsed >= threshold) {
                alert = createAlert(budget, "WARNING", percentageUsed, spentAmount);
            }

            if (alert != null) {
                alerts.add(alert);
            }
        }

        return alerts;
    }

    // Vérification automatique des budgets (peut être planifiée)
    @Scheduled(cron = "0 0 9 * * *") // Tous les jours à 9h
    public void scheduledBudgetCheck() {
        LocalDate now = LocalDate.now();
        List<BudgetAlertDTO> alerts = checkBudgetAlerts(now.getMonthValue(), now.getYear());

        if (!alerts.isEmpty()) {
            // Log les alertes (dans un vrai système, on enverrait des notifications)
            System.out.println("=== ALERTES BUDGET ===");
            alerts.forEach(alert -> System.out.println(alert.getMessage()));
        }
    }

    // Méthodes privées helper

    private BigDecimal calculateSpentAmount(Budget budget) {
        if (budget.getCategory() != null) {
            // Budget par catégorie
            return expenseRepository.getTotalExpenseByCategoryAndMonthAndYear(
                    budget.getCategory().getId(), budget.getMonth(), budget.getYear());
        } else {
            // Budget global
            return expenseRepository.getTotalExpenseByMonthAndYear(
                    budget.getMonth(), budget.getYear());
        }
    }

    private String determineStatus(double percentageUsed, BigDecimal customThreshold) {
        double threshold = customThreshold != null ? customThreshold.doubleValue() : WARNING_THRESHOLD;

        if (percentageUsed >= 100.0) return "EXCEEDED";
        if (percentageUsed >= CRITICAL_THRESHOLD) return "CRITICAL";
        if (percentageUsed >= threshold) return "WARNING";
        return "OK";
    }

    private BudgetAlertDTO createAlert(Budget budget, String type, double percentageUsed, BigDecimal spentAmount) {
        BigDecimal remaining = budget.getAmount().subtract(spentAmount);
        String categoryName = budget.getCategory() != null ? budget.getCategory().getName() : "Global";
        String monthName = String.format("%02d/%d", budget.getMonth(), budget.getYear());

        String message;
        switch (type) {
            case "EXCEEDED":
                message = String.format("Budget %s dépassé de %.2f€ pour %s !",
                        categoryName, remaining.abs(), monthName);
                break;
            case "CRITICAL":
                message = String.format("Budget %s critique : %.1f%% utilisé (%.2f€ restants) pour %s",
                        categoryName, percentageUsed, remaining, monthName);
                break;
            default:
                message = String.format("Attention : %.1f%% du budget %s utilisé pour %s",
                        percentageUsed, categoryName, monthName);
        }

        return BudgetAlertDTO.builder()
                .budgetId(budget.getId())
                .alertType(type)
                .message(message)
                .budgetAmount(budget.getAmount())
                .spentAmount(spentAmount)
                .remainingAmount(remaining)
                .percentageUsed(percentageUsed)
                .categoryName(categoryName)
                .alertDate(LocalDateTime.now())
                .build();
    }
}