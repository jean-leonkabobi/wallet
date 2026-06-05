package com.kabobi.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyEvolutionDTO {

    private int month;
    private String monthName;
    private BigDecimal totalExpenses;
    private BigDecimal totalRevenues;
}