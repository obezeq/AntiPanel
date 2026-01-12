package com.antipanel.backend.repository;

import com.antipanel.backend.entity.BalanceHold;
import com.antipanel.backend.entity.enums.BalanceHoldStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BalanceHold entity.
 * Handles balance reservation operations with pessimistic locking.
 */
@Repository
public interface BalanceHoldRepository extends JpaRepository<BalanceHold, Long> {

    /**
     * Find balance hold by idempotency key.
     *
     * @param idempotencyKey Unique request identifier
     * @return Optional balance hold
     */
    Optional<BalanceHold> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find balance hold by ID with pessimistic write lock and timeout.
     * Prevents concurrent modifications during capture/release operations.
     *
     * @param id Hold ID
     * @return Optional balance hold with exclusive lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT bh FROM BalanceHold bh WHERE bh.id = :id")
    Optional<BalanceHold> findByIdForUpdate(@Param("id") Long id);

    /**
     * Find all expired holds that are still in HELD status.
     * Used by the cleanup scheduler to refund timed-out reservations.
     *
     * @param now Current timestamp
     * @return List of expired holds
     */
    @Query("SELECT bh FROM BalanceHold bh WHERE bh.status = 'HELD' AND bh.expiresAt < :now")
    List<BalanceHold> findExpiredHolds(@Param("now") LocalDateTime now);

    /**
     * Find balance holds by user and status.
     *
     * @param userId User ID
     * @param status Hold status
     * @return List of matching holds
     */
    List<BalanceHold> findByUserIdAndStatus(Long userId, BalanceHoldStatus status);

    /**
     * Find balance hold by reference type and ID.
     * Useful for finding holds linked to specific orders.
     *
     * @param referenceType Type of reference (e.g., "ORDER")
     * @param referenceId   ID of the referenced entity
     * @return Optional balance hold
     */
    Optional<BalanceHold> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
