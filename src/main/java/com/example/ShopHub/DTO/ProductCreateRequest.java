package com.example.ShopHub.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProductCreateRequest {

    @NotBlank(message = "Product name cannot be blank")
    private String name;

    private String slug;

    private String description;

    @NotNull(message = "Price cannot be blank")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "Sale Price must greater than 0")
    private BigDecimal salePrice;

    @NotNull(message = "Stock cannot be blank")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private String sku;

    private List<String> images;

    private Map<String, String> attributes;

    @NotNull(message = "Category's id is empty")
    private UUID categoryId;

    private boolean active = true;
}