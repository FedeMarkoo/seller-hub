package com.sellerhub;

import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.model.Priority;
import com.sellerhub.domain.model.QuestionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuyerQuestionTest {
    private static final Instant NOW = Instant.parse("2026-10-09T12:00:00Z");

    @Test
    void prioritizesPendingQuestionsByAge() {
        var question = BuyerQuestion.builder().createdAt(NOW.minusSeconds(49 * 3600)).status(QuestionStatus.PENDING).build();
        assertThat(question.priority(NOW)).isEqualTo(Priority.CRITICAL);
    }

    @Test
    void answersQuestionOnlyOnce() {
        var question = BuyerQuestion.builder().createdAt(NOW).status(QuestionStatus.PENDING).build();
        question.answerWith("  Respuesta útil  ", NOW);

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
        assertThat(question.getAnswer()).isEqualTo("Respuesta útil");
        assertThatThrownBy(() -> question.answerWith("Otra", NOW)).isInstanceOf(IllegalStateException.class);
    }
}
