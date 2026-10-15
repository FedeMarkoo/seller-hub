package com.sellerhub.domain.port;

import com.sellerhub.domain.model.QuestionGroupIndex;

import java.util.List;

public interface QuestionGroupRepository {
    List<QuestionGroupIndex> findBySellerId(String sellerId);

    void replaceForSeller(String sellerId, List<QuestionGroupIndex> groups);

    void clear();
}
