package com.kabobi.wallet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequestDTO {

    @NotNull(message = "La date de début est obligatoire")
    private LocalDate startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate endDate;

    private String format; // "PDF" ou "CSV"
    private Long categoryId; // Optionnel, pour filtrer par catégorie
    private String type; // "REVENUE", "EXPENSE" ou "ALL"
}