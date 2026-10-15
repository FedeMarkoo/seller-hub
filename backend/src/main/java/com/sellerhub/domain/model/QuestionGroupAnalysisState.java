package com.sellerhub.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class QuestionGroupAnalysisState {
    private String sellerId;
    private QuestionGroupAnalysisStatus status;
    private Instant startedAt;
    private Instant finishedAt;
    private String error;
    private int groups;
}
