package com.kabobi.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSearchDTO {

    private String type; // "REVENUE", "EXPENSE" ou null pour les deux
    private String keyword;
    private Long categoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String sortBy = "date"; // "date", "amount", "description"
    private String sortDirection = "desc"; // "asc" ou "desc"
}