package com.sellerhub.application.dto;

import com.sellerhub.infrastructure.security.DemoRole;

public record DemoSessionView(String token, DemoRole role, String sellerId) {
}
