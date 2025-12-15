package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.transaction.TransactionResponse;
import com.antipanel.backend.dto.transaction.TransactionSummary;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.mapper.TransactionMapper;
import com.antipanel.backend.repository.TransactionRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TransactionService.
 * Handles transaction history, audit records, and statistics.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;
    private final PageMapper pageMapper;

    // ============ CREATE OPERATIONS (Internal use) ============

    @Override
    @Transactional
    public Transaction createTransaction(User user, TransactionType type, BigDecimal amount,
                                         String referenceType, Long referenceId, String description) {
        log.debug("Creating {} transaction for user ID: {} with amount: {}",
                type, user.getId(), amount);

        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(type)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Created transaction ID: {} for user ID: {}", saved.getId(), user.getId());
        return saved;
    }

    // ============ READ OPERATIONS ============

    @Override
    public TransactionResponse getById(Long id) {
        log.debug("Getting transaction by ID: {}", id);
        Transaction transaction = findTransactionById(id);
        return transactionMapper.toResponse(transaction);
    }

    @Override
    public TransactionResponse getLatestByUser(Long userId) {
        log.debug("Getting latest transaction for user ID: {}", userId);
        Optional<Transaction> transaction = transactionRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
        return transaction.map(transactionMapper::toResponse).orElse(null);
    }

    // ============ USER QUERIES ============

    @Override
    public List<TransactionResponse> getByUser(Long userId) {
        log.debug("Getting transactions for user ID: {}", userId);
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return transactionMapper.toResponseList(transactions);
    }

    @Override
    public PageResponse<TransactionResponse> getByUserPaginated(Long userId, Pageable pageable) {
        log.debug("Getting paginated transactions for user ID: {}", userId);
        Page<Transaction> page = transactionRepository.findByUserId(userId, pageable);
        List<TransactionResponse> content = transactionMapper.toResponseList(page.getContent());
        return pageMapper.toPageResponse(page, content);
    }

    @Override
    public List<TransactionResponse> getByUserAndType(Long userId, TransactionType type) {
        log.debug("Getting transactions for user ID: {} with type: {}", userId, type);
        List<Transaction> transactions = transactionRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
        return transactionMapper.toResponseList(transactions);
    }

    @Override
    public List<TransactionResponse> getUserTransactionsBetweenDates(Long userId, LocalDateTime start, LocalDateTime end) {
        log.debug("Getting transactions for user ID: {} between {} and {}", userId, start, end);
        List<Transaction> transactions = transactionRepository.findUserTransactionsBetweenDates(userId, start, end);
        return transactionMapper.toResponseList(transactions);
    }

    // ============ ADMIN QUERIES ============

    @Override
    public List<TransactionResponse> getByType(TransactionType type) {
        log.debug("Getting transactions by type: {}", type);
        List<Transaction> transactions = transactionRepository.findByTypeOrderByCreatedAtDesc(type);
        return transactionMapper.toResponseList(transactions);
    }

    @Override
    public PageResponse<TransactionResponse> getByTypePaginated(TransactionType type, Pageable pageable) {
        log.debug("Getting paginated transactions by type: {}", type);
        Page<Transaction> page = transactionRepository.findByType(type, pageable);
        List<TransactionResponse> content = transactionMapper.toResponseList(page.getContent());
        return pageMapper.toPageResponse(page, content);
    }

    @Override
    public List<TransactionResponse> getByReference(String referenceType, Long referenceId) {
        log.debug("Getting transactions by reference: {} - {}", referenceType, referenceId);
        List<Transaction> transactions = transactionRepository.findByReference(referenceType, referenceId);
        return transactionMapper.toResponseList(transactions);
    }

    @Override
    public List<TransactionResponse> getTransactionsBetweenDates(LocalDateTime start, LocalDateTime end) {
        log.debug("Getting transactions between {} and {}", start, end);
        List<Transaction> transactions = transactionRepository.findTransactionsBetweenDates(start, end);
        return transactionMapper.toResponseList(transactions);
    }

    // ============ AUDIT & VALIDATION ============

    @Override
    public List<TransactionResponse> findInconsistentTransactions() {
        log.debug("Finding inconsistent transactions");
        List<Transaction> transactions = transactionRepository.findInconsistentTransactions();
        if (!transactions.isEmpty()) {
            log.warn("Found {} inconsistent transactions", transactions.size());
        }
        return transactionMapper.toResponseList(transactions);
    }

    @Override
    public boolean validateUserBalance(Long userId) {
        log.debug("Validating balance for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<Transaction> latestTransaction = transactionRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);

        if (latestTransaction.isEmpty()) {
            // No transactions, balance should be initial or zero
            return true;
        }

        BigDecimal expectedBalance = latestTransaction.get().getBalanceAfter();
        boolean isValid = user.getBalance().compareTo(expectedBalance) == 0;

        if (!isValid) {
            log.warn("Balance mismatch for user ID: {}. Current: {}, Expected: {}",
                    userId, user.getBalance(), expectedBalance);
        }

        return isValid;
    }

    // ============ STATISTICS ============

    @Override
    public long countByType(TransactionType type) {
        return transactionRepository.countByType(type);
    }

    @Override
    public BigDecimal getTotalDeposits() {
        BigDecimal total = transactionRepository.getTotalDeposits();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalOrderTransactions() {
        BigDecimal total = transactionRepository.getTotalOrderTransactions();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getDepositsBetweenDates(LocalDateTime start, LocalDateTime end) {
        BigDecimal deposits = transactionRepository.getDepositsBetweenDates(start, end);
        return deposits != null ? deposits : BigDecimal.ZERO;
    }

    // ============ SUMMARIES ============

    @Override
    public List<TransactionSummary> getAllSummaries() {
        log.debug("Getting all transaction summaries");
        List<Transaction> transactions = transactionRepository.findAll();
        return transactionMapper.toSummaryList(transactions);
    }

    // ============ HELPER METHODS ============

    private Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }
}
