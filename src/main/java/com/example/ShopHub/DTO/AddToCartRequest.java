package com.example.ShopHub.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

    @NotNull(message = "Product ID cannot be blank!")
    private UUID productId;

    @NotNull(message = "Quantity cannot be blank!")
    @Min(value = 1, message = "Quantity must greater than 0")
    private Integer quantity;

    private String selectedVariant;
}