package com.example.ShopHub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private UUID id;
    private UUID userId;
    private List<CartItemDTO> items;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper method
    public BigDecimal calculateTotalAmount() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()   // items is "List", use stream to loop through items
                .map(CartItemDTO::getSubtotal) // get subtotal from each item
                .reduce(BigDecimal.ZERO, BigDecimal::add); // addition
    }
}