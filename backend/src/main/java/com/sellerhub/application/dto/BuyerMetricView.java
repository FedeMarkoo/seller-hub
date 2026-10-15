package com.sellerhub.application.dto;

public record BuyerMetricView(
        String buyerName,
        long purchaseCount,
        long questionCount
) {
}
