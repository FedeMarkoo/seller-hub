package com.sellerhub.infrastructure.web;

import com.sellerhub.application.dto.DemoSessionView;
import com.sellerhub.application.mapper.SellerHubMapper;
import com.sellerhub.infrastructure.security.DemoRole;
import com.sellerhub.infrastructure.security.DemoSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/demo/session")
@RequiredArgsConstructor
public class DemoSessionController {
    private final DemoSessionService sessions;
    private final SellerHubMapper mapper;

    @PostMapping
    public DemoSessionView create(@Valid @RequestBody SessionRequest request) {
        return mapper.toDemoSessionView(sessions.create(request.role(), request.sellerId()), request.role(), request.sellerId());
    }

    @DeleteMapping
    public void revoke(@RequestHeader("X-Demo-Session") String token) {
        sessions.revoke(token);
    }

    public record SessionRequest(@NotNull DemoRole role, String sellerId) {}
}
