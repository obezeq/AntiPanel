package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.transaction.TransactionResponse;
import com.antipanel.backend.dto.transaction.TransactionSummary;
import com.antipanel.backend.entity.Transaction;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for Transaction entity.
 * Handles conversion between Transaction entity and DTOs.
 * Note: Transactions are read-only audit records - no create/update methods.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class}
)
public interface TransactionMapper {

    /**
     * Convert Transaction entity to TransactionResponse DTO.
     * Maps user relationship.
     */
    TransactionResponse toResponse(Transaction transaction);

    /**
     * Convert Transaction entity to TransactionSummary DTO.
     */
    TransactionSummary toSummary(Transaction transaction);

    /**
     * Convert list of Transactions to list of TransactionResponse DTOs.
     */
    List<TransactionResponse> toResponseList(List<Transaction> transactions);

    /**
     * Convert list of Transactions to list of TransactionSummary DTOs.
     */
    List<TransactionSummary> toSummaryList(List<Transaction> transactions);
}
