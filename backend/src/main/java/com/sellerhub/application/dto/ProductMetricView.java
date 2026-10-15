package com.sellerhub.application.dto;

public record ProductMetricView(
        String product,
        long purchaseCount,
        long questionCount,
        long unitsSold
) {
}
