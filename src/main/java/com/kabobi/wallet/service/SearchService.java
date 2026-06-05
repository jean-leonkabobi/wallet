package com.kabobi.wallet.service;

import com.kabobi.wallet.dto.TransactionDTO;
import com.kabobi.wallet.dto.TransactionSearchDTO;
import com.kabobi.wallet.model.Expense;
import com.kabobi.wallet.model.Revenue;
import com.kabobi.wallet.repository.ExpenseRepository;
import com.kabobi.wallet.repository.RevenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    @Autowired
    private RevenueRepository revenueRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    public List<TransactionDTO> searchTransactions(TransactionSearchDTO searchDTO) {
        List<TransactionDTO> transactions = new ArrayList<>();

        // Récupérer les revenus si nécessaire
        if (searchDTO.getType() == null || "REVENUE".equalsIgnoreCase(searchDTO.getType())) {
            List<TransactionDTO> revenueTransactions = searchRevenues(searchDTO);
            transactions.addAll(revenueTransactions);
        }

        // Récupérer les dépenses si nécessaire
        if (searchDTO.getType() == null || "EXPENSE".equalsIgnoreCase(searchDTO.getType())) {
            List<TransactionDTO> expenseTransactions = searchExpenses(searchDTO);
            transactions.addAll(expenseTransactions);
        }

        // Trier les résultats
        transactions = sortTransactions(transactions, searchDTO.getSortBy(), searchDTO.getSortDirection());

        return transactions;
    }

    private List<TransactionDTO> searchRevenues(TransactionSearchDTO searchDTO) {
        Stream<Revenue> revenueStream = revenueRepository.findAll().stream();

        // Appliquer les filtres
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
            String keyword = searchDTO.getKeyword().toLowerCase();
            revenueStream = revenueStream.filter(r ->
                    r.getDescription().toLowerCase().contains(keyword) ||
                            (r.getNotes() != null && r.getNotes().toLowerCase().contains(keyword))
            );
        }

        if (searchDTO.getStartDate() != null) {
            revenueStream = revenueStream.filter(r ->
                    !r.getRevenueDate().isBefore(searchDTO.getStartDate())
            );
        }

        if (searchDTO.getEndDate() != null) {
            revenueStream = revenueStream.filter(r ->
                    !r.getRevenueDate().isAfter(searchDTO.getEndDate())
            );
        }

        if (searchDTO.getMinAmount() != null) {
            revenueStream = revenueStream.filter(r ->
                    r.getAmount().compareTo(searchDTO.getMinAmount()) >= 0
            );
        }

        if (searchDTO.getMaxAmount() != null) {
            revenueStream = revenueStream.filter(r ->
                    r.getAmount().compareTo(searchDTO.getMaxAmount()) <= 0
            );
        }

        return revenueStream.map(this::convertRevenueToTransaction).collect(Collectors.toList());
    }

    private List<TransactionDTO> searchExpenses(TransactionSearchDTO searchDTO) {
        Stream<Expense> expenseStream = expenseRepository.findAll().stream();

        // Appliquer les filtres
        if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().isEmpty()) {
            String keyword = searchDTO.getKeyword().toLowerCase();
            expenseStream = expenseStream.filter(e ->
                    e.getDescription().toLowerCase().contains(keyword) ||
                            (e.getNotes() != null && e.getNotes().toLowerCase().contains(keyword))
            );
        }

        if (searchDTO.getCategoryId() != null) {
            expenseStream = expenseStream.filter(e ->
                    e.getCategory().getId().equals(searchDTO.getCategoryId())
            );
        }

        if (searchDTO.getStartDate() != null) {
            expenseStream = expenseStream.filter(e ->
                    !e.getExpenseDate().isBefore(searchDTO.getStartDate())
            );
        }

        if (searchDTO.getEndDate() != null) {
            expenseStream = expenseStream.filter(e ->
                    !e.getExpenseDate().isAfter(searchDTO.getEndDate())
            );
        }

        if (searchDTO.getMinAmount() != null) {
            expenseStream = expenseStream.filter(e ->
                    e.getAmount().compareTo(searchDTO.getMinAmount()) >= 0
            );
        }

        if (searchDTO.getMaxAmount() != null) {
            expenseStream = expenseStream.filter(e ->
                    e.getAmount().compareTo(searchDTO.getMaxAmount()) <= 0
            );
        }

        return expenseStream.map(this::convertExpenseToTransaction).collect(Collectors.toList());
    }

    private TransactionDTO convertRevenueToTransaction(Revenue revenue) {
        return TransactionDTO.builder()
                .id(revenue.getId())
                .type("REVENUE")
                .description(revenue.getDescription())
                .amount(revenue.getAmount())
                .date(revenue.getRevenueDate())
                .categoryName(null)
                .notes(revenue.getNotes())
                .build();
    }

    private TransactionDTO convertExpenseToTransaction(Expense expense) {
        return TransactionDTO.builder()
                .id(expense.getId())
                .type("EXPENSE")
                .description(expense.getDescription())
                .amount(expense.getAmount().negate()) // Montant négatif pour les dépenses
                .date(expense.getExpenseDate())
                .categoryName(expense.getCategory().getName())
                .notes(expense.getNotes())
                .build();
    }

    private List<TransactionDTO> sortTransactions(List<TransactionDTO> transactions, String sortBy, String sortDirection) {
        Comparator<TransactionDTO> comparator;

        switch (sortBy != null ? sortBy.toLowerCase() : "date") {
            case "amount":
                comparator = Comparator.comparing(TransactionDTO::getAmount);
                break;
            case "description":
                comparator = Comparator.comparing(TransactionDTO::getDescription);
                break;
            case "date":
            default:
                comparator = Comparator.comparing(TransactionDTO::getDate);
                break;
        }

        if ("asc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return transactions.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}