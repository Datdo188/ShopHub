package com.example.ShopHub.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "User ID cannot be blank")
    private UUID userId;

    @NotEmpty(message = "Order must have at least 1 item!")
    private List<OrderItemRequest> items;

    @NotBlank(message = "Address cannot be blank")
    private String shippingAddressJson;

    @NotBlank(message = "Choose one payment method!")
    private String paymentMethod;

    private BigDecimal shippingFee = BigDecimal.ZERO;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String note;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID cannot be blank")
        private UUID productId;

        @NotNull(message = "Quantity cannot be blank")
        private Integer quantity;

        private String variantSnapshot;
    }
}