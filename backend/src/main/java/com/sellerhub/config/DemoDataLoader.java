package com.sellerhub.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.model.Order;
import com.sellerhub.domain.port.OrderRepository;
import com.sellerhub.domain.port.QuestionRepository;
import com.sellerhub.domain.port.QuestionGroupRepository;
import com.sellerhub.domain.port.QuestionGroupAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {
    private final OrderRepository orderRepository;
    private final QuestionRepository questionRepository;
    private final QuestionGroupRepository questionGroupRepository;
    private final QuestionGroupAnalysisRepository questionGroupAnalysisRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws IOException {
        if (orderRepository.findAll().isEmpty()) reload();
    }

    public synchronized void reload() throws IOException {
        var resource = new ClassPathResource("demo-data.json");
        Map<String, List<Map<String, Object>>> root = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        List<Order> orders = root.get("orders").stream()
                .map(raw -> objectMapper.convertValue(raw, Order.class))
                .toList();
        Instant now = Instant.now();
        List<BuyerQuestion> questions = root.get("questions").stream()
                .map(raw -> {
                    var mutable = new java.util.LinkedHashMap<>(raw);
                    Number ageHours = (Number) mutable.remove("ageHours");
                    Number responseHours = (Number) mutable.remove("responseHours");
                    var question = objectMapper.convertValue(mutable, BuyerQuestion.class);
                    question.setCreatedAt(now.minusSeconds(ageHours.longValue() * 3600));
                    if (responseHours != null) question.setAnsweredAt(question.getCreatedAt().plusSeconds(responseHours.longValue() * 3600));
                    return question;
                })
                .toList();
        orderRepository.replaceAll(orders);
        questionRepository.replaceAll(questions);
        questionGroupRepository.clear();
        questionGroupAnalysisRepository.clear();
    }
}
