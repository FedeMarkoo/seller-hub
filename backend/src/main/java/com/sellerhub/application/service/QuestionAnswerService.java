package com.sellerhub.application.service;

import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.mapper.SellerHubMapper;
import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class QuestionAnswerService {
    private final QuestionRepository questionRepository;
    private final SellerHubMapper mapper;

    public synchronized QuestionView answer(String id, String answer, String sellerId) {
        BuyerQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pregunta no encontrada: " + id));
        if (sellerId != null && !sellerId.equals(question.getSellerId())) {
            throw new com.sellerhub.infrastructure.security.ForbiddenException();
        }
        Instant now = Instant.now();
        question.answerWith(answer, now);
        return mapper.toQuestionView(questionRepository.save(question), now);
    }
}
