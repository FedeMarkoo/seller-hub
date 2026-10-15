package com.sellerhub.application.service;

import com.sellerhub.application.dto.PageView;
import com.sellerhub.application.dto.QuestionGroupAnalysisView;
import com.sellerhub.application.dto.QuestionGroupPageView;
import com.sellerhub.application.dto.QuestionGroupView;
import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.dto.CreateQuestionRequest;
import com.sellerhub.domain.model.QuestionGroupAnalysisState;
import com.sellerhub.domain.model.QuestionGroupAnalysisStatus;
import com.sellerhub.domain.model.QuestionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Application facade kept stable for controllers and use cases. */
@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionQueryService queryService;
    private final QuestionGroupAnalysisService groupAnalysisService;
    private final QuestionAnswerService answerService;
    private final QuestionPriorityService priorityService;
    private final QuestionCreationService creationService;

    public List<QuestionView> find(String sellerId, String orderId, QuestionStatus status, int requestedLimit) {
        return queryService.find(sellerId, orderId, status, requestedLimit);
    }

    public PageView<QuestionView> findPage(String sellerId, String orderId, QuestionStatus status, int page, int pageSize) {
        return queryService.findPage(sellerId, orderId, status, page, pageSize);
    }

    public QuestionGroupPageView findGroupsPage(String sellerId, QuestionStatus status, String search, int page, int pageSize) {
        QuestionGroupAnalysisView analysis = groupAnalysisService.status(sellerId);
        boolean analyzed = analysis.status() == QuestionGroupAnalysisStatus.COMPLETED && groupAnalysisService.hasGroups(sellerId);
        return queryService.findGroupsPage(sellerId, status, search, page, pageSize, analyzed, analysis);
    }

    public QuestionGroupView findGroup(String sellerId, QuestionStatus status, String search, String groupId) {
        return queryService.findGroup(sellerId, status, search, groupId);
    }

    public List<QuestionGroupView> findGroups(String sellerId, QuestionStatus status, int requestedLimit) {
        return queryService.findGroups(sellerId, status, requestedLimit);
    }

    public QuestionGroupAnalysisState requestGroupAnalysis(String sellerId) {
        return groupAnalysisService.request(sellerId);
    }

    public QuestionGroupAnalysisView groupAnalysisStatus(String sellerId) {
        return groupAnalysisService.status(sellerId);
    }

    public List<QuestionView> pendingPrioritized(String sellerId, int requestedLimit) {
        return priorityService.pending(sellerId, requestedLimit);
    }

    public PageView<QuestionView> pendingPrioritizedPage(String sellerId, int page, int pageSize) {
        return priorityService.pendingPage(sellerId, page, pageSize);
    }

    public QuestionView answer(String id, String answer, String sellerId) {
        return answerService.answer(id, answer, sellerId);
    }

    public QuestionView create(String orderId, CreateQuestionRequest request, String sellerId) {
        return creationService.create(orderId, request, sellerId);
    }
}
