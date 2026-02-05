package com.example.ShopHub.Service;

import com.example.ShopHub.Entity.Category;
import com.example.ShopHub.Entity.Product;
import com.example.ShopHub.Repository.CategoryRepository;
import com.example.ShopHub.Repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Product createProduct(Product product) {

        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(generateSlug(product.getName()));
        }

        if (product.getCategory() != null) {
            categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found!"));
        }

        if (product.getSku() == null || product.getSku().isEmpty()) {
            product.setSku(generateSku(product.getName())); // dùng hàm generateSku ở dưới
        }

        return productRepository.save(product);
    }

    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id)
                .filter(p -> !p.isDeleted());
    }

    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .filter(p -> !p.isDeleted());
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.isActive() && !p.isDeleted())
                .toList();
    }

    public List<Product> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword);
    }

    @Transactional
    public Product updateProduct(UUID id, Product updatedProduct) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Can not find product with id: " + id));

        if (updatedProduct.getName() != null && !updatedProduct.getName().isEmpty()) {
            existing.setName(updatedProduct.getName());
            existing.setSlug(generateSlug(updatedProduct.getName()));
        }
        if (updatedProduct.getDescription() != null) {
            existing.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getPrice() != null) {
            existing.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getSalePrice() != null) {
            existing.setSalePrice(updatedProduct.getSalePrice());
        }
        if (updatedProduct.getStock() != null) {
            existing.setStock(updatedProduct.getStock());
        }
        if (updatedProduct.getSku() != null) {
            existing.setSku(updatedProduct.getSku());
        }
        if (updatedProduct.getImages() != null) {
            existing.setImages(updatedProduct.getImages());
        }
        if (updatedProduct.getAttributes() != null) {
            existing.setAttributes(updatedProduct.getAttributes());
        }
        if (updatedProduct.getCategory() != null) {
            Category category = (Category) categoryRepository.findById(updatedProduct.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Cannot find category with id: " +  updatedProduct.getCategory().getId()));
            existing.setCategory(category);
        }

        return productRepository.save(existing);
    }

    @Transactional
    public Product updateStock(UUID id, Integer stockChange) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot find product with id: " + id));

        int newStock = product.getStock() + stockChange;
        if (newStock < 0) {
            throw new RuntimeException("New stock cannot be less than 0");
        }

        product.setStock(newStock);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateRating(UUID id, BigDecimal newRating) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Can not find product with id: " + id));

        BigDecimal currentRating = product.getRatingAvg();
        int currentCount = product.getReviewCount();

        BigDecimal totalRating = currentRating.multiply(BigDecimal.valueOf(currentCount))
                .add(newRating);
        int newCount = currentCount + 1;

        BigDecimal avgRating = totalRating.divide(
                BigDecimal.valueOf(newCount),
                2,
                BigDecimal.ROUND_HALF_UP
        );

        product.setRatingAvg(avgRating);
        product.setReviewCount(newCount);

        return productRepository.save(product);
    }

    @Transactional
    public Product toggleActive(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot find product with id: " + id));

        product.setActive(!product.isActive());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot find product with id: " + id));

        product.setActive(false);
        product.setDeleted(true);
        productRepository.save(product);
    }

    public List<Product> getSaleProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.isActive() && !p.isDeleted()
                        && p.getSalePrice() != null
                        && p.getSalePrice().compareTo(p.getPrice()) < 0)
                .toList();
    }

    public List<Product> getLatestProducts(int limit) {
        return productRepository.findAll().stream()
                .filter(p -> p.isActive() && !p.isDeleted())
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .limit(limit)
                .toList();
    }

    public List<Product> getBestSellingProducts(int limit) {
        return productRepository.findAll().stream()
                .filter(p -> p.isActive() && !p.isDeleted())
                .sorted((p1, p2) -> p2.getReviewCount().compareTo(p1.getReviewCount()))
                .limit(limit)
                .toList();
    }

    public boolean isInStock(UUID id) {
        return productRepository.findById(id)
                .map(p -> p.getStock() > 0 && p.isActive() && !p.isDeleted())
                .orElse(false);
    }

    public boolean hasEnoughStock(UUID id, Integer quantity) {
        return productRepository.findById(id)
                .map(p -> p.getStock() >= quantity && p.isActive() && !p.isDeleted())
                .orElse(false);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("đ", "d")
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private String generateSku(String name) {
        String prefix = name.substring(0, Math.min(3, name.length())).toUpperCase();
        long timestamp = System.currentTimeMillis();
        return prefix + "-" + timestamp;
    }
}