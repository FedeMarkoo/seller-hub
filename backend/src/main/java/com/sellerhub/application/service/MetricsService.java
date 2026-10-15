package com.sellerhub.application.service;

import com.sellerhub.application.dto.MetricsView;
import com.sellerhub.application.dto.ProductMetricView;
import com.sellerhub.application.dto.BuyerMetricView;
import com.sellerhub.application.dto.CommonQuestionView;
import com.sellerhub.application.mapper.SellerHubMapper;
import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.model.Priority;
import com.sellerhub.domain.model.QuestionStatus;
import com.sellerhub.domain.port.OrderRepository;
import com.sellerhub.domain.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final OrderRepository orderRepository;
    private final QuestionRepository questionRepository;
    private final SellerHubMapper mapper;
    private final QuestionService questionService;

    public MetricsView calculate(String sellerId) {
        var orders = orderRepository.findAll().stream()
                .filter(order -> sellerId == null || sellerId.equals(order.getSellerId()))
                .toList();
        var questions = questionRepository.findAll().stream()
                .filter(question -> sellerId == null || sellerId.equals(question.getSellerId()))
                .toList();
        long pending = questions.stream()
                .filter(q -> q.getStatus() == QuestionStatus.PENDING)
                .count();
        long answered = questions.size() - pending;
        long ordersWithPending = questions.stream()
                .filter(q -> q.getStatus() == QuestionStatus.PENDING)
                .map(BuyerQuestion::getOrderId)
                .distinct()
                .count();
        long processing = orders.stream()
                .filter(order -> order.getStatus().name().equals("PROCESSING"))
                .count();
        long shipped = orders.stream()
                .filter(order -> order.getStatus().name().equals("SHIPPED"))
                .count();
        double avgResponse = questions.stream()
                .filter(q -> q.getAnsweredAt() != null)
                .mapToLong(q -> Duration.between(q.getCreatedAt(), q.getAnsweredAt()).toMinutes())
                .average()
                .orElse(0) / 60.0;
        Instant now = Instant.now();
        var byPriority = Arrays.stream(Priority.values())
                .collect(Collectors.toMap(Enum::name,
                        p -> questions.stream()
                                .filter(q -> q.getStatus() == QuestionStatus.PENDING)
                                .filter(q -> q.priority(now) == p)
                                .count(),
                        (a, b) -> a,
                        LinkedHashMap::new));
        var byStatus = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), LinkedHashMap::new, Collectors.counting()));
        BigDecimal grossSales = orders.stream()
                .map(o -> o.total())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var questionsByOrder = questions.stream()
                .collect(Collectors.groupingBy(BuyerQuestion::getOrderId));
        var topProducts = topProducts(orders, questionsByOrder);
        var topBuyers = topBuyers(orders, questionsByOrder);
        var mostCommonQuestion = questionService.findGroups(sellerId, null, 10000).stream()
                .max(java.util.Comparator.comparingLong(group -> group.questionCount()))
                .map(group -> mapper.toCommonQuestionView(group.representativeText(), group.questionCount()))
                .orElse(null);
        var trend = trend(orders);
        return mapper.toMetricsView(orders.size(), grossSales, questions.size(), answered, pending,
                byPriority.get(Priority.CRITICAL.name()), ordersWithPending, processing, shipped,
                round(avgResponse), questions.isEmpty() ? 0 : round(answered * 100.0 / questions.size()),
                byPriority, byStatus, topProducts, topBuyers, mostCommonQuestion, trend);
    }

    private com.sellerhub.application.dto.MetricsTrendView trend(List<com.sellerhub.domain.model.Order> orders) {
        Instant currentStart = Instant.now().minus(7, ChronoUnit.DAYS);
        Instant previousStart = currentStart.minus(7, ChronoUnit.DAYS);
        var current = orders.stream().filter(o -> !o.getCreatedAt().isBefore(currentStart)).toList();
        var previous = orders.stream().filter(o -> !o.getCreatedAt().isBefore(previousStart) && o.getCreatedAt().isBefore(currentStart)).toList();
        BigDecimal currentRevenue = current.stream().map(com.sellerhub.domain.model.Order::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal previousRevenue = previous.stream().map(com.sellerhub.domain.model.Order::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new com.sellerhub.application.dto.MetricsTrendView(7, current.size(), previous.size(), currentRevenue, previousRevenue,
                change(current.size(), previous.size()), change(currentRevenue.doubleValue(), previousRevenue.doubleValue()));
    }

    private double change(double current, double previous) {
        if (previous == 0) return current == 0 ? 0 : 100;
        return round((current - previous) * 100 / previous);
    }

    private List<ProductMetricView> topProducts(List<com.sellerhub.domain.model.Order> orders,
                                                 Map<String, List<BuyerQuestion>> questionsByOrder) {
        Map<String, ProductAccumulator> totals = new LinkedHashMap<>();
        orders.forEach(order -> order.getItems().forEach(item -> {
            var accumulator = totals.computeIfAbsent(item.getProduct(), ignored -> new ProductAccumulator());
            accumulator.purchaseCount++;
            accumulator.unitsSold += item.getQuantity();
            accumulator.questionCount += questionsByOrder.getOrDefault(order.getId(), List.of()).size();
        }));
        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, ProductAccumulator>comparingByValue(java.util.Comparator
                        .comparingLong((ProductAccumulator value) -> value.purchaseCount).reversed()
                        .thenComparing(java.util.Comparator.comparingLong((ProductAccumulator value) -> value.questionCount).reversed())))
                .limit(5)
                .map(entry -> mapper.toProductMetricView(entry.getKey(), entry.getValue().purchaseCount,
                        entry.getValue().questionCount, entry.getValue().unitsSold))
                .toList();
    }

    private List<BuyerMetricView> topBuyers(List<com.sellerhub.domain.model.Order> orders,
                                             Map<String, List<BuyerQuestion>> questionsByOrder) {
        Map<String, BuyerAccumulator> totals = new LinkedHashMap<>();
        orders.forEach(order -> {
            var accumulator = totals.computeIfAbsent(order.getBuyerName(), ignored -> new BuyerAccumulator());
            accumulator.purchaseCount++;
            accumulator.questionCount += questionsByOrder.getOrDefault(order.getId(), List.of()).size();
        });
        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, BuyerAccumulator>comparingByValue(java.util.Comparator
                        .comparingLong((BuyerAccumulator value) -> value.purchaseCount).reversed()
                        .thenComparing(java.util.Comparator.comparingLong((BuyerAccumulator value) -> value.questionCount).reversed())))
                .limit(5)
                .map(entry -> mapper.toBuyerMetricView(entry.getKey(), entry.getValue().purchaseCount,
                        entry.getValue().questionCount))
                .toList();
    }

    private static final class ProductAccumulator {
        private long purchaseCount;
        private long questionCount;
        private long unitsSold;
    }

    private static final class BuyerAccumulator {
        private long purchaseCount;
        private long questionCount;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
