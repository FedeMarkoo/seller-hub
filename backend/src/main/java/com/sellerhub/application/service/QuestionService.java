package com.sellerhub.application.service;

import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.model.QuestionStatus;
import com.sellerhub.domain.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final Clock clock;

    public List<QuestionView> find(String sellerId, QuestionStatus status) {
        Instant now = clock.instant();
        return questionRepository.findAll().stream()
                .filter(q -> sellerId == null || sellerId.isBlank() || q.getSellerId().equals(sellerId))
                .filter(q -> status == null || q.getStatus() == status)
                .map(q -> toView(q, now))
                .sorted(Comparator.comparing(QuestionView::createdAt).reversed())
                .toList();
    }

    public List<QuestionView> pendingPrioritized() {
        Instant now = clock.instant();
        return questionRepository.findAll().stream()
                .filter(q -> q.getStatus() == QuestionStatus.PENDING)
                .map(q -> toView(q, now))
                .sorted(Comparator.comparing(QuestionView::priority)
                        .thenComparing(QuestionView::createdAt))
                .toList();
    }

    public QuestionView answer(String id, String answer) {
        BuyerQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pregunta no encontrada: " + id));
        question.answerWith(answer, clock.instant());
        return toView(questionRepository.save(question), clock.instant());
    }

    private QuestionView toView(BuyerQuestion q, Instant now) {
        return new QuestionView(q.getId(), q.getOrderId(), q.getSellerId(), q.getBuyerName(), q.getText(),
                q.getCreatedAt(), q.getStatus(), q.getAnswer(), q.getAnsweredAt(), q.priority(now),
                Math.max(0, Duration.between(q.getCreatedAt(), q.getAnsweredAt() == null ? now : q.getAnsweredAt()).toHours()));
    }
}
