package com.example.ShopHub.Controller;

import com.example.ShopHub.DTO.OrderCreateRequest;
import com.example.ShopHub.DTO.OrderDTO;
import com.example.ShopHub.Entity.Order;
import com.example.ShopHub.Enum.OrderStatus;
import com.example.ShopHub.Exception.ResourceNotFoundException;
import com.example.ShopHub.Exception.UnauthorizedException;
import com.example.ShopHub.Mapper.OrderMapper;
import com.example.ShopHub.Security.CustomUserDetailsService;
import com.example.ShopHub.Service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Create new order
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        UUID userId = getCurrentUserId();

        // Ensure the order is for the current user
        if (!request.getUserId().equals(userId)) {
            throw new UnauthorizedException("Cannot create order for another user");
        }

        Order order = orderMapper.toEntity(request);
        Order createdOrder = orderService.createOrder(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toDTO(createdOrder));
    }

    /**
     * Get current user's orders
     */
    @GetMapping("/my")
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        UUID userId = getCurrentUserId();
        List<Order> orders = orderService.getOrdersByUser(userId);

        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderDTOs);
    }

    /**
     * Get order by ID (User can only view their own orders)
     */
    @GetMapping("/my/{id}")
    public ResponseEntity<OrderDTO> getMyOrderById(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();

        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Check if order belongs to current user
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view your own orders");
        }

        return ResponseEntity.ok(orderMapper.toDTO(order));
    }

    /**
     * Get order by order number
     */
    @GetMapping("/my/number/{orderNumber}")
    public ResponseEntity<OrderDTO> getMyOrderByNumber(@PathVariable String orderNumber) {
        UUID userId = getCurrentUserId();

        Order order = orderService.getOrderByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        // Check if order belongs to current user
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only view your own orders");
        }

        return ResponseEntity.ok(orderMapper.toDTO(order));
    }

    /**
     * Cancel order (User can only cancel their own orders)
     */
    @PostMapping("/my/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelMyOrder(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason
    ) {
        UUID userId = getCurrentUserId();

        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Check if order belongs to current user
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only cancel your own orders");
        }

        orderService.cancelOrder(id, reason);

        Order cancelledOrder = orderService.getOrderById(id).orElseThrow();
        return ResponseEntity.ok(orderMapper.toDTO(cancelledOrder));
    }

    /**
     * Request return (User can only return their own orders)
     */
    @PostMapping("/my/{id}/return")
    public ResponseEntity<OrderDTO> requestReturn(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason
    ) {
        UUID userId = getCurrentUserId();

        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Check if order belongs to current user
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only return your own orders");
        }

        Order returnedOrder = orderService.requestReturn(id, reason);
        return ResponseEntity.ok(orderMapper.toDTO(returnedOrder));
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all orders (Admin/Seller only)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<Order> orders = orderService.getOrdersByStatus(null);

        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderDTOs);
    }

    /**
     * Get order by ID (Admin/Seller only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable UUID id) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        return ResponseEntity.ok(orderMapper.toDTO(order));
    }

    /**
     * Get orders by status (Admin/Seller only)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);

        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderDTOs);
    }

    /**
     * Update order status (Admin/Seller only)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status
    ) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(orderMapper.toDTO(updatedOrder));
    }

    /**
     * Confirm order (Admin/Seller only)
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<OrderDTO> confirmOrder(@PathVariable UUID id) {
        Order confirmedOrder = orderService.confirmOrder(id);
        return ResponseEntity.ok(orderMapper.toDTO(confirmedOrder));
    }

    /**
     * Mark as paid (Admin/Seller only)
     */
    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<OrderDTO> markAsPaid(@PathVariable UUID id) {
        Order paidOrder = orderService.markAsPaid(id);
        return ResponseEntity.ok(orderMapper.toDTO(paidOrder));
    }

    /**
     * Mark as shipped (Admin/Seller only)
     */
    @PostMapping("/{id}/mark-shipped")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<OrderDTO> markAsShipped(@PathVariable UUID id) {
        Order shippedOrder = orderService.markAsShipped(id);
        return ResponseEntity.ok(orderMapper.toDTO(shippedOrder));
    }

    /**
     * Mark as delivered (Admin/Seller only)
     */
    @PostMapping("/{id}/mark-delivered")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<OrderDTO> markAsDelivered(@PathVariable UUID id) {
        Order deliveredOrder = orderService.markAsDelivered(id);
        return ResponseEntity.ok(orderMapper.toDTO(deliveredOrder));
    }

    /**
     * Get order statistics (Admin only)
     */
    @GetMapping("/stats/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getOrderStats() {
        Map<String, Long> stats = new HashMap<>();

        for (OrderStatus status : OrderStatus.values()) {
            long count = orderService.countOrdersByStatus(status);
            stats.put(status.name(), count);
        }

        return ResponseEntity.ok(stats);
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