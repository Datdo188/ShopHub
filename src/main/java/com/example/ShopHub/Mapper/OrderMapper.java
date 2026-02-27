package com.example.ShopHub.Mapper;

import com.example.ShopHub.DTO.OrderCreateRequest;
import com.example.ShopHub.DTO.OrderDTO;
import com.example.ShopHub.DTO.OrderItemDTO;
import com.example.ShopHub.Entity.Order;
import com.example.ShopHub.Entity.OrderItem;
import com.example.ShopHub.Entity.Product;
import com.example.ShopHub.Entity.User;
import com.example.ShopHub.Enum.OrderStatus;
import com.example.ShopHub.Repository.ProductRepository;
import com.example.ShopHub.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public OrderDTO toDTO(Order order) {
        if (order == null) return null;

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setShippingAddressJson(order.getShippingAddressJson());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setNote(order.getNote());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUserEmail(order.getUser().getEmail());
            dto.setUserFullName(order.getUser().getFullName());
        }

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public OrderItemDTO toItemDTO(OrderItem item) {
        if (item == null) return null;

        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductNameSnapshot(item.getProductNameSnapshot());
        dto.setVariantSnapshot(item.getVariantSnapshot());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getSubtotal());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
        }

        return dto;
    }

    public Order toEntity(OrderCreateRequest request) {
        if (request == null) return null;

        Order order = new Order();

        // Set user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        order.setUser(user);

        // Set basic info
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddressJson(request.getShippingAddressJson());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus("pending");
        order.setNote(request.getNote());
        order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);

        // Create order items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderCreateRequest.OrderItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(itemReq.getQuantity());
                orderItem.setVariantSnapshot(itemReq.getVariantSnapshot());

                order.getItems().add(orderItem);
            }
        }

        return order;
    }
}