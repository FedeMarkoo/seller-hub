package com.sellerhub.application.dto;

import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.model.Priority;
import com.sellerhub.domain.model.QuestionStatus;

import java.time.Instant;

public record QuestionView(
        String id,
        String orderId,
        String sellerId,
        String buyerName,
        String text,
        Instant createdAt,
        QuestionStatus status,
        String answer,
        Instant answeredAt,
        Priority priority,
        long waitingHours
) {
}
