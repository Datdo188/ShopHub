package com.example.ShopHub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stock;
    private String sku;
    private List<String> images;
    private Map<String, String> attributes;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal ratingAvg;
    private Integer reviewCount;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed field
    public BigDecimal getEffectivePrice() {
        return salePrice != null ? salePrice : price;
    }

    public boolean isOnSale() {
        return salePrice != null && salePrice.compareTo(price) < 0;
    }

    public BigDecimal getDiscountPercentage() {
        if (salePrice != null && price.compareTo(BigDecimal.ZERO) > 0) {
            return price.subtract(salePrice)
                    .divide(price, 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
}