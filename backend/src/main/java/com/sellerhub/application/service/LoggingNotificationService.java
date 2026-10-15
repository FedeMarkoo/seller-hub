package com.sellerhub.application.service;

import com.sellerhub.domain.model.BuyerQuestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LoggingNotificationService implements NotificationService {
    @Override
    public void notifyPendingQuestion(BuyerQuestion question) {
        log.info("Pending buyer question notification sellerId={} orderId={} questionId={}",
                question.getSellerId(), question.getOrderId(), question.getId());
    }
}
