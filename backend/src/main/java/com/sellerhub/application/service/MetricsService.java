package com.sellerhub.application.service;

import com.sellerhub.application.dto.MetricsView;
import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.model.Priority;
import com.sellerhub.domain.model.QuestionStatus;
import com.sellerhub.domain.port.OrderRepository;
import com.sellerhub.domain.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final OrderRepository orderRepository;
    private final QuestionRepository questionRepository;
    private final Clock clock;

    public MetricsView calculate() {
        var orders = orderRepository.findAll();
        var questions = questionRepository.findAll();
        long pending = questions.stream().filter(q -> q.getStatus() == QuestionStatus.PENDING).count();
        long answered = questions.size() - pending;
        double avgResponse = questions.stream().filter(q -> q.getAnsweredAt() != null)
                .mapToLong(q -> Duration.between(q.getCreatedAt(), q.getAnsweredAt()).toMinutes()).average()
                .orElse(0) / 60.0;
        var byPriority = Arrays.stream(Priority.values()).collect(Collectors.toMap(Enum::name,
                p -> questions.stream().filter(q -> q.getStatus() == QuestionStatus.PENDING)
                        .filter(q -> q.priority(clock.instant()) == p).count(), (a, b) -> a, LinkedHashMap::new));
        var byStatus = orders.stream().collect(Collectors.groupingBy(o -> o.getStatus().name(), LinkedHashMap::new,
                Collectors.counting()));
        BigDecimal grossSales = orders.stream().map(o -> o.total()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MetricsView(orders.size(), grossSales, pending, byPriority.get(Priority.CRITICAL.name()),
                round(avgResponse), questions.isEmpty() ? 0 : round(answered * 100.0 / questions.size()),
                byPriority, byStatus);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
