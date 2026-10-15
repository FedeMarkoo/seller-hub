package com.sellerhub.application.mapper;

import com.sellerhub.application.dto.MetricsView;
import com.sellerhub.application.dto.ProductMetricView;
import com.sellerhub.application.dto.BuyerMetricView;
import com.sellerhub.application.dto.CommonQuestionView;
import com.sellerhub.application.dto.OrderView;
import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.dto.DemoSessionView;
import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface SellerHubMapper {
    @Mapping(target = "total", expression = "java(order.total())")
    @Mapping(target = "pendingQuestions", source = "pendingQuestions")
    OrderView toOrderView(Order order, long pendingQuestions);

    @Mapping(target = "priority", expression = "java(question.priority(now))")
    @Mapping(target = "waitingHours", expression = "java(waitingHours(question, now))")
    QuestionView toQuestionView(BuyerQuestion question, Instant now);

    DemoSessionView toDemoSessionView(String token, com.sellerhub.infrastructure.security.DemoRole role, String sellerId);

    MetricsView toMetricsView(long totalOrders, java.math.BigDecimal grossSales, long totalQuestions,
                              long answeredQuestions, long pendingQuestions, long criticalQuestions,
                              long ordersWithPendingQuestions, long processingOrders, long shippedOrders,
                              double averageResponseHours, double answerRate,
                              Map<String, Long> questionsByPriority, Map<String, Long> ordersByStatus,
                              java.util.List<ProductMetricView> topProducts,
                              java.util.List<BuyerMetricView> topBuyers,
                              CommonQuestionView mostCommonQuestion,
                              com.sellerhub.application.dto.MetricsTrendView trend);

    ProductMetricView toProductMetricView(String product, long purchaseCount, long questionCount, long unitsSold);

    BuyerMetricView toBuyerMetricView(String buyerName, long purchaseCount, long questionCount);

    CommonQuestionView toCommonQuestionView(String text, long questionCount);

    default long waitingHours(BuyerQuestion question, Instant now) {
        Instant end = question.getAnsweredAt() == null ? now : question.getAnsweredAt();
        return Math.max(0, Duration.between(question.getCreatedAt(), end).toHours());
    }
}
