package com.sellerhub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerQuestion {
    private String id;
    private String orderId;
    private String sellerId;
    private String buyerName;
    private String text;
    private Instant createdAt;
    private QuestionStatus status;
    private String answer;
    private Instant answeredAt;

    public Priority priority(Instant now) {
        if (status == QuestionStatus.ANSWERED) return Priority.LOW;
        long ageHours = Duration.between(createdAt, now).toHours();
        if (ageHours >= 48) return Priority.CRITICAL;
        if (ageHours >= 24) return Priority.HIGH;
        if (ageHours >= 8) return Priority.MEDIUM;
        return Priority.LOW;
    }

    public void answerWith(String response, Instant now) {
        if (status == QuestionStatus.ANSWERED) {
            throw new IllegalStateException("La pregunta ya fue respondida");
        }
        answer = response.trim();
        answeredAt = now;
        status = QuestionStatus.ANSWERED;
    }
}
