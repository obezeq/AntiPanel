package com.antipanel.backend.service.impl;

import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.repository.TransactionRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.OrderCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementation of OrderCompensationService.
 * Handles order compensation in a separate transaction to ensure
 * refunds persist even when the calling transaction rolls back.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCompensationServiceImpl implements OrderCompensationService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Compensate a failed order by refunding the user.
     * Uses REQUIRES_NEW propagation to ensure the refund is NOT rolled back
     * even if the calling transaction fails/rolls back.
     *
     * This method is idempotent - calling it multiple times for the same
     * order will have no additional effect after the first successful compensation.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void compensateFailedOrder(Long orderId) {
        log.debug("Compensating failed order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Idempotency check - prevent double compensation
        if (order.getStatus() == OrderStatus.FAILED || order.getStatus() == OrderStatus.REFUNDED) {
            log.warn("Order ID: {} already compensated (status: {}), skipping", orderId, order.getStatus());
            return;
        }

        User user = order.getUser();
        BigDecimal amount = order.getTotalCharge();

        // Refund balance
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        user.setBalance(balanceAfter);
        userRepository.save(user);

        // Create refund transaction
        Transaction refundTransaction = Transaction.builder()
                .user(user)
                .type(TransactionType.REFUND)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceType("ORDER")
                .referenceId(order.getId())
                .description("Auto-refund: Order #" + order.getId() + " - provider submission failed")
                .build();
        transactionRepository.save(refundTransaction);

        // Mark order as failed
        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        log.info("Compensated failed order ID: {}. Refunded {} to user ID: {}",
                orderId, amount, user.getId());
    }
}
