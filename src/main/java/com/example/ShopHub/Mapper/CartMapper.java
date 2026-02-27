package com.example.ShopHub.Mapper;

import com.example.ShopHub.DTO.CartDTO;
import com.example.ShopHub.DTO.CartItemDTO;
import com.example.ShopHub.Entity.Cart;
import com.example.ShopHub.Entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDTO toDTO(Cart cart) {
        if (cart == null) return null;

        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setTotalItems(cart.getTotalItems());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        if (cart.getItems() != null) {
            dto.setItems(cart.getItems().stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList()));
        }

        dto.setTotalAmount(dto.calculateTotalAmount());

        return dto;
    }

    public CartItemDTO toItemDTO(CartItem item) {
        if (item == null) return null;

        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setSelectedVariant(item.getSelectedVariant());
        dto.setPriceSnapshot(item.getPriceSnapshot());
        dto.setSubtotal(item.getSubtotal());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            dto.setProductSlug(item.getProduct().getSlug());
            dto.setAvailableStock(item.getProduct().getStock());
            dto.setInStock(item.getProduct().getStock() > 0);

            // Lấy ảnh đầu tiên nếu có
            if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                dto.setProductImage(item.getProduct().getImages().get(0));
            }
        }

        return dto;
    }
}