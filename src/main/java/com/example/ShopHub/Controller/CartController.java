package com.example.ShopHub.Controller;

import com.example.ShopHub.DTO.AddToCartRequest;
import com.example.ShopHub.DTO.CartDTO;
import com.example.ShopHub.DTO.UpdateCartItemRequest;
import com.example.ShopHub.Entity.Cart;
import com.example.ShopHub.Entity.CartItem;
import com.example.ShopHub.Exception.ResourceNotFoundException;
import com.example.ShopHub.Exception.UnauthorizedException;
import com.example.ShopHub.Mapper.CartMapper;
import com.example.ShopHub.Security.CustomUserDetailsService;
import com.example.ShopHub.Service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Get current user's cart
     */
    @GetMapping
    public ResponseEntity<CartDTO> getCart() {
        UUID userId = getCurrentUserId();
        Cart cart = cartService.getCartByUserId(userId)
                .orElseGet(() -> cartService.getOrCreateCart(userId));
        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }

    /**
     * Add item to cart
     */
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addToCart(@Valid @RequestBody AddToCartRequest request) {
        UUID userId = getCurrentUserId();

        cartService.addToCart(
                userId,
                request.getProductId(),
                request.getQuantity(),
                request.getSelectedVariant()
        );

        Cart cart = cartService.getCartByUserId(userId).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(cartMapper.toDTO(cart));
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        UUID userId = getCurrentUserId();

        cartService.updateCartItemQuantity(itemId, request.getQuantity());

        Cart cart = cartService.getCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable UUID itemId) {
        UUID userId = getCurrentUserId();

        cartService.removeFromCart(itemId);

        Cart cart = cartService.getCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }

    /**
     * Clear cart
     */
    @DeleteMapping
    public ResponseEntity<?> clearCart() {
        UUID userId = getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.ok().body("Cart cleared successfully");
    }

    /**
     * Get cart total
     */
    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getCartTotal() {
        UUID userId = getCurrentUserId();
        BigDecimal total = cartService.calculateCartTotal(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    /**
     * Sync cart prices with current product prices
     */
    @PostMapping("/sync-prices")
    public ResponseEntity<CartDTO> syncPrices() {
        UUID userId = getCurrentUserId();
        cartService.syncCartPrices(userId);

        Cart cart = cartService.getCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return ResponseEntity.ok(cartMapper.toDTO(cart));
    }

    /**
     * Validate cart before checkout
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCart() {
        UUID userId = getCurrentUserId();

        try {
            cartService.validateCart(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("message", "Cart is valid for checkout");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Helper method to get current user ID
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String email = authentication.getName();
        return userDetailsService.loadUserEntityByEmail(email).getId();
    }
}