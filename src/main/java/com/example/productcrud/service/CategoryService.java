package com.example.productcrud.service;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAllByOwner(User owner) {
        return categoryRepository.findByOwner(owner);
    }

    public Optional<Category> findByIdAndOwner(Long id, User owner) {
        return categoryRepository.findByIdAndOwner(id, owner);
    }

    public boolean existsByNameAndOwner(String name, User owner) {
        return categoryRepository.existsByNameAndOwner(name, owner);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteByIdAndOwner(Long id, User owner) {
        categoryRepository.findByIdAndOwner(id, owner)
                .ifPresent(categoryRepository::delete);
    }
}