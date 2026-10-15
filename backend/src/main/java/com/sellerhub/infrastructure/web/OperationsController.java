package com.sellerhub.infrastructure.web;

import com.sellerhub.application.dto.MetricsView;
import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.dto.PageView;
import com.sellerhub.application.service.MetricsService;
import com.sellerhub.application.service.QuestionService;
import com.sellerhub.config.DemoDataLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.sellerhub.infrastructure.security.DemoSessionService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OperationsController {
    private final QuestionService questionService;
    private final MetricsService metricsService;
    private final DemoDataLoader demoDataLoader;
    private final DemoSessionService sessions;

    @GetMapping("/operations/questions")
    public PageView<QuestionView> pendingQuestions(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "100") int pageSize,
                                               @RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        sessions.requireOperations(token);
        return questionService.pendingPrioritizedPage(identity.sellerId(), page, pageSize);
    }

    @GetMapping("/operations/metrics")
    public MetricsView metrics(@RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        sessions.requireOperations(token);
        return metricsService.calculate(identity.sellerId());
    }

    @PostMapping("/demo/reset")
    public ResponseEntity<Map<String, String>> resetDemo(@RequestHeader("X-Demo-Session") String token) throws IOException {
        sessions.requireOperations(token);
        demoDataLoader.reload();
        return ResponseEntity.ok(Map.of("message", "Datos demo recargados"));
    }
}
