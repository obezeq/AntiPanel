package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.servicetype.ServiceTypeCreateRequest;
import com.antipanel.backend.dto.servicetype.ServiceTypeResponse;
import com.antipanel.backend.dto.servicetype.ServiceTypeSummary;
import com.antipanel.backend.dto.servicetype.ServiceTypeUpdateRequest;
import com.antipanel.backend.entity.ServiceType;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for ServiceType entity.
 * Handles conversion between ServiceType entity and DTOs.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CategoryMapper.class}
)
public interface ServiceTypeMapper extends BaseMapper<ServiceType, ServiceTypeResponse, ServiceTypeCreateRequest, ServiceTypeUpdateRequest, ServiceTypeSummary> {

    /**
     * Convert ServiceType entity to ServiceTypeResponse DTO.
     * Note: serviceCount is not auto-mapped (must be set manually if needed).
     */
    @Override
    @Mapping(target = "serviceCount", ignore = true)
    ServiceTypeResponse toResponse(ServiceType serviceType);

    /**
     * Convert ServiceTypeCreateRequest to ServiceType entity.
     * Note: Category must be set manually in service layer using categoryId.
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)  // Set manually from categoryId
    ServiceType toEntity(ServiceTypeCreateRequest createRequest);

    /**
     * Update ServiceType entity from ServiceTypeUpdateRequest.
     * Null values are ignored (supports partial updates).
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)  // Category cannot be changed
    @Mapping(target = "slug", ignore = true)       // Slug should not be changed after creation
    void updateEntityFromDto(ServiceTypeUpdateRequest updateRequest, @MappingTarget ServiceType serviceType);

    /**
     * Convert ServiceType entity to ServiceTypeSummary DTO.
     */
    @Override
    ServiceTypeSummary toSummary(ServiceType serviceType);

    /**
     * Convert list of ServiceTypes to list of ServiceTypeResponse DTOs.
     */
    @Override
    List<ServiceTypeResponse> toResponseList(List<ServiceType> serviceTypes);

    /**
     * Convert list of ServiceTypes to list of ServiceTypeSummary DTOs.
     */
    @Override
    List<ServiceTypeSummary> toSummaryList(List<ServiceType> serviceTypes);

    /**
     * Convert ServiceType to ServiceTypeResponse with serviceCount.
     * Utility method for enriching response with count data.
     */
    default ServiceTypeResponse toResponseWithCount(ServiceType serviceType, Long serviceCount) {
        ServiceTypeResponse response = toResponse(serviceType);
        response.setServiceCount(serviceCount);
        return response;
    }
}
