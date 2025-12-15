package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.order.OrderCreateRequest;
import com.antipanel.backend.dto.order.OrderDetailResponse;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.dto.order.OrderSummary;
import com.antipanel.backend.entity.Order;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Order entity.
 * Handles conversion between Order entity and DTOs.
 * Note: Orders have computed fields (progress, canRequestRefill) that require enrichment.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class, ServiceMapper.class}
)
public interface OrderMapper {

    /**
     * Convert Order entity to OrderResponse DTO.
     * Note: progress and canRequestRefill are computed from entity methods.
     */
    @Named("toResponse")
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(target = "progress", expression = "java(order.getProgress())")
    @Mapping(target = "canRequestRefill", expression = "java(order.canRequestRefill())")
    OrderResponse toResponse(Order order);

    /**
     * Convert Order entity to OrderDetailResponse DTO.
     * Includes nested service details.
     */
    @Mapping(source = "service", target = "service")
    @Mapping(target = "progress", expression = "java(order.getProgress())")
    @Mapping(target = "canRequestRefill", expression = "java(order.canRequestRefill())")
    OrderDetailResponse toDetailResponse(Order order);

    /**
     * Convert OrderCreateRequest to Order entity.
     * Note: Most fields must be set manually by service layer including
     * user, service, providerService, and all calculated fields.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "serviceName", ignore = true)
    @Mapping(target = "providerService", ignore = true)
    @Mapping(target = "providerOrderId", ignore = true)
    @Mapping(target = "startCount", ignore = true)
    @Mapping(target = "remains", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "pricePerK", ignore = true)
    @Mapping(target = "costPerK", ignore = true)
    @Mapping(target = "totalCharge", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    @Mapping(target = "profit", ignore = true)
    @Mapping(target = "isRefillable", ignore = true)
    @Mapping(target = "refillDays", ignore = true)
    @Mapping(target = "refillDeadline", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderCreateRequest createRequest);

    /**
     * Convert Order entity to OrderSummary DTO.
     */
    OrderSummary toSummary(Order order);

    /**
     * Convert list of Orders to list of OrderResponse DTOs.
     */
    List<OrderResponse> toResponseList(List<Order> orders);

    /**
     * Convert list of Orders to list of OrderSummary DTOs.
     */
    List<OrderSummary> toSummaryList(List<Order> orders);

    /**
     * Convert list of Orders to list of OrderDetailResponse DTOs.
     */
    List<OrderDetailResponse> toDetailResponseList(List<Order> orders);
}
