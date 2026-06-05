package com.kabobi.wallet.controller;

import com.kabobi.wallet.dto.BudgetAlertDTO;
import com.kabobi.wallet.dto.BudgetDTO;
import com.kabobi.wallet.dto.BudgetStatusDTO;
import com.kabobi.wallet.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin("*")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetDTO> createBudget(@Valid @RequestBody BudgetDTO budgetDTO) {
        BudgetDTO createdBudget = budgetService.createBudget(budgetDTO);
        return new ResponseEntity<>(createdBudget, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getAllBudgets() {
        List<BudgetDTO> budgets = budgetService.getAllBudgets();
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable Long id) {
        BudgetDTO budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/by-month")
    public ResponseEntity<List<BudgetDTO>> getBudgetsByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year) {
        List<BudgetDTO> budgets = budgetService.getBudgetsByMonthAndYear(month, year);
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> updateBudget(@PathVariable Long id, @Valid @RequestBody BudgetDTO budgetDTO) {
        BudgetDTO updatedBudget = budgetService.updateBudget(id, budgetDTO);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<BudgetStatusDTO> getBudgetStatus(@PathVariable Long id) {
        BudgetStatusDTO status = budgetService.getBudgetStatus(id);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/status")
    public ResponseEntity<List<BudgetStatusDTO>> getAllBudgetsStatus(
            @RequestParam int month,
            @RequestParam int year) {
        List<BudgetStatusDTO> statuses = budgetService.getAllBudgetsStatus(month, year);
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<BudgetAlertDTO>> checkBudgetAlerts(
            @RequestParam int month,
            @RequestParam int year) {
        List<BudgetAlertDTO> alerts = budgetService.checkBudgetAlerts(month, year);
        return ResponseEntity.ok(alerts);
    }
}