package com.kabobi.wallet.service;

import com.kabobi.wallet.dto.ExpenseDTO;
import com.kabobi.wallet.exception.ResourceNotFoundException;
import com.kabobi.wallet.model.Category;
import com.kabobi.wallet.model.Expense;
import com.kabobi.wallet.repository.CategoryRepository;
import com.kabobi.wallet.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Conversion Entity -> DTO
    private ExpenseDTO convertToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setDescription(expense.getDescription());
        dto.setAmount(expense.getAmount());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setCategoryId(expense.getCategory().getId());
        dto.setNotes(expense.getNotes());
        return dto;
    }

    // Conversion DTO -> Entity
    private Expense convertToEntity(ExpenseDTO dto) {
        Expense expense = new Expense();
        expense.setDescription(dto.getDescription());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setNotes(dto.getNotes());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", dto.getCategoryId()));
        expense.setCategory(category);

        return expense;
    }

    // CRUD Operations

    public ExpenseDTO createExpense(ExpenseDTO expenseDTO) {
        Expense expense = convertToEntity(expenseDTO);
        Expense savedExpense = expenseRepository.save(expense);
        return convertToDTO(savedExpense);
    }

    public List<ExpenseDTO> getAllExpenses() {
        return expenseRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        return convertToDTO(expense);
    }

    public ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        expense.setDescription(expenseDTO.getDescription());
        expense.setAmount(expenseDTO.getAmount());
        expense.setExpenseDate(expenseDTO.getExpenseDate());
        expense.setNotes(expenseDTO.getNotes());

        if (!expense.getCategory().getId().equals(expenseDTO.getCategoryId())) {
            Category category = categoryRepository.findById(expenseDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", expenseDTO.getCategoryId()));
            expense.setCategory(category);
        }

        Expense updatedExpense = expenseRepository.save(expense);
        return convertToDTO(updatedExpense);
    }

    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense", "id", id);
        }
        expenseRepository.deleteById(id);
    }

    // Méthodes additionnelles

    public List<ExpenseDTO> searchExpenses(String keyword) {
        return expenseRepository.findByDescriptionContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesByCategoryAndMonth(Long categoryId, int month, int year) {
        return expenseRepository.findByCategoryAndMonthAndYear(categoryId, month, year)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}