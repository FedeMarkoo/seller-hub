package com.sellerhub.application.dto;

import java.util.List;

public record QuestionGroupPageView(
        List<QuestionGroupView> items,
        int page,
        int pageSize,
        long totalGroups,
        long totalQuestions,
        long pendingQuestions,
        boolean hasNext,
        boolean analyzed,
        QuestionGroupAnalysisView analysis
) {}
