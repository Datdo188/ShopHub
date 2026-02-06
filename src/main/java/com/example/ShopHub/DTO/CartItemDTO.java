package com.example.ShopHub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productImage;
    private String productSlug;
    private Integer quantity;
    private String selectedVariant;
    private BigDecimal priceSnapshot;
    private BigDecimal subtotal;
    private Integer availableStock;
    private boolean inStock;
}