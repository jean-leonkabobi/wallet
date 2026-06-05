package com.kabobi.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private BigDecimal currentBalance;      // Solde actuel
    private BigDecimal totalRevenuesMonth;  // Revenus du mois
    private BigDecimal totalExpensesMonth;  // Dépenses du mois
    private BigDecimal savingsRate;         // Taux d'épargne (%)
    private List<CategoryExpenseDTO> expensesByCategory;  // Dépenses par catégorie
    private List<MonthlyEvolutionDTO> monthlyEvolution;    // Évolution mensuelle
}