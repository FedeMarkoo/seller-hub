package com.sellerhub.application.dto;

import java.util.List;

public record PageView<T>(List<T> items, int page, int pageSize, long total, boolean hasNext) {
}
