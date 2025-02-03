package com.theara.postgres.service;

import com.theara.postgres.model.response.Category;
import java.util.List;
import java.util.Optional;


public interface CategoryService {

    // Get all categories
    List<Category> getAllCategories();

    // Get category by ID
    Optional<Category> getCategoryById(Long id);

    // Save a new category
    Category saveCategory(Category category);

    // Update an existing category
    Category updateCategory(Long id, Category category);

    // Delete a category by ID
    boolean deleteCategory(Long id);
}


