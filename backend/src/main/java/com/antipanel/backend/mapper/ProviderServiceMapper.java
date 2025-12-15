package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.providerservice.ProviderServiceCreateRequest;
import com.antipanel.backend.dto.providerservice.ProviderServiceResponse;
import com.antipanel.backend.dto.providerservice.ProviderServiceSummary;
import com.antipanel.backend.dto.providerservice.ProviderServiceUpdateRequest;
import com.antipanel.backend.entity.ProviderService;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for ProviderService entity.
 * Handles conversion between ProviderService entity and DTOs.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {ProviderMapper.class}
)
public interface ProviderServiceMapper extends BaseMapper<ProviderService, ProviderServiceResponse, ProviderServiceCreateRequest, ProviderServiceUpdateRequest, ProviderServiceSummary> {

    /**
     * Convert ProviderService entity to ProviderServiceResponse DTO.
     * Maps provider relationship to ProviderSummary.
     */
    @Override
    ProviderServiceResponse toResponse(ProviderService providerService);

    /**
     * Convert ProviderServiceCreateRequest to ProviderService entity.
     * Note: Provider must be set manually in service layer using providerId.
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provider", ignore = true)     // Set manually from providerId
    @Mapping(target = "lastSyncedAt", ignore = true)
    ProviderService toEntity(ProviderServiceCreateRequest createRequest);

    /**
     * Update ProviderService entity from ProviderServiceUpdateRequest.
     * Null values are ignored (supports partial updates).
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provider", ignore = true)         // Provider cannot be changed
    @Mapping(target = "providerServiceId", ignore = true) // Cannot change external ID
    @Mapping(target = "lastSyncedAt", ignore = true)
    void updateEntityFromDto(ProviderServiceUpdateRequest updateRequest, @MappingTarget ProviderService providerService);

    /**
     * Convert ProviderService entity to ProviderServiceSummary DTO.
     */
    @Override
    ProviderServiceSummary toSummary(ProviderService providerService);

    /**
     * Convert list of ProviderServices to list of ProviderServiceResponse DTOs.
     */
    @Override
    List<ProviderServiceResponse> toResponseList(List<ProviderService> providerServices);

    /**
     * Convert list of ProviderServices to list of ProviderServiceSummary DTOs.
     */
    @Override
    List<ProviderServiceSummary> toSummaryList(List<ProviderService> providerServices);
}
