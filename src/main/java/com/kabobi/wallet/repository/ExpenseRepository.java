package com.kabobi.wallet.repository;

import com.kabobi.wallet.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Trouver toutes les dépenses par mois/année
    @Query("SELECT e FROM Expense e WHERE MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year")
    List<Expense> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Trouver les dépenses par catégorie et mois/année
    @Query("SELECT e FROM Expense e WHERE e.category.id = :categoryId AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year")
    List<Expense> findByCategoryAndMonthAndYear(@Param("categoryId") Long categoryId, @Param("month") int month, @Param("year") int year);

    // Trouver les dépenses entre deux dates
    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    // Trouver les dépenses par catégorie entre deux dates
    List<Expense> findByCategoryIdAndExpenseDateBetween(Long categoryId, LocalDate startDate, LocalDate endDate);

    // Calculer le total des dépenses par mois/année
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year")
    BigDecimal getTotalExpenseByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Calculer le total des dépenses par catégorie et mois/année
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.category.id = :categoryId AND MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year")
    BigDecimal getTotalExpenseByCategoryAndMonthAndYear(@Param("categoryId") Long categoryId, @Param("month") int month, @Param("year") int year);

    // Recherche par description
    List<Expense> findByDescriptionContainingIgnoreCase(String keyword);

    // Trouver toutes les dépenses par date
    List<Expense> findByExpenseDate(LocalDate date);

    // Agréger les dépenses par catégorie pour un mois/année
    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE MONTH(e.expenseDate) = :month AND YEAR(e.expenseDate) = :year GROUP BY e.category.name")
    List<Object[]> getExpensesGroupByCategory(@Param("month") int month, @Param("year") int year);

    // Évolution mensuelle des dépenses sur une année
    @Query("SELECT MONTH(e.expenseDate), SUM(e.amount) FROM Expense e WHERE YEAR(e.expenseDate) = :year GROUP BY MONTH(e.expenseDate) ORDER BY MONTH(e.expenseDate)")
    List<Object[]> getMonthlyEvolution(@Param("year") int year);
}