package com.sellerhub.application.util;

import com.sellerhub.application.dto.PageView;

import java.util.List;

public final class PaginationUtils {
    private PaginationUtils() {}

    public static <T> PageView<T> paginate(List<T> items, int page, int pageSize) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        int from = Math.min(safePage * safeSize, items.size());
        int to = Math.min(from + safeSize, items.size());
        return new PageView<>(items.subList(from, to), safePage, safeSize, items.size(), to < items.size());
    }
}
