package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.orderrefill.OrderRefillCreateRequest;
import com.antipanel.backend.dto.orderrefill.OrderRefillResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillSummary;
import com.antipanel.backend.entity.OrderRefill;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for OrderRefill entity.
 * Handles conversion between OrderRefill entity and DTOs.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderRefillMapper {

    /**
     * Convert OrderRefill entity to OrderRefillResponse DTO.
     */
    @Mapping(source = "order.id", target = "orderId")
    OrderRefillResponse toResponse(OrderRefill orderRefill);

    /**
     * Convert OrderRefillCreateRequest to OrderRefill entity.
     * Note: Most fields must be set manually by service layer including
     * order, quantity, and status.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "providerRefillId", ignore = true)
    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    OrderRefill toEntity(OrderRefillCreateRequest createRequest);

    /**
     * Convert OrderRefill entity to OrderRefillSummary DTO.
     */
    @Mapping(source = "order.id", target = "orderId")
    OrderRefillSummary toSummary(OrderRefill orderRefill);

    /**
     * Convert list of OrderRefills to list of OrderRefillResponse DTOs.
     */
    List<OrderRefillResponse> toResponseList(List<OrderRefill> orderRefills);

    /**
     * Convert list of OrderRefills to list of OrderRefillSummary DTOs.
     */
    List<OrderRefillSummary> toSummaryList(List<OrderRefill> orderRefills);
}
