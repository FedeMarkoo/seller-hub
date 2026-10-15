package com.sellerhub.application.dto;

import com.sellerhub.domain.model.QuestionGroupAnalysisStatus;

import java.time.Instant;

public record QuestionGroupAnalysisView(
        QuestionGroupAnalysisStatus status,
        Instant startedAt,
        Instant finishedAt,
        String error,
        int groups
) {}
