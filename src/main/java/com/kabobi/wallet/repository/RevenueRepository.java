package com.kabobi.wallet.repository;

import com.kabobi.wallet.model.Revenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RevenueRepository extends JpaRepository<Revenue, Long> {

    // Trouver tous les revenus par mois/année
    @Query("SELECT r FROM Revenue r WHERE MONTH(r.revenueDate) = :month AND YEAR(r.revenueDate) = :year")
    List<Revenue> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Trouver les revenus entre deux dates
    List<Revenue> findByRevenueDateBetween(LocalDate startDate, LocalDate endDate);

    // Calculer le total des revenus pour un mois/année donné
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Revenue r WHERE MONTH(r.revenueDate) = :month AND YEAR(r.revenueDate) = :year")
    BigDecimal getTotalRevenueByMonthAndYear(@Param("month") int month, @Param("year") int year);

    // Recherche par description (contenant)
    List<Revenue> findByDescriptionContainingIgnoreCase(String keyword);

    // Trouver tous les revenus par date
    List<Revenue> findByRevenueDate(LocalDate date);
}