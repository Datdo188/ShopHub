package com.example.ShopHub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stock;
    private String sku;
    private List<String> images;
    private Map<String, String> attributes;
    private UUID categoryId;
    private Boolean active;
}