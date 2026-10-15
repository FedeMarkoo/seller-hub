package com.sellerhub.application.service;

import com.sellerhub.application.dto.QuestionGroupAnalysisView;
import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.domain.model.QuestionGroupAnalysisState;
import com.sellerhub.domain.model.QuestionGroupAnalysisStatus;
import com.sellerhub.domain.model.QuestionGroupIndex;
import com.sellerhub.domain.port.QuestionGroupAnalysisRepository;
import com.sellerhub.domain.port.QuestionGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionGroupAnalysisService {
    private final QuestionQueryService questionQueryService;
    private final QuestionSimilarityService similarityService;
    private final QuestionGroupRepository questionGroupRepository;
    private final QuestionGroupAnalysisRepository analysisRepository;
    private final TaskExecutor analysisExecutor;

    public QuestionGroupAnalysisState request(String sellerId) {
        var current = analysisRepository.findBySellerId(sellerId);
        if (current.isPresent() && current.get().getStatus() == QuestionGroupAnalysisStatus.RUNNING) return current.get();
        var started = analysisRepository.start(sellerId, Instant.now());
        if (started.getStatus() == QuestionGroupAnalysisStatus.RUNNING) {
            analysisExecutor.execute(() -> run(sellerId));
        }
        return started;
    }

    public QuestionGroupAnalysisView status(String sellerId) {
        return analysisRepository.findBySellerId(sellerId)
                .map(this::toView)
                .orElse(new QuestionGroupAnalysisView(QuestionGroupAnalysisStatus.IDLE, null, null, null, 0));
    }

    public boolean hasGroups(String sellerId) {
        return !questionGroupRepository.findBySellerId(sellerId).isEmpty();
    }

    public int analyze(String sellerId) {
        List<QuestionView> questions = questionQueryService.find(sellerId, null, null, 10000);
        List<List<QuestionView>> groups = new ArrayList<>();
        questions.forEach(question -> groups.stream()
                .filter(group -> group.stream().anyMatch(candidate -> similarityService.similar(candidate.text(), question.text())))
                .findFirst()
                .ifPresentOrElse(group -> group.add(question), () -> groups.add(new ArrayList<>(List.of(question)))));
        List<QuestionGroupIndex> indexes = groups.stream()
                .map(group -> QuestionGroupIndex.builder()
                        .id(group.getFirst().id())
                        .sellerId(sellerId)
                        .representativeText(group.getFirst().text())
                        .questionIds(group.stream()
                                .map(QuestionView::id)
                                .toList())
                        .build())
                .toList();
        questionGroupRepository.replaceForSeller(sellerId, indexes);
        return indexes.size();
    }

    private void run(String sellerId) {
        try {
            int groups = analyze(sellerId);
            analysisRepository.complete(sellerId, Instant.now(), groups);
        } catch (RuntimeException exception) {
            analysisRepository.fail(sellerId, Instant.now(), exception.getMessage());
        }
    }

    private QuestionGroupAnalysisView toView(QuestionGroupAnalysisState state) {
        return new QuestionGroupAnalysisView(state.getStatus(), state.getStartedAt(), state.getFinishedAt(), state.getError(), state.getGroups());
    }
}
