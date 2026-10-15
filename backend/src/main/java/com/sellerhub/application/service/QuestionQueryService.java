package com.sellerhub.application.service;

import com.sellerhub.application.dto.PageView;
import com.sellerhub.application.dto.QuestionGroupAnalysisView;
import com.sellerhub.application.dto.QuestionGroupPageView;
import com.sellerhub.application.dto.QuestionGroupView;
import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.mapper.SellerHubMapper;
import com.sellerhub.application.util.PaginationUtils;
import com.sellerhub.domain.model.QuestionGroupIndex;
import com.sellerhub.domain.model.QuestionStatus;
import com.sellerhub.domain.port.QuestionGroupRepository;
import com.sellerhub.domain.port.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionQueryService {
    private final QuestionRepository questionRepository;
    private final QuestionGroupRepository questionGroupRepository;
    private final SellerHubMapper mapper;

    public List<QuestionView> find(String sellerId, String orderId, QuestionStatus status, int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 10000));
        Instant now = Instant.now();
        return questionRepository.findByFilters(sellerId, orderId, status).stream()
                .map(question -> mapper.toQuestionView(question, now))
                .sorted(Comparator.comparing(QuestionView::createdAt).reversed())
                .limit(limit)
                .toList();
    }

    public PageView<QuestionView> findPage(String sellerId, String orderId, QuestionStatus status, int page, int pageSize) {
        List<QuestionView> all = find(sellerId, orderId, status, 10000);
        return PaginationUtils.paginate(all, page, pageSize);
    }

    public List<QuestionGroupView> findGroups(String sellerId, QuestionStatus status, int requestedLimit) {
        List<QuestionView> questions = find(sellerId, null, status, requestedLimit);
        return questionGroupRepository.findBySellerId(sellerId).stream()
                .map(index -> toGroupView(index, byId(questions)))
                .filter(group -> group.questionCount() > 0)
                .toList();
    }

    public QuestionGroupPageView findGroupsPage(String sellerId, QuestionStatus status, String search, int page, int pageSize, boolean analyzed, QuestionGroupAnalysisView analysis) {
        List<QuestionGroupView> all = findGroups(sellerId, status, 10000).stream()
                .filter(group -> matchesSearch(group, search))
                .toList();
        List<QuestionView> questions = find(sellerId, null, status, 10000).stream()
                .filter(question -> matchesSearch(question, search))
                .toList();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int from = Math.min(safePage * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        long pending = questions.stream()
                .filter(question -> question.status() == QuestionStatus.PENDING)
                .count();
        List<QuestionGroupView> summaries = all.subList(from, to).stream()
                .map(group -> new QuestionGroupView(group.id(), group.representativeText(), List.of(), group.questionCount(), group.pendingCount()))
                .toList();
        return new QuestionGroupPageView(summaries, safePage, safeSize, all.size(), questions.size(), pending, to < all.size(), analyzed, analysis);
    }

    public QuestionGroupView findGroup(String sellerId, QuestionStatus status, String search, String groupId) {
        QuestionGroupIndex index = questionGroupRepository.findBySellerId(sellerId).stream()
                .filter(group -> group.getId().equals(groupId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Grupo de preguntas no encontrado"));
        QuestionGroupView result = toGroupView(index, byId(find(sellerId, null, status, 10000)));
        if (!matchesSearch(result, search)) throw new NoSuchElementException("Grupo de preguntas no encontrado");
        return result;
    }

    private Map<String, QuestionView> byId(List<QuestionView> questions) {
        return questions.stream()
                .collect(Collectors.toMap(QuestionView::id, question -> question));
    }

    private QuestionGroupView toGroupView(QuestionGroupIndex index, Map<String, QuestionView> byId) {
        List<QuestionView> group = index.getQuestionIds().stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
        long pending = group.stream()
                .filter(question -> question.status() == QuestionStatus.PENDING)
                .count();
        return new QuestionGroupView(index.getId(), index.getRepresentativeText(), group, group.size(), pending);
    }

    private boolean matchesSearch(QuestionGroupView group, String search) {
        if (search == null || search.isBlank()) return true;
        String normalized = search.toLowerCase(Locale.ROOT).trim();
        return group.representativeText().toLowerCase(Locale.ROOT).contains(normalized)
                || group.questions().stream()
                        .anyMatch(question -> matchesSearch(question, search));
    }

    private boolean matchesSearch(QuestionView question, String search) {
        if (search == null || search.isBlank()) return true;
        String normalized = search.toLowerCase(Locale.ROOT).trim();
        return String.join(" ", question.id(), question.orderId(), question.buyerName(), question.text())
                .toLowerCase(Locale.ROOT).contains(normalized);
    }
}
