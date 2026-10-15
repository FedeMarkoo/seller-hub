package com.sellerhub.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateQuestionRequest(
        @NotBlank @Size(max = 120) String buyerName,
        @NotBlank @Size(max = 2000) String text
) {}
