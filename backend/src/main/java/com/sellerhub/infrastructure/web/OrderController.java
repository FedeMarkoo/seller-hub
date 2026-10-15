package com.sellerhub.infrastructure.web;

import com.sellerhub.application.dto.OrderView;
import com.sellerhub.application.dto.PageView;
import com.sellerhub.application.service.OrderService;
import com.sellerhub.application.dto.CreateQuestionRequest;
import com.sellerhub.application.dto.QuestionView;
import com.sellerhub.application.service.QuestionService;
import com.sellerhub.application.dto.OrderStatusRequest;
import com.sellerhub.domain.model.OrderStatus;
import jakarta.validation.Valid;
import com.sellerhub.infrastructure.security.DemoRole;
import com.sellerhub.infrastructure.security.DemoSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final DemoSessionService sessions;
    private final QuestionService questionService;

    @GetMapping
    public PageView<OrderView> find(@RequestParam(required = false) String sellerId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "100") int pageSize,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) OrderStatus status,
                                @RequestParam(required = false) LocalDate from,
                                @RequestParam(required = false) LocalDate to,
                                @RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        if (identity.role() == DemoRole.SELLER && sellerId != null && !identity.canAccessSeller(sellerId)) {
            throw new com.sellerhub.infrastructure.security.ForbiddenException();
        }
        String effectiveSeller = identity.sellerId() != null ? identity.sellerId() : sellerId;
        if (effectiveSeller != null) sessions.requireSeller(token, effectiveSeller);
        return orderService.findOrdersPage(effectiveSeller, page, pageSize, search, status, from, to);
    }

    @GetMapping("/{id}")
    public OrderView findById(@PathVariable String id, @RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        OrderView view = orderService.findById(id);
        if (!identity.canAccessSeller(view.sellerId())) {
            throw new com.sellerhub.infrastructure.security.ForbiddenException();
        }
        return view;
    }

    @PostMapping("/{id}/status")
    public OrderView transition(@PathVariable String id, @Valid @RequestBody OrderStatusRequest request,
                                @RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        return orderService.transition(id, request.status(), identity.sellerId());
    }

    @PostMapping("/{id}/questions")
    public QuestionView createQuestion(@PathVariable String id, @Valid @RequestBody CreateQuestionRequest request,
                                       @RequestHeader("X-Demo-Session") String token) {
        var identity = sessions.require(token);
        return questionService.create(id, request, identity.sellerId());
    }
}
