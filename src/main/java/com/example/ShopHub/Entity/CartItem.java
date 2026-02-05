package com.example.ShopHub.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
public class CartItem extends Base {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(columnDefinition = "jsonb")
    private String selectedVariant;

    @Column(precision = 15, scale = 2)
    private BigDecimal priceSnapshot;

    public BigDecimal getSubtotal() {
        if (priceSnapshot != null && quantity != null) {
            return priceSnapshot.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}