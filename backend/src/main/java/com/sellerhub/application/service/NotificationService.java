package com.sellerhub.application.service;

import com.sellerhub.domain.model.BuyerQuestion;

public interface NotificationService {
    void notifyPendingQuestion(BuyerQuestion question);
}
