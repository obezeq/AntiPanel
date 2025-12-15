package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.provider.ProviderCreateRequest;
import com.antipanel.backend.dto.provider.ProviderResponse;
import com.antipanel.backend.dto.provider.ProviderSummary;
import com.antipanel.backend.dto.provider.ProviderUpdateRequest;
import com.antipanel.backend.entity.Provider;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Provider entity.
 * Handles conversion between Provider entity and DTOs.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProviderMapper extends BaseMapper<Provider, ProviderResponse, ProviderCreateRequest, ProviderUpdateRequest, ProviderSummary> {

    /**
     * Convert Provider entity to ProviderResponse DTO.
     * Note: serviceCount is not auto-mapped (must be set manually if needed).
     */
    @Override
    @Mapping(target = "serviceCount", ignore = true)
    ProviderResponse toResponse(Provider provider);

    /**
     * Convert ProviderCreateRequest to Provider entity.
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)     // Set default in service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Provider toEntity(ProviderCreateRequest createRequest);

    /**
     * Update Provider entity from ProviderUpdateRequest.
     * Null values are ignored (supports partial updates).
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)     // Balance not updatable via DTO
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ProviderUpdateRequest updateRequest, @MappingTarget Provider provider);

    /**
     * Convert Provider entity to ProviderSummary DTO.
     */
    @Override
    ProviderSummary toSummary(Provider provider);

    /**
     * Convert list of Providers to list of ProviderResponse DTOs.
     */
    @Override
    List<ProviderResponse> toResponseList(List<Provider> providers);

    /**
     * Convert list of Providers to list of ProviderSummary DTOs.
     */
    @Override
    List<ProviderSummary> toSummaryList(List<Provider> providers);

    /**
     * Convert Provider to ProviderResponse with serviceCount.
     * Utility method for enriching response with count data.
     */
    default ProviderResponse toResponseWithCount(Provider provider, Long serviceCount) {
        ProviderResponse response = toResponse(provider);
        response.setServiceCount(serviceCount);
        return response;
    }
}
