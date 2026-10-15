package com.sellerhub.application.dto;

import com.sellerhub.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(@NotNull OrderStatus status) {}
