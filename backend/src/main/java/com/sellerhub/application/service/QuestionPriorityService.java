package com.sellerhub.application.service;

import com.sellerhub.application.dto.PageView;
import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.mapper.SellerHubMapper;
import com.sellerhub.application.util.PaginationUtils;
import com.sellerhub.domain.model.QuestionStatus;
import com.sellerhub.domain.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionPriorityService {
    private final QuestionRepository questionRepository;
    private final SellerHubMapper mapper;

    public List<QuestionView> pending(String sellerId, int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 10000));
        Instant now = Instant.now();
        return questionRepository.findAll().stream()
                .filter(question -> question.getStatus() == QuestionStatus.PENDING)
                .filter(question -> sellerId == null || sellerId.equals(question.getSellerId()))
                .map(question -> mapper.toQuestionView(question, now))
                .sorted(Comparator.comparing(QuestionView::priority).thenComparing(QuestionView::createdAt))
                .limit(limit)
                .toList();
    }

    public PageView<QuestionView> pendingPage(String sellerId, int page, int pageSize) {
        List<QuestionView> all = pending(sellerId, 1000);
        return PaginationUtils.paginate(all, page, pageSize);
    }
}
