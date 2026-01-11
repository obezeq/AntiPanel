package com.antipanel.backend.service.impl;

import com.antipanel.backend.entity.BalanceHold;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.BalanceHoldStatus;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.exception.BadRequestException;
import com.antipanel.backend.exception.HoldAlreadyReleasedException;
import com.antipanel.backend.exception.InsufficientBalanceException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.repository.BalanceHoldRepository;
import com.antipanel.backend.repository.TransactionRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.BalanceHoldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BalanceHoldService.
 * Handles balance reservations for order creation with proper ACID guarantees.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceHoldServiceImpl implements BalanceHoldService {

    private final BalanceHoldRepository balanceHoldRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public BalanceHold createHold(Long userId, BigDecimal amount, String idempotencyKey, Duration holdDuration) {
        log.debug("Creating balance hold for user {} amount {} key {}", userId, amount, idempotencyKey);

        // 1. Lock user FIRST to serialize concurrent requests
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 2. Check idempotency AFTER lock (now serialized, no race condition)
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<BalanceHold> existing = balanceHoldRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Returning existing hold {} for idempotency key {}",
                        existing.get().getId(), idempotencyKey);
                return existing.get();
            }
        }

        // 3. Validate user can place orders
        if (user.getIsBanned()) {
            throw new BadRequestException("User is banned and cannot place orders");
        }

        // 4. Validate balance
        if (user.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Required: %s, Available: %s",
                            amount, user.getBalance()));
        }

        // 5. Deduct balance (reservation)
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        // 6. Create hold record
        BalanceHold hold = BalanceHold.builder()
                .user(user)
                .amount(amount)
                .status(BalanceHoldStatus.HELD)
                .idempotencyKey(idempotencyKey)
                .expiresAt(LocalDateTime.now().plus(holdDuration))
                .build();

        BalanceHold saved = balanceHoldRepository.save(hold);
        log.info("Created balance hold {} for user {} amount {}", saved.getId(), userId, amount);

        return saved;
    }

    @Override
    @Transactional
    public void captureHold(Long holdId, Long orderId) {
        log.debug("Capturing hold {} for order {}", holdId, orderId);

        BalanceHold hold = balanceHoldRepository.findByIdForUpdate(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("BalanceHold", "id", holdId));

        // Handle all non-HELD states
        switch (hold.getStatus()) {
            case CAPTURED:
                log.warn("Hold {} already captured, skipping (idempotent)", holdId);
                return;
            case RELEASED:
            case EXPIRED:
                log.error("CRITICAL: Hold {} already {} - order {} requires manual review",
                        holdId, hold.getStatus(), orderId);
                throw new HoldAlreadyReleasedException(holdId,
                        String.format("Hold %d was %s. Order %d needs review.",
                                holdId, hold.getStatus(), orderId));
            case HELD:
                // Expected state - proceed
                break;
        }

        // Update hold
        hold.setStatus(BalanceHoldStatus.CAPTURED);
        hold.setReferenceType("ORDER");
        hold.setReferenceId(orderId);
        balanceHoldRepository.save(hold);

        // Create transaction record (balance was already deducted during hold creation)
        Transaction transaction = Transaction.builder()
                .user(hold.getUser())
                .type(TransactionType.ORDER)
                .amount(hold.getAmount().negate())
                .balanceBefore(hold.getUser().getBalance().add(hold.getAmount()))
                .balanceAfter(hold.getUser().getBalance())
                .referenceType("ORDER")
                .referenceId(orderId)
                .description("Order #" + orderId + " - balance captured from hold #" + holdId)
                .build();
        transactionRepository.save(transaction);

        log.info("Captured hold {} for order {}", holdId, orderId);
    }

    @Override
    @Transactional
    public void releaseHold(Long holdId, String reason) {
        log.debug("Releasing hold {} reason: {}", holdId, reason);

        BalanceHold hold = balanceHoldRepository.findByIdForUpdate(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("BalanceHold", "id", holdId));

        if (hold.getStatus() != BalanceHoldStatus.HELD) {
            log.warn("Hold {} already processed (status: {}), skipping release", holdId, hold.getStatus());
            return; // Idempotent
        }

        // Refund balance
        User user = hold.getUser();
        user.setBalance(user.getBalance().add(hold.getAmount()));
        userRepository.save(user);

        // Update hold
        hold.setStatus(BalanceHoldStatus.RELEASED);
        hold.setReleaseReason(reason);
        balanceHoldRepository.save(hold);

        log.info("Released hold {} amount {} to user {} reason: {}",
                holdId, hold.getAmount(), user.getId(), reason);
    }

    @Override
    public Optional<BalanceHold> findByIdempotencyKey(String idempotencyKey) {
        return balanceHoldRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    @Transactional
    public int releaseExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<BalanceHold> expiredHolds = balanceHoldRepository.findExpiredHolds(now);

        int released = 0;
        for (BalanceHold hold : expiredHolds) {
            try {
                releaseHold(hold.getId(), "Hold expired - automatic cleanup");
                released++;
            } catch (Exception e) {
                log.error("Failed to release expired hold {}: {}", hold.getId(), e.getMessage());
            }
        }

        if (released > 0) {
            log.info("Released {} expired balance holds", released);
        }

        return released;
    }
}
