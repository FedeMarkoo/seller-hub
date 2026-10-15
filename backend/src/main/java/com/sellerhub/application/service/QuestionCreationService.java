package com.sellerhub.application.service;

import com.sellerhub.application.dto.CreateQuestionRequest;
import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.mapper.SellerHubMapper;
import com.sellerhub.domain.model.BuyerQuestion;
import com.sellerhub.domain.model.QuestionStatus;
import com.sellerhub.domain.port.OrderRepository;
import com.sellerhub.domain.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionCreationService {
    private final OrderRepository orderRepository;
    private final QuestionRepository questionRepository;
    private final SellerHubMapper mapper;
    private final NotificationService notificationService;

    public QuestionView create(String orderId, CreateQuestionRequest request, String sellerId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado: " + orderId));
        if (sellerId != null && !sellerId.equals(order.getSellerId())) {
            throw new com.sellerhub.infrastructure.security.ForbiddenException();
        }
        var now = Instant.now();
        var question = BuyerQuestion.builder()
                .id(UUID.randomUUID().toString())
                .orderId(orderId)
                .sellerId(order.getSellerId())
                .buyerName(request.buyerName().trim())
                .text(request.text().trim())
                .createdAt(now)
                .status(QuestionStatus.PENDING)
                .build();
        var saved = questionRepository.save(question);
        notificationService.notifyPendingQuestion(saved);
        return mapper.toQuestionView(saved, now);
    }
}
