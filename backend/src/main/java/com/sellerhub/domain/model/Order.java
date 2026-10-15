package com.sellerhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;
    private String sellerId;
    private String buyerName;
    private String buyerEmail;
    private Instant createdAt;
    private OrderStatus status;
    private String shippingAddress;
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public BigDecimal total() {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
