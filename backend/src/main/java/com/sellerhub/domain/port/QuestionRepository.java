package com.sellerhub.domain.port;

import com.sellerhub.domain.model.BuyerQuestion;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository {
    List<BuyerQuestion> findAll();
    Optional<BuyerQuestion> findById(String id);
    BuyerQuestion save(BuyerQuestion question);
    void replaceAll(List<BuyerQuestion> questions);
}
