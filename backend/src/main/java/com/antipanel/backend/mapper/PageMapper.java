package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Utility mapper for converting Spring Data Page to PageResponse DTO.
 */
@Component
public class PageMapper {

    /**
     * Convert a Spring Data Page to a PageResponse.
     *
     * @param page   The Spring Data Page
     * @param mapper Function to map content items
     * @param <E>    Entity type
     * @param <D>    DTO type
     * @return PageResponse containing mapped content
     */
    public <E, D> PageResponse<D> toPageResponse(Page<E> page, Function<E, D> mapper) {
        List<D> content = page.getContent().stream()
                .map(mapper)
                .toList();

        return PageResponse.<D>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Convert a Spring Data Page to a PageResponse using a list of already mapped content.
     *
     * @param page    The Spring Data Page
     * @param content The already mapped content
     * @param <D>     DTO type
     * @return PageResponse containing the content
     */
    public <D> PageResponse<D> toPageResponse(Page<?> page, List<D> content) {
        return PageResponse.<D>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
