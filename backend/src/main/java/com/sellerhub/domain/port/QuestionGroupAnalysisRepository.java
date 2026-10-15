package com.sellerhub.domain.port;

import com.sellerhub.domain.model.QuestionGroupAnalysisState;

import java.time.Instant;
import java.util.Optional;

public interface QuestionGroupAnalysisRepository {
    Optional<QuestionGroupAnalysisState> findBySellerId(String sellerId);

    QuestionGroupAnalysisState start(String sellerId, Instant startedAt);

    void complete(String sellerId, Instant finishedAt, int groups);

    void fail(String sellerId, Instant finishedAt, String error);

    void clear();
}
