package com.example.promptengineering.dto;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;

public record PageDto<T>(List<T> content, int page, int size, long totalElements,
        int totalPages, boolean first, boolean last) {
    public static <E, R> PageDto<R> from(Page<E> page, Function<E, R> mapper) {
        return new PageDto<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast());
    }

    public static <T> PageDto<T> of(List<T> content, int page, int size,
                                    long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PageDto<>(content, page, size, totalElements, totalPages, page == 0,
                page + 1 >= totalPages);
    }
}
