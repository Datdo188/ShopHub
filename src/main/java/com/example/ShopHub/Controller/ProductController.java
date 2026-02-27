package com.example.ShopHub.Controller;

import com.example.ShopHub.DTO.ProductCreateRequest;
import com.example.ShopHub.DTO.ProductDTO;
import com.example.ShopHub.DTO.ProductUpdateRequest;
import com.example.ShopHub.Entity.Product;
import com.example.ShopHub.Exception.ResourceNotFoundException;
import com.example.ShopHub.Mapper.ProductMapper;
import com.example.ShopHub.Service.ProductService;
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
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    /**
     * Get all active products (Public)
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productService.getAllActiveProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Get product by ID (Public)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable UUID id) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ResponseEntity.ok(productMapper.toDTO(product));
    }

    /**
     * Get product by slug (Public)
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String slug) {
        Product product = productService.getProductBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return ResponseEntity.ok(productMapper.toDTO(product));
    }

    /**
     * Search products by name (Public)
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProducts(keyword);
        List<ProductDTO> productDTOs = products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Get products by category (Public)
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable UUID categoryId) {
        List<Product> products = productService.getProductsByCategory(categoryId);
        List<ProductDTO> productDTOs = products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Get sale products (Public)
     */
    @GetMapping("/sale")
    public ResponseEntity<List<ProductDTO>> getSaleProducts() {
        List<Product> products = productService.getSaleProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Get latest products (Public)
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ProductDTO>> getLatestProducts(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Product> products = productService.getLatestProducts(limit);
        List<ProductDTO> productDTOs = products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Get best selling products (Public)
     */
    @GetMapping("/bestsellers")
    public ResponseEntity<List<ProductDTO>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Product> products = productService.getBestSellingProducts(limit);
        List<ProductDTO> productDTOs = products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    /**
     * Create product (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toDTO(createdProduct));
    }

    /**
     * Update product (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productMapper.updateEntityFromRequest(product, request);
        Product updatedProduct = productService.updateProduct(id, product);

        return ResponseEntity.ok(productMapper.toDTO(updatedProduct));
    }

    /**
     * Update product stock (Admin only)
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateStock(
            @PathVariable UUID id,
            @RequestParam Integer stockChange
    ) {
        Product updatedProduct = productService.updateStock(id, stockChange);
        return ResponseEntity.ok(productMapper.toDTO(updatedProduct));
    }

    /**
     * Toggle product active status (Admin only)
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> toggleActive(@PathVariable UUID id) {
        Product updatedProduct = productService.toggleActive(id);
        return ResponseEntity.ok(productMapper.toDTO(updatedProduct));
    }

    /**
     * Delete product (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().body("Product deleted successfully");
    }
}