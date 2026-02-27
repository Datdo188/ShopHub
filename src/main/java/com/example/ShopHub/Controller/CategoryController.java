package com.example.ShopHub.Controller;

import com.example.ShopHub.DTO.CategoryCreateRequest;
import com.example.ShopHub.DTO.CategoryDTO;
import com.example.ShopHub.Entity.Category;
import com.example.ShopHub.Exception.ResourceNotFoundException;
import com.example.ShopHub.Repository.CategoryRepository;
import com.example.ShopHub.Service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Get all categories (Public)
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .filter(c -> !c.isDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDTOs);
    }

    /**
     * Get root categories (Public)
     */
    @GetMapping("/root")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        List<Category> categories = categoryService.getRootCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .filter(c -> !c.isDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDTOs);
    }

    /**
     * Get category by ID (Public)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable UUID id) {
        Category category = categoryService.getCategoryById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return ResponseEntity.ok(toDTO(category));
    }

    /**
     * Get category by slug (Public)
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        Category category = categoryService.getCategoryBySlug(slug)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return ResponseEntity.ok(toDTO(category));
    }

    /**
     * Get sub-categories (Public)
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryDTO>> getSubCategories(@PathVariable UUID id) {
        List<Category> categories = categoryService.getSubCategories(id);
        List<CategoryDTO> categoryDTOs = categories.stream()
                .filter(c -> !c.isDeleted())
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDTOs);
    }

    /**
     * Create category (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setImageUrl(request.getImageUrl());

        if (request.getParentId() != null) {
            Category parent = (Category) categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", "id", request.getParentId()));
            category.setParent(parent);
        }

        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(createdCategory));
    }

    /**
     * Update category (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        Category updated = new Category();
        updated.setName(request.getName());
        updated.setImageUrl(request.getImageUrl());

        if (request.getParentId() != null) {
            Category parent = (Category) categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", "id", request.getParentId()));
            updated.setParent(parent);
        }

        Category updatedCategory = categoryService.updateCategory(id, updated);
        return ResponseEntity.ok(toDTO(updatedCategory));
    }

    /**
     * Delete category (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().body("Category deleted successfully");
    }

    /**
     * Helper method to convert Category to DTO
     */
    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setImageUrl(category.getImageUrl());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }

        if (category.getChildren() != null) {
            List<CategoryDTO> children = category.getChildren().stream()
                    .filter(c -> !c.isDeleted())
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            dto.setChildren(children);
        }

        if (category.getProducts() != null) {
            dto.setProductCount(category.getProducts().size());
        }

        return dto;
    }
}