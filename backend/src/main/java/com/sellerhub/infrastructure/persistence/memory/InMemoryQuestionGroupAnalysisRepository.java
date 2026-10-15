package com.sellerhub.infrastructure.persistence.memory;

import com.sellerhub.domain.model.QuestionGroupAnalysisState;
import com.sellerhub.domain.model.QuestionGroupAnalysisStatus;
import com.sellerhub.domain.port.QuestionGroupAnalysisRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryQuestionGroupAnalysisRepository implements QuestionGroupAnalysisRepository {
    private final ConcurrentHashMap<String, QuestionGroupAnalysisState> states = new ConcurrentHashMap<>();
    @Override public Optional<QuestionGroupAnalysisState> findBySellerId(String sellerId) { return Optional.ofNullable(states.get(sellerId)); }
    @Override public synchronized QuestionGroupAnalysisState start(String sellerId, Instant startedAt) {
        var current = states.get(sellerId);
        if (current != null && current.getStatus() == QuestionGroupAnalysisStatus.RUNNING) return current;
        var next = QuestionGroupAnalysisState.builder().sellerId(sellerId).status(QuestionGroupAnalysisStatus.RUNNING).startedAt(startedAt).groups(0).build();
        states.put(sellerId, next); return next;
    }
    @Override public void complete(String sellerId, Instant finishedAt, int groups) {
        update(sellerId, QuestionGroupAnalysisState.builder().sellerId(sellerId).status(QuestionGroupAnalysisStatus.COMPLETED).finishedAt(finishedAt).groups(groups).build());
    }
    @Override public void fail(String sellerId, Instant finishedAt, String error) {
        var next = QuestionGroupAnalysisState.builder().sellerId(sellerId).status(QuestionGroupAnalysisStatus.FAILED).finishedAt(finishedAt).error(error).build();
        update(sellerId, next);
    }
    private void update(String sellerId, QuestionGroupAnalysisState next) { states.computeIfPresent(sellerId, (ignored, current) -> QuestionGroupAnalysisState.builder().sellerId(sellerId).status(next.getStatus()).startedAt(current.getStartedAt()).finishedAt(next.getFinishedAt()).error(next.getError()).groups(next.getGroups()).build()); }
    @Override public void clear() { states.clear(); }
}
