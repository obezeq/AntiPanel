package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.order.OrderCreateRequest;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.entity.BalanceHold;
import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.ProviderService;
import com.antipanel.backend.entity.Service;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.exception.BadRequestException;
import com.antipanel.backend.exception.ProviderApiException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.OrderMapper;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.repository.ServiceRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.BalanceHoldService;
import com.antipanel.backend.service.ExternalOrderService;
import com.antipanel.backend.service.OrderCreationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Optional;

/**
 * Implementation of OrderCreationFacade using the Balance Reservation Pattern.
 * Uses TransactionTemplate for programmatic transaction control (Spring Framework 7 best practice).
 *
 * Transaction Flow:
 * 1. Transaction 1: Create balance hold (reserves funds, checks idempotency)
 * 2. Transaction 2: Create pending order
 * 3. NO transaction: External API call to provider
 * 4. Transaction 3: Capture hold on success, release on failure
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class OrderCreationFacadeImpl implements OrderCreationFacade {

    private static final Duration HOLD_DURATION = Duration.ofMinutes(15);

    private final BalanceHoldService balanceHoldService;
    private final OrderRepository orderRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ExternalOrderService externalOrderService;
    private final OrderMapper orderMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {
        log.debug("Creating order for user ID: {} with idempotency key: {}",
                userId, request.getIdempotencyKey());

        // Step 1: Validate service and calculate total (read-only, no transaction needed)
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        validateServiceAndQuantity(service, request.getQuantity());
        BigDecimal totalCharge = calculateTotalCharge(service, request.getQuantity());

        // Step 2: Reserve balance with idempotency check inside transaction
        // This serializes requests by user lock and checks idempotency atomically
        BalanceHold hold = balanceHoldService.createHold(
                userId, totalCharge, request.getIdempotencyKey(), HOLD_DURATION);

        // Step 3: Check if order already exists for this hold (idempotent return)
        Optional<Order> existingOrder = orderRepository.findByBalanceHoldId(hold.getId());
        if (existingOrder.isPresent()) {
            log.info("Returning existing order {} for hold {}",
                    existingOrder.get().getId(), hold.getId());
            return orderMapper.toResponse(existingOrder.get());
        }

        // Step 4: Create pending order using TransactionTemplate (separate transaction)
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        Order order;
        try {
            order = transactionTemplate.execute(status ->
                    createPendingOrderInternal(userId, request, hold, request.getServiceId(), totalCharge)
            );
        } catch (Exception e) {
            // CRITICAL: Check if another concurrent request already created the order for this hold
            // This handles the race condition where two requests with the same idempotency key
            // both get the same hold, but one succeeds in creating the order first
            Optional<Order> concurrentOrder = orderRepository.findByBalanceHoldId(hold.getId());
            if (concurrentOrder.isPresent()) {
                log.info("Concurrent request created order {} for hold {}, returning it",
                        concurrentOrder.get().getId(), hold.getId());
                return orderMapper.toResponse(concurrentOrder.get());
            }

            // No concurrent order exists - this is a genuine failure, release the hold
            log.error("Failed to create order for hold {}: {}", hold.getId(), e.getMessage());
            balanceHoldService.releaseHold(hold.getId(), "Order creation failed: " + e.getMessage());
            throw e;
        }

        // Safety check - should never happen but ensures no NPE
        if (order == null) {
            log.error("CRITICAL: Order creation returned null for hold {}", hold.getId());
            balanceHoldService.releaseHold(hold.getId(), "Order creation returned null");
            throw new IllegalStateException("Order creation failed - null result");
        }

        // Step 5: Submit to provider (uses orderId to re-fetch inside @Transactional)
        try {
            OrderResponse response = externalOrderService.submitOrder(order.getId());

            // Step 6a: Capture hold on success
            balanceHoldService.captureHold(hold.getId(), order.getId());

            log.info("Order {} successfully created and submitted to provider", order.getId());
            return response;

        } catch (ProviderApiException e) {
            // Known provider failure - compensate
            log.error("Provider failed for order {}: {}", order.getId(), e.getMessage());
            handleSubmissionFailure(hold.getId(), order.getId(), e.getMessage());
            throw e;

        } catch (Exception e) {
            // Unknown failures - compensate and log critical
            log.error("UNEXPECTED error during order {} submission", order.getId(), e);
            handleSubmissionFailure(hold.getId(), order.getId(),
                    "Unexpected error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw new RuntimeException("Order submission failed unexpectedly", e);
        }
    }

    /**
     * Create the pending order record.
     * Called inside a transaction template.
     */
    private Order createPendingOrderInternal(Long userId, OrderCreateRequest request,
            BalanceHold hold, Integer serviceId, BigDecimal totalCharge) {

        // Re-fetch service inside transaction to ensure Hibernate session is available for lazy loading
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", serviceId));
        ProviderService providerService = service.getProviderService();
        BigDecimal costPerK = providerService.getCostPerK();
        BigDecimal totalCost = calculateTotalAmount(costPerK, request.getQuantity());
        BigDecimal profit = totalCharge.subtract(totalCost);

        Order order = Order.builder()
                .user(userRepository.getReferenceById(userId))
                .service(service)
                .serviceName(service.getName())
                .providerService(providerService)
                .balanceHoldId(hold.getId())
                .idempotencyKey(request.getIdempotencyKey())
                .target(request.getTarget())
                .quantity(request.getQuantity())
                .remains(request.getQuantity())
                .status(OrderStatus.PENDING)
                .pricePerK(service.getPricePerK())
                .costPerK(costPerK)
                .totalCharge(totalCharge)
                .totalCost(totalCost)
                .profit(profit)
                .isRefillable(service.getRefillDays() > 0)
                .refillDays(service.getRefillDays())
                .build();

        Order saved = orderRepository.save(order);
        log.debug("Created pending order {} linked to hold {}", saved.getId(), hold.getId());
        return saved;
    }

    /**
     * Handle submission failure by releasing hold and marking order as failed.
     * Uses REQUIRES_NEW semantics via new TransactionTemplate.
     * Failures during cleanup are logged but don't prevent the original exception from being thrown.
     */
    private void handleSubmissionFailure(Long holdId, Long orderId, String reason) {
        try {
            TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
            requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

            requiresNew.executeWithoutResult(status -> {
                balanceHoldService.releaseHold(holdId, reason);

                Order order = orderRepository.findById(orderId).orElse(null);
                if (order != null && order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.FAILED);
                    orderRepository.save(order);
                    log.info("Marked order {} as FAILED due to: {}", orderId, reason);
                }
            });
        } catch (Exception cleanupEx) {
            // Log cleanup failure but don't mask the original exception
            // The hold will be cleaned up by the scheduled cleanup task
            log.error("CRITICAL: Cleanup failed for hold {} order {}. Manual review required. Error: {}",
                    holdId, orderId, cleanupEx.getMessage(), cleanupEx);
        }
    }

    private void validateServiceAndQuantity(Service service, Integer quantity) {
        if (!service.getIsActive()) {
            throw new BadRequestException("Service is not active");
        }

        if (!service.isQuantityValid(quantity)) {
            throw new BadRequestException(String.format(
                    "Quantity must be between %d and %d",
                    service.getMinQuantity(), service.getMaxQuantity()));
        }
    }

    private BigDecimal calculateTotalCharge(Service service, Integer quantity) {
        return calculateTotalAmount(service.getPricePerK(), quantity);
    }

    private BigDecimal calculateTotalAmount(BigDecimal pricePerK, Integer quantity) {
        return pricePerK
                .multiply(BigDecimal.valueOf(quantity))
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
    }
}
