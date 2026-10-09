package com.sellerhub.infrastructure.web;

import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.service.QuestionService;
import com.sellerhub.domain.model.QuestionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping
    public List<QuestionView> find(@RequestParam(required = false) String sellerId,
                                   @RequestParam(required = false) QuestionStatus status) {
        return questionService.find(sellerId, status);
    }

    @PostMapping("/{id}/answer")
    public QuestionView answer(@PathVariable String id, @Valid @RequestBody AnswerRequest request) {
        return questionService.answer(id, request.answer());
    }

    public record AnswerRequest(@NotBlank @Size(max = 1000) String answer) {}
}
