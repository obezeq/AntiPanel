package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.service.ServiceCreateRequest;
import com.antipanel.backend.dto.service.ServiceDetailResponse;
import com.antipanel.backend.dto.service.ServiceResponse;
import com.antipanel.backend.dto.service.ServiceSummary;
import com.antipanel.backend.dto.service.ServiceUpdateRequest;
import com.antipanel.backend.entity.Service;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * MapStruct mapper for Service entity.
 * Handles conversion between Service entity and DTOs.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CategoryMapper.class, ServiceTypeMapper.class, ProviderServiceMapper.class}
)
public interface ServiceMapper extends BaseMapper<Service, ServiceResponse, ServiceCreateRequest, ServiceUpdateRequest, ServiceSummary> {

    /**
     * Convert Service entity to ServiceResponse DTO.
     * Maps flat IDs and includes cost/profit margin fields.
     */
    @Override
    @Named("toResponse")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "serviceType.id", target = "serviceTypeId")
    @Mapping(source = "providerService.id", target = "providerServiceId")
    @Mapping(source = "providerService.costPerK", target = "costPerK")
    @Mapping(target = "profitMargin", ignore = true)  // Calculated in enrichWithProfitMargin
    ServiceResponse toResponse(Service service);

    /**
     * Convert Service entity to ServiceDetailResponse DTO.
     * Includes nested DTOs for category, serviceType, and providerService.
     */
    @Mapping(source = "category", target = "category")
    @Mapping(source = "serviceType", target = "serviceType")
    @Mapping(source = "providerService", target = "providerService")
    ServiceDetailResponse toDetailResponse(Service service);

    /**
     * Convert ServiceCreateRequest to Service entity.
     * Note: Category, ServiceType, and ProviderService must be set manually
     * in service layer using their respective IDs.
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)          // Set manually from categoryId
    @Mapping(target = "serviceType", ignore = true)       // Set manually from serviceTypeId
    @Mapping(target = "providerService", ignore = true)   // Set manually from providerServiceId
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Service toEntity(ServiceCreateRequest createRequest);

    /**
     * Update Service entity from ServiceUpdateRequest.
     * Null values are ignored (supports partial updates).
     */
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)          // Cannot change category
    @Mapping(target = "serviceType", ignore = true)       // Cannot change service type
    @Mapping(target = "providerService", ignore = true)   // Cannot change provider service
    @Mapping(target = "quality", ignore = true)           // Cannot change quality
    @Mapping(target = "speed", ignore = true)             // Cannot change speed
    @Mapping(target = "refillDays", ignore = true)        // Cannot change refill days
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ServiceUpdateRequest updateRequest, @MappingTarget Service service);

    /**
     * Convert Service entity to ServiceSummary DTO.
     */
    @Override
    ServiceSummary toSummary(Service service);

    /**
     * Convert list of Services to list of ServiceResponse DTOs.
     */
    @Override
    @IterableMapping(qualifiedByName = "toResponse")
    List<ServiceResponse> toResponseList(List<Service> services);

    /**
     * Convert list of Services to list of ServiceSummary DTOs.
     */
    @Override
    List<ServiceSummary> toSummaryList(List<Service> services);

    /**
     * Convert list of Services to list of ServiceDetailResponse DTOs.
     */
    List<ServiceDetailResponse> toDetailResponseList(List<Service> services);

    /**
     * Enrich ServiceResponse with profit margin calculation.
     * Formula: (pricePerK - costPerK) / pricePerK * 100
     */
    default ServiceResponse enrichWithProfitMargin(Service service) {
        ServiceResponse response = toResponse(service);
        if (response.getPricePerK() != null && response.getCostPerK() != null
                && response.getPricePerK().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margin = response.getPricePerK()
                    .subtract(response.getCostPerK())
                    .divide(response.getPricePerK(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            response.setProfitMargin(margin);
        }
        return response;
    }
}
