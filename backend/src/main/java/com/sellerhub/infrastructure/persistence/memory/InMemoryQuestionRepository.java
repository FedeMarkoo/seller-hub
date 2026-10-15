package com.sellerhub.infrastructure.persistence.memory;

import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.port.QuestionRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.sellerhub.domain.model.QuestionStatus;

@Repository
public class InMemoryQuestionRepository implements QuestionRepository {
    private final ConcurrentHashMap<String, BuyerQuestion> questions = new ConcurrentHashMap<>();
    @Override public List<BuyerQuestion> findAll() { return new ArrayList<>(questions.values()); }
    @Override public Optional<BuyerQuestion> findById(String id) { return Optional.ofNullable(questions.get(id)); }
    @Override public List<BuyerQuestion> findByFilters(String sellerId, String orderId, QuestionStatus status) {
        return questions.values().stream()
                .filter(q -> sellerId == null || sellerId.isBlank() || sellerId.equals(q.getSellerId()))
                .filter(q -> orderId == null || orderId.isBlank() || orderId.equals(q.getOrderId()))
                .filter(q -> status == null || status == q.getStatus()).toList();
    }
    @Override public BuyerQuestion save(BuyerQuestion question) { questions.put(question.getId(), question); return question; }
    @Override public synchronized void replaceAll(List<BuyerQuestion> values) {
        questions.clear();
        values.forEach(question -> questions.put(question.getId(), question));
    }
}
