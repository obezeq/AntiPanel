package com.antipanel.backend.repository;

import com.antipanel.backend.entity.PaymentProcessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentProcessor entity.
 * Handles database operations for payment gateway configurations.
 */
@Repository
public interface PaymentProcessorRepository extends JpaRepository<PaymentProcessor, Integer> {

    // ============ BASIC FINDERS ============

    /**
     * Find payment processor by code
     *
     * @param code Unique processor code
     * @return Optional payment processor
     */
    Optional<PaymentProcessor> findByCode(String code);

    /**
     * Check if payment processor exists by code
     *
     * @param code Unique processor code
     * @return true if exists
     */
    boolean existsByCode(String code);

    // ============ ACTIVE PROCESSORS ============

    /**
     * Find all active payment processors sorted by sort order
     *
     * @return List of active processors
     */
    List<PaymentProcessor> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * Find all active payment processors sorted by sort order and name
     *
     * @return List of active processors
     */
    @Query("SELECT pp FROM PaymentProcessor pp WHERE pp.isActive = true " +
           "ORDER BY pp.sortOrder ASC, pp.name ASC")
    List<PaymentProcessor> findAllActiveProcessors();

    // ============ AMOUNT VALIDATION ============

    /**
     * Find active processors that support a specific amount
     *
     * @param amount Deposit amount to validate
     * @return List of compatible processors
     */
    @Query("SELECT pp FROM PaymentProcessor pp WHERE pp.isActive = true " +
           "AND pp.minAmount <= :amount " +
           "AND (pp.maxAmount IS NULL OR pp.maxAmount >= :amount) " +
           "ORDER BY pp.sortOrder ASC")
    List<PaymentProcessor> findProcessorsForAmount(@Param("amount") BigDecimal amount);

    // ============ STATISTICS ============

    /**
     * Count active payment processors
     *
     * @return Number of active processors
     */
    @Query("SELECT COUNT(pp) FROM PaymentProcessor pp WHERE pp.isActive = true")
    long countActiveProcessors();
}
