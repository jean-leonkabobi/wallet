package com.kabobi.wallet.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {

    private Long id;

    @NotNull(message = "Le montant du budget est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;

    @NotNull(message = "Le mois est obligatoire")
    @Min(value = 1, message = "Le mois doit être entre 1 et 12")
    @Max(value = 12, message = "Le mois doit être entre 1 et 12")
    private Integer month;

    @NotNull(message = "L'année est obligatoire")
    @Min(value = 2024, message = "L'année doit être 2024 ou plus")
    private Integer year;

    private Long categoryId; // null = budget global

    @DecimalMin(value = "0.01", message = "Le seuil doit être supérieur à 0")
    @DecimalMax(value = "100.00", message = "Le seuil doit être inférieur ou égal à 100")
    private BigDecimal alertThreshold; // Pourcentage (ex: 80.00 pour 80%)

    private Boolean isActive;
}