package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorCreateRequest;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorResponse;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorSummary;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorUpdateRequest;
import com.antipanel.backend.entity.PaymentProcessor;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for PaymentProcessor entity.
 * Handles conversion between PaymentProcessor entity and DTOs.
 * Note: API credentials are excluded from response DTOs for security.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentProcessorMapper extends BaseMapper<PaymentProcessor, PaymentProcessorResponse, PaymentProcessorCreateRequest, PaymentProcessorUpdateRequest, PaymentProcessorSummary> {

    /**
     * Convert PaymentProcessor entity to PaymentProcessorResponse DTO.
     * Note: API credentials (apiKey, apiSecret, configJson) are NOT included.
     */
    @Override
    PaymentProcessorResponse toResponse(PaymentProcessor paymentProcessor);

    /**
     * Convert PaymentProcessorCreateRequest to PaymentProcessor entity.
     */
    @Override
    @Mapping(target = "id", ignore = true)
    PaymentProcessor toEntity(PaymentProcessorCreateRequest createRequest);

    /**
     * Update PaymentProcessor entity from PaymentProcessorUpdateRequest.
     * Null values are ignored (supports partial updates).
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)  // Code should not be changed after creation
    void updateEntityFromDto(PaymentProcessorUpdateRequest updateRequest, @MappingTarget PaymentProcessor paymentProcessor);

    /**
     * Convert PaymentProcessor entity to PaymentProcessorSummary DTO.
     */
    @Override
    PaymentProcessorSummary toSummary(PaymentProcessor paymentProcessor);

    /**
     * Convert list of PaymentProcessors to list of PaymentProcessorResponse DTOs.
     */
    @Override
    List<PaymentProcessorResponse> toResponseList(List<PaymentProcessor> paymentProcessors);

    /**
     * Convert list of PaymentProcessors to list of PaymentProcessorSummary DTOs.
     */
    @Override
    List<PaymentProcessorSummary> toSummaryList(List<PaymentProcessor> paymentProcessors);
}
