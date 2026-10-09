package com.sellerhub.application.dto;

import java.math.BigDecimal;
import java.util.Map;

public record MetricsView(
        long totalOrders,
        BigDecimal grossSales,
        long pendingQuestions,
        long criticalQuestions,
        double averageResponseHours,
        double answerRate,
        Map<String, Long> questionsByPriority,
        Map<String, Long> ordersByStatus
) {
}
