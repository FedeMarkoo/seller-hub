package com.sellerhub.application.dto;

import com.sellerhub.domain.model.Order;
import com.sellerhub.domain.model.OrderItem;
import com.sellerhub.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderView(
        String id,
        String sellerId,
        String buyerName,
        String buyerEmail,
        Instant createdAt,
        OrderStatus status,
        String shippingAddress,
        List<OrderItem> items,
        BigDecimal total,
        long pendingQuestions
) {
    public static OrderView from(Order order, long pendingQuestions) {
        return new OrderView(order.getId(), order.getSellerId(), order.getBuyerName(), order.getBuyerEmail(),
                order.getCreatedAt(), order.getStatus(), order.getShippingAddress(), order.getItems(),
                order.total(), pendingQuestions);
    }
}
