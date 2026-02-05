package com.example.ShopHub.Repository;

import com.example.ShopHub.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByCategoryIdAndActiveTrue(UUID categoryId);

    Optional<Product> findBySlug(String slug);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    Optional<Product> findBySlugAndActiveTrue(String slug);
}