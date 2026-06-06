package com.kabobi.wallet.controller;

import com.kabobi.wallet.dto.CategoryExpenseDTO;
import com.kabobi.wallet.dto.DashboardDTO;
import com.kabobi.wallet.dto.DateRangeDTO;
import com.kabobi.wallet.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(
            @RequestParam(required = false, defaultValue = "0") int month,
            @RequestParam(required = false, defaultValue = "0") int year) {
        DashboardDTO dashboard = dashboardService.getDashboard(month, year);
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/expenses-by-category")
    public ResponseEntity<List<CategoryExpenseDTO>> getExpensesByCategory(
            @RequestParam(required = false) Long categoryId,
            @Valid @RequestBody DateRangeDTO dateRange) {
        List<CategoryExpenseDTO> expenses = dashboardService.getExpensesByDateRange(
                categoryId, dateRange.getStartDate(), dateRange.getEndDate());
        return ResponseEntity.ok(expenses);
    }
}