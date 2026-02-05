package com.example.ShopHub.Service;

import com.example.ShopHub.Entity.Order;
import com.example.ShopHub.Entity.OrderItem;
import com.example.ShopHub.Entity.Product;
import com.example.ShopHub.Entity.User;
import com.example.ShopHub.Enum.OrderStatus;
import com.example.ShopHub.Repository.OrderRepository;
import com.example.ShopHub.Repository.ProductRepository;
import com.example.ShopHub.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order createOrder(Order order) {

        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Can not find user with id: " + order.getUser().getId()));

        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber(generateOrderNumber());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Can not find product with id: " + item.getProduct().getId()));

            if (!product.isActive() || product.isDeleted()) {
                throw new RuntimeException("Product " + product.getName() + " unactivated");
            }

            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Product " + product.getName() + " not enough stock");
            }

            item.setProductNameSnapshot(product.getName());

            BigDecimal currentPrice = product.getSalePrice() != null
                    ? product.getSalePrice()
                    : product.getPrice();
            item.setPrice(currentPrice);

            BigDecimal subtotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(subtotal);
            totalAmount = totalAmount.add(subtotal);

            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);

            item.setOrder(order);
        }

        order.setTotalAmount(totalAmount);

        BigDecimal finalAmount = totalAmount
                .add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO)
                .subtract(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);

        order.setFinalAmount(finalAmount);

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
        if (order.getPaymentStatus() == null) {
            order.setPaymentStatus("pending");
        }

        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(UUID id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return Optional.ofNullable(orderRepository.findByOrderNumber(orderNumber));
    }

    public List<Order> getOrdersByUser(UUID userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order updateOrderStatus(UUID id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Can not  find order with id: " + id));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        if (newStatus == OrderStatus.PAID) {
            order.setPaymentStatus("paid");
        } else if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED) {
            restoreStock(order);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(UUID id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Can not find order with id: " + id));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Order cannot be cancelled in  " + order.getStatus() + " status");
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (reason != null && !reason.isEmpty()) {
            order.setNote(order.getNote() != null
                    ? order.getNote() + "\nReason: " + reason
                    : "Reason: " + reason);
        }

        restoreStock(order);

        orderRepository.save(order);
    }

    @Transactional
    public Order confirmOrder(UUID id) {
        return updateOrderStatus(id, OrderStatus.CONFIRMED);
    }

    @Transactional
    public Order markAsPaid(UUID id) {
        return updateOrderStatus(id, OrderStatus.PAID);
    }

    @Transactional
    public Order markAsShipped(UUID id) {
        return updateOrderStatus(id, OrderStatus.SHIPPED);
    }

    @Transactional
    public Order markAsDelivered(UUID id) {
        return updateOrderStatus(id, OrderStatus.DELIVERED);
    }

    @Transactional
    public Order requestReturn(UUID id, String reason) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot find order with id: " + id));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Return can only be made once the order has been delivered");
        }

        LocalDateTime deliveryDate = order.getUpdatedAt();
        if (deliveryDate.plusDays(7).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("The return time has passed");
        }

        order.setStatus(OrderStatus.RETURNED);
        if (reason != null && !reason.isEmpty()) {
            order.setNote(order.getNote() != null
                    ? order.getNote() + "\nReturn reason: " + reason
                    : "Return reason: " + reason);
        }

        restoreStock(order);

        return orderRepository.save(order);
    }

    public BigDecimal calculateRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED
                        && o.getCreatedAt().isAfter(startDate)
                        && o.getCreatedAt().isBefore(endDate))
                .toList();

        return orders.stream()
                .map(Order::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).size();
    }

    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = String.format("%04d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String timePart = String.format("%02d%02d%02d",
                now.getHour(), now.getMinute(), now.getSecond());
        return "DH" + datePart + timePart;
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PAID || newStatus == OrderStatus.CANCELLED;
            case PAID -> newStatus == OrderStatus.SHIPPED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED -> newStatus == OrderStatus.RETURNED;
            default -> false;
        };

        if (!isValid) {
            throw new RuntimeException(
                    "Cannot convert from " + currentStatus + " to " + newStatus
            );
        }
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }
}