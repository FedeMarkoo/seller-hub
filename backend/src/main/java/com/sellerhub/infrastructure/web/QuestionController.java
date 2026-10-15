package com.sellerhub.infrastructure.web;

import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.dto.QuestionGroupView;
import com.sellerhub.application.dto.PageView;
import com.sellerhub.application.dto.QuestionGroupPageView;
import com.sellerhub.application.dto.QuestionGroupAnalysisView;
import com.sellerhub.application.service.QuestionService;
import com.sellerhub.infrastructure.security.DemoRole;
import com.sellerhub.infrastructure.security.DemoSessionService;
import com.sellerhub.domain.model.QuestionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final DemoSessionService sessions;

    @GetMapping
    public PageView<QuestionView> find(@RequestParam(required = false) String sellerId,
                                   @RequestParam(required = false) String orderId,
                                   @RequestParam(required = false) QuestionStatus status,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "100") int pageSize,
                                   @RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        if (identity.role() == DemoRole.SELLER && sellerId != null && !identity.canAccessSeller(sellerId)) {
            throw new com.sellerhub.infrastructure.security.ForbiddenException();
        }
        String effectiveSeller = identity.sellerId() != null ? identity.sellerId() : sellerId;
        if (effectiveSeller != null) sessions.requireSeller(token, effectiveSeller);
        return questionService.findPage(effectiveSeller, orderId, status, page, pageSize);
    }

    @GetMapping("/groups")
    public QuestionGroupPageView findGroups(@RequestParam(required = false) String sellerId,
                                              @RequestParam(required = false) QuestionStatus status,
                                              @RequestParam(required = false) String search,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "5") int pageSize,
                                              @RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        if (identity.role() == DemoRole.SELLER && sellerId != null && !identity.canAccessSeller(sellerId)) {
            throw new com.sellerhub.infrastructure.security.ForbiddenException();
        }
        String effectiveSeller = identity.sellerId() != null ? identity.sellerId() : sellerId;
        if (effectiveSeller != null) sessions.requireSeller(token, effectiveSeller);
        return questionService.findGroupsPage(effectiveSeller, status, search, page, pageSize);
    }

    @GetMapping("/groups/{groupId}")
    public QuestionGroupView findGroup(@PathVariable String groupId,
                                       @RequestParam(required = false) String sellerId,
                                       @RequestParam(required = false) QuestionStatus status,
                                       @RequestParam(required = false) String search,
                                       @RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        String effectiveSeller = identity.sellerId() != null ? identity.sellerId() : sellerId;
        if (effectiveSeller != null) sessions.requireSeller(token, effectiveSeller);
        return questionService.findGroup(effectiveSeller, status, search, groupId);
    }

    @PostMapping("/groups/analyze")
    public QuestionGroupAnalysisView analyzeGroups(@RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        String sellerId = identity.sellerId();
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Se requiere un vendedor para analizar los grupos");
        }
        var state = questionService.requestGroupAnalysis(sellerId);
        return new QuestionGroupAnalysisView(state.getStatus(), state.getStartedAt(), state.getFinishedAt(), state.getError(), state.getGroups());
    }

    @GetMapping("/groups/analyze/status")
    public QuestionGroupAnalysisView analyzeGroupsStatus(@RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        String sellerId = identity.sellerId();
        if (sellerId == null || sellerId.isBlank()) {
            throw new IllegalArgumentException("Se requiere un vendedor para consultar el análisis");
        }
        return questionService.groupAnalysisStatus(sellerId);
    }

    @PostMapping("/{id}/answer")
    public QuestionView answer(@PathVariable String id, @Valid @RequestBody AnswerRequest request,
                               @RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        return questionService.answer(id, request.answer(), identity.sellerId());
    }

    public record AnswerRequest(@NotBlank @Size(max = 1000) String answer) {}
}
