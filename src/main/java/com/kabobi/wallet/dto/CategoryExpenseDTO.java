package com.kabobi.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpenseDTO {

    private String categoryName;
    private BigDecimal totalAmount;
    private Double percentage;
}