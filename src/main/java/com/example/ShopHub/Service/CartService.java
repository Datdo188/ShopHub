package com.example.ShopHub.Service;

import com.example.ShopHub.Entity.Cart;
import com.example.ShopHub.Entity.CartItem;
import com.example.ShopHub.Entity.Product;
import com.example.ShopHub.Entity.User;
import com.example.ShopHub.Repository.CartItemRepository;
import com.example.ShopHub.Repository.CartRepository;
import com.example.ShopHub.Repository.ProductRepository;
import com.example.ShopHub.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    Cart cart = new Cart();
                    cart.setUser(user);
                    cart.setTotalItems(0);
                    return cartRepository.save(cart);
                });
    }

    public Optional<Cart> getCartByUserId(UUID userId) {
        return cartRepository.findByUserId(userId);
    }

    @Transactional
    public CartItem addToCart(UUID userId, UUID productId, Integer quantity, String selectedVariant) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.isActive() || product.isDeleted()) {
            throw new RuntimeException("Product is not available");
        }

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient product stock");
        }

        Cart cart = getOrCreateCart(userId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId)
                        && isSameVariant(item.getSelectedVariant(), selectedVariant))
                .findFirst();

        CartItem cartItem;
        if (existingItem.isPresent()) {

            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Quantity exceeds available stock");
            }

            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setSelectedVariant(selectedVariant);

            BigDecimal currentPrice = product.getSalePrice() != null
                    ? product.getSalePrice()
                    : product.getPrice();
            cartItem.setPriceSnapshot(currentPrice);

            cart.addItem(cartItem);
        }

        cartItemRepository.save(cartItem);
        cartRepository.save(cart);

        return cartItem;
    }

    @Transactional
    public CartItem updateCartItemQuantity(UUID cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        Product product = cartItem.getProduct();
        if (product.getStock() < quantity) {
            throw new RuntimeException("Quantity exceeds available stock");
        }

        cartItem.setQuantity(quantity);
        CartItem updated = cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        cart.setTotalItems(cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum());
        cartRepository.save(cart);

        return updated;
    }

    @Transactional
    public void removeFromCart(UUID cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);

        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.clearItems();
        cartItemRepository.deleteByCartId(cart.getId());
        cartRepository.save(cart);
    }

    public BigDecimal calculateCartTotal(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        return cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isSameVariant(String variant1, String variant2) {
        if (variant1 == null && variant2 == null) return true;
        if (variant1 == null || variant2 == null) return false;
        return variant1.equals(variant2);
    }

    @Transactional
    public void syncCartPrices(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            BigDecimal currentPrice = product.getSalePrice() != null
                    ? product.getSalePrice()
                    : product.getPrice();

            item.setPriceSnapshot(currentPrice);
        }

        cartRepository.save(cart);
    }

    public void validateCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (!product.isActive() || product.isDeleted()) {
                throw new RuntimeException("Product " + product.getName() + " is no longer available");
            }

            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Product " + product.getName() + " does not have enough stock");
            }
        }
    }
}
