package com.sellerhub.infrastructure.persistence.memory;

import com.sellerhub.domain.model.QuestionGroupIndex;
import com.sellerhub.domain.port.QuestionGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryQuestionGroupRepository implements QuestionGroupRepository {
    private final ConcurrentHashMap<String, List<QuestionGroupIndex>> groups = new ConcurrentHashMap<>();
    @Override public List<QuestionGroupIndex> findBySellerId(String sellerId) {
        return new ArrayList<>(groups.getOrDefault(sellerId, List.of()));
    }
    @Override public void replaceForSeller(String sellerId, List<QuestionGroupIndex> values) {
        groups.put(sellerId, new ArrayList<>(values));
    }
    @Override public void clear() { groups.clear(); }
}
