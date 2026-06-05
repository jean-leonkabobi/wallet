package com.kabobi.wallet.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    private Long id;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 2, max = 200, message = "La description doit contenir entre 2 et 200 caractères")
    private String description;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    @Digits(integer = 8, fraction = 2, message = "Le montant doit avoir maximum 8 chiffres avant la virgule et 2 après")
    private BigDecimal amount;

    @NotNull(message = "La date est obligatoire")
    private LocalDate expenseDate;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    @Size(max = 500, message = "Les notes ne peuvent pas dépasser 500 caractères")
    private String notes;
}