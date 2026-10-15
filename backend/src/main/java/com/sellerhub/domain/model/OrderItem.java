package com.sellerhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    /** Stable catalog identity; product is the display snapshot captured at purchase time. */
    private String productId;
    private String product;
    private int quantity;
    private BigDecimal unitPrice;
}
