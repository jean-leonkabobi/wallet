package com.kabobi.wallet.repository;

import com.kabobi.wallet.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // Trouver le budget pour un mois/année spécifique
    List<Budget> findByMonthAndYearAndIsActiveTrue(int month, int year);

    // Trouver le budget global (category = null) pour un mois/année
    Optional<Budget> findByMonthAndYearAndCategoryIsNull(int month, int year);

    // Trouver le budget pour une catégorie spécifique et mois/année
    Optional<Budget> findByMonthAndYearAndCategoryId(int month, int year, Long categoryId);

    // Trouver tous les budgets actifs
    List<Budget> findByIsActiveTrue();

    // Vérifier si un budget existe déjà pour ce mois/année/catégorie
    boolean existsByMonthAndYearAndCategoryId(int month, int year, Long categoryId);

    // Vérifier si un budget global existe déjà pour ce mois/année
    boolean existsByMonthAndYearAndCategoryIsNull(int month, int year);
}