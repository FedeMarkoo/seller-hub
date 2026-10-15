package com.sellerhub.domain.port;

import com.sellerhub.domain.model.BuyerQuestion;

import java.util.List;
import java.util.Optional;
import com.sellerhub.domain.model.QuestionStatus;

public interface QuestionRepository {
    List<BuyerQuestion> findAll();
    Optional<BuyerQuestion> findById(String id);
    default List<BuyerQuestion> findByFilters(String sellerId, String orderId, QuestionStatus status) {
        return findAll().stream()
                .filter(q -> sellerId == null || sellerId.isBlank() || sellerId.equals(q.getSellerId()))
                .filter(q -> orderId == null || orderId.isBlank() || orderId.equals(q.getOrderId()))
                .filter(q -> status == null || status == q.getStatus())
                .toList();
    }
    BuyerQuestion save(BuyerQuestion question);
    void replaceAll(List<BuyerQuestion> questions);
}
