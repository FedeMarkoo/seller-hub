package com.sellerhub.application.dto;

import java.util.List;

public record QuestionGroupView(
        String id,
        String representativeText,
        List<QuestionView> questions,
        long questionCount,
        long pendingCount
) {
}
