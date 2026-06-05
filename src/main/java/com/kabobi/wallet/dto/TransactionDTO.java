package com.kabobi.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;
    private String type; // "REVENUE" ou "EXPENSE"
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String categoryName;
    private String notes;
}