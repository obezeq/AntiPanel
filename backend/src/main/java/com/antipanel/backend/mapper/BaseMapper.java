package com.antipanel.backend.mapper;

import java.util.List;

/**
 * Base mapper interface for MapStruct mappers.
 * Provides common mapping methods for all entities.
 *
 * @param <E> Entity type
 * @param <R> Response DTO type
 * @param <C> Create request DTO type
 * @param <U> Update request DTO type
 * @param <S> Summary DTO type
 */
public interface BaseMapper<E, R, C, U, S> {

    /**
     * Convert entity to response DTO.
     */
    R toResponse(E entity);

    /**
     * Convert create request DTO to entity.
     */
    E toEntity(C createRequest);

    /**
     * Update entity from update request DTO.
     * Null values in the DTO should be ignored.
     */
    void updateEntityFromDto(U updateRequest, @org.mapstruct.MappingTarget E entity);

    /**
     * Convert entity to summary DTO.
     */
    S toSummary(E entity);

    /**
     * Convert list of entities to list of response DTOs.
     */
    List<R> toResponseList(List<E> entities);

    /**
     * Convert list of entities to list of summary DTOs.
     */
    List<S> toSummaryList(List<E> entities);
}
