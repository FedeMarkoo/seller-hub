package com.sellerhub.application.dto;

import com.sellerhub.domain.model.OrderItem;
import com.sellerhub.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderView(
        String id,
        String sellerId,
        String buyerName,
        Instant createdAt,
        OrderStatus status,
        List<OrderItem> items,
        BigDecimal total,
        long pendingQuestions
){
}
