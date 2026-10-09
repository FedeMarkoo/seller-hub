package com.sellerhub.infrastructure.web;

import com.sellerhub.application.dto.MetricsView;
import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.service.MetricsService;
import com.sellerhub.application.service.QuestionService;
import com.sellerhub.config.DemoDataLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OperationsController {
    private final QuestionService questionService;
    private final MetricsService metricsService;
    private final DemoDataLoader demoDataLoader;

    @GetMapping("/operations/questions")
    public List<QuestionView> pendingQuestions() {
        return questionService.pendingPrioritized();
    }

    @GetMapping("/operations/metrics")
    public MetricsView metrics() {
        return metricsService.calculate();
    }

    @PostMapping("/demo/reset")
    public ResponseEntity<Map<String, String>> resetDemo() throws IOException {
        demoDataLoader.reload();
        return ResponseEntity.ok(Map.of("message", "Datos demo recargados"));
    }
}
