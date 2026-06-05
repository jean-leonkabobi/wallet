package com.kabobi.wallet.service;

import com.kabobi.wallet.exception.ResourceNotFoundException;
import com.kabobi.wallet.model.Category;
import com.kabobi.wallet.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", name));
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Une catégorie avec ce nom existe déjà : " + category.getName());
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getName().equals(categoryDetails.getName()) &&
                categoryRepository.existsByName(categoryDetails.getName())) {
            throw new IllegalArgumentException("Une catégorie avec ce nom existe déjà : " + categoryDetails.getName());
        }

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}