package com.example.ShopHub.Mapper;

import com.example.ShopHub.DTO.ProductCreateRequest;
import com.example.ShopHub.DTO.ProductDTO;
import com.example.ShopHub.DTO.ProductUpdateRequest;
import com.example.ShopHub.Entity.Category;
import com.example.ShopHub.Entity.Product;
import com.example.ShopHub.Repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductMapper {

    @Autowired
    private CategoryRepository categoryRepository;

    public ProductDTO toDTO(Product product) {
        if (product == null) return null;

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setStock(product.getStock());
        dto.setSku(product.getSku());
        dto.setImages(product.getImages());
        dto.setAttributes(product.getAttributes());
        dto.setRatingAvg(product.getRatingAvg());
        dto.setReviewCount(product.getReviewCount());
        dto.setActive(product.isActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        return dto;
    }

    public Product toEntity(ProductCreateRequest request) {
        if (request == null) return null;

        Product product = new Product();
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSalePrice(request.getSalePrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        product.setImages(request.getImages());
        product.setAttributes(request.getAttributes());
        product.setActive(request.isActive());
        product.setRatingAvg(BigDecimal.ZERO);
        product.setReviewCount(0);

        if (request.getCategoryId() != null) {
            Category category = (Category) categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }

        return product;
    }

    public void updateEntityFromRequest(Product product, ProductUpdateRequest request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getSalePrice() != null) {
            product.setSalePrice(request.getSalePrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getSku() != null) {
            product.setSku(request.getSku());
        }
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }
        if (request.getAttributes() != null) {
            product.setAttributes(request.getAttributes());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getCategoryId() != null) {
            Category category = (Category) categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }
    }
}