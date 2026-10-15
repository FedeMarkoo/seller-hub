package com.sellerhub.application.dto;

import java.math.BigDecimal;

public record MetricsTrendView(
        int periodDays,
        long currentOrders,
        long previousOrders,
        BigDecimal currentRevenue,
        BigDecimal previousRevenue,
        double ordersChangePercent,
        double revenueChangePercent
) {}
