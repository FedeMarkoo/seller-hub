package com.sellerhub.infrastructure.persistence;

import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.port.QuestionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryQuestionRepository implements QuestionRepository {
    private final ConcurrentHashMap<String, BuyerQuestion> data = new ConcurrentHashMap<>();

    @Override
    public List<BuyerQuestion> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public Optional<BuyerQuestion> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public BuyerQuestion save(BuyerQuestion question) {
        data.put(question.getId(), question);
        return question;
    }

    @Override
    public void replaceAll(List<BuyerQuestion> questions) {
        data.clear();
        questions.forEach(question -> data.put(question.getId(), question));
    }
}
