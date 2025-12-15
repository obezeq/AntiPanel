package com.antipanel.backend.controller;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.transaction.TransactionResponse;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.security.CurrentUser;
import com.antipanel.backend.security.CustomUserDetails;
import com.antipanel.backend.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for user transaction history.
 * All endpoints require authentication.
 * Transactions are read-only audit records.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "User transaction history endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Get user's transaction history with pagination",
            description = "Returns paginated list of transactions for the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getUserTransactions(
            @CurrentUser CustomUserDetails currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("Getting transactions for user ID: {}", currentUser.getUserId());
        PageResponse<TransactionResponse> response = transactionService.getByUserPaginated(currentUser.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user's transactions by type",
            description = "Returns transactions filtered by type (DEPOSIT, ORDER, REFUND, etc.)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TransactionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/type")
    public ResponseEntity<List<TransactionResponse>> getUserTransactionsByType(
            @CurrentUser CustomUserDetails currentUser,
            @Parameter(description = "Transaction type", example = "DEPOSIT")
            @RequestParam TransactionType type) {
        log.debug("Getting transactions of type {} for user ID: {}", type, currentUser.getUserId());
        List<TransactionResponse> response = transactionService.getByUserAndType(currentUser.getUserId(), type);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get latest transaction",
            description = "Returns the most recent transaction for the current user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "204", description = "No transactions found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/latest")
    public ResponseEntity<TransactionResponse> getLatestTransaction(@CurrentUser CustomUserDetails currentUser) {
        log.debug("Getting latest transaction for user ID: {}", currentUser.getUserId());
        TransactionResponse response = transactionService.getLatestByUser(currentUser.getUserId());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}
