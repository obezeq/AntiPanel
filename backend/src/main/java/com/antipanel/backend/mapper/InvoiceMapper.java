package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.invoice.InvoiceCreateRequest;
import com.antipanel.backend.dto.invoice.InvoiceResponse;
import com.antipanel.backend.dto.invoice.InvoiceSummary;
import com.antipanel.backend.entity.Invoice;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Invoice entity.
 * Handles conversion between Invoice entity and DTOs.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class, PaymentProcessorMapper.class}
)
public interface InvoiceMapper {

    /**
     * Convert Invoice entity to InvoiceResponse DTO.
     * Maps user and processor relationships.
     */
    InvoiceResponse toResponse(Invoice invoice);

    /**
     * Convert InvoiceCreateRequest to Invoice entity.
     * Note: Most fields must be set manually by service layer including
     * user, processor, fee, netAmount, and status.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "processor", ignore = true)
    @Mapping(target = "processorInvoiceId", ignore = true)
    @Mapping(target = "fee", ignore = true)
    @Mapping(target = "netAmount", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentUrl", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Invoice toEntity(InvoiceCreateRequest createRequest);

    /**
     * Convert Invoice entity to InvoiceSummary DTO.
     */
    InvoiceSummary toSummary(Invoice invoice);

    /**
     * Convert list of Invoices to list of InvoiceResponse DTOs.
     */
    List<InvoiceResponse> toResponseList(List<Invoice> invoices);

    /**
     * Convert list of Invoices to list of InvoiceSummary DTOs.
     */
    List<InvoiceSummary> toSummaryList(List<Invoice> invoices);
}
