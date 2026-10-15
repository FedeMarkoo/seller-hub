package com.sellerhub.application.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

public record MetricsView(
        long totalOrders,
        BigDecimal grossSales,
        long totalQuestions,
        long answeredQuestions,
        long pendingQuestions,
        long criticalQuestions,
        long ordersWithPendingQuestions,
        long processingOrders,
        long shippedOrders,
        double averageResponseHours,
        double answerRate,
        Map<String, Long> questionsByPriority,
        Map<String, Long> ordersByStatus,
        List<ProductMetricView> topProducts,
        List<BuyerMetricView> topBuyers,
        CommonQuestionView mostCommonQuestion,
        MetricsTrendView trend
) {
}
