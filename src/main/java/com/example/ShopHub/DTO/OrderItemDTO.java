package com.example.ShopHub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private UUID id;
    private UUID productId;
    private String productNameSnapshot;
    private String variantSnapshot;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}