package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.invoice.InvoiceCreateRequest;
import com.antipanel.backend.dto.invoice.InvoiceResponse;
import com.antipanel.backend.dto.invoice.InvoiceSummary;
import com.antipanel.backend.dto.paymento.PaymentoPaymentResponse;
import com.antipanel.backend.dto.paymento.PaymentoVerifyResponse;
import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.PaymentProcessor;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.exception.BadRequestException;
import com.antipanel.backend.exception.PaymentoApiException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.InvoiceMapper;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.repository.InvoiceRepository;
import com.antipanel.backend.repository.PaymentProcessorRepository;
import com.antipanel.backend.repository.TransactionRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.InvoiceService;
import com.antipanel.backend.service.payment.PaymentoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of InvoiceService.
 * Handles deposit invoice creation, payment processing, and statistics.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private static final String PAYMENTO_PROCESSOR_CODE = "paymento";

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final PaymentProcessorRepository paymentProcessorRepository;
    private final TransactionRepository transactionRepository;
    private final InvoiceMapper invoiceMapper;
    private final PageMapper pageMapper;
    private final PaymentoClient paymentoClient;

    // ============ CREATE OPERATIONS ============

    @Override
    @Transactional
    public InvoiceResponse create(Long userId, InvoiceCreateRequest request) {
        log.debug("Creating invoice for user ID: {} with processor ID: {}", userId, request.getProcessorId());

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getIsBanned()) {
            throw new BadRequestException("User is banned and cannot create invoices");
        }

        // Validate payment processor
        PaymentProcessor processor = paymentProcessorRepository.findById(request.getProcessorId())
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProcessor", "id", request.getProcessorId()));

        if (!processor.getIsActive()) {
            throw new BadRequestException("Payment processor is not active");
        }

        // Validate amount
        if (!processor.isAmountValid(request.getAmount())) {
            throw new BadRequestException(String.format(
                    "Amount must be between %s and %s",
                    processor.getMinAmount(),
                    processor.getMaxAmount() != null ? processor.getMaxAmount() : "unlimited"));
        }

        // Calculate fees
        BigDecimal fee = processor.calculateFee(request.getAmount());
        BigDecimal netAmount = request.getAmount().subtract(fee);

        // Create invoice
        Invoice invoice = Invoice.builder()
                .user(user)
                .processor(processor)
                .amount(request.getAmount())
                .fee(fee)
                .netAmount(netAmount)
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status(InvoiceStatus.PENDING)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created invoice ID: {} for user ID: {}", saved.getId(), userId);

        // Integrate with Paymento if processor is paymento
        if (PAYMENTO_PROCESSOR_CODE.equalsIgnoreCase(processor.getCode())) {
            saved = createPaymentoPayment(saved, processor);
        }

        return invoiceMapper.toResponse(saved);
    }

    /**
     * Creates a payment with Paymento and updates the invoice.
     *
     * @throws PaymentoApiException if Paymento API call fails (triggers transaction rollback)
     */
    private Invoice createPaymentoPayment(Invoice invoice, PaymentProcessor processor) {
        log.debug("Creating Paymento payment for invoice ID: {}", invoice.getId());

        PaymentoPaymentResponse paymentResponse = paymentoClient.createPayment(processor, invoice);

        // Update invoice with token and payment URL
        // Keep PENDING status - user hasn't paid yet
        invoice.setProcessorInvoiceId(paymentResponse.getToken());
        invoice.setPaymentUrl(paymentResponse.getPaymentUrl());

        Invoice updated = invoiceRepository.save(invoice);
        log.info("Paymento payment created for invoice ID: {} - Payment URL: {}",
                updated.getId(), updated.getPaymentUrl());

        return updated;
    }

    // ============ READ OPERATIONS ============

    @Override
    public InvoiceResponse getById(Long id) {
        log.debug("Getting invoice by ID: {}", id);
        Invoice invoice = findInvoiceById(id);
        return invoiceMapper.toResponse(invoice);
    }

    @Override
    public InvoiceResponse getByProcessorInvoiceId(String processorInvoiceId) {
        log.debug("Getting invoice by processor invoice ID: {}", processorInvoiceId);
        Invoice invoice = invoiceRepository.findByProcessorInvoiceId(processorInvoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "processorInvoiceId", processorInvoiceId));
        return invoiceMapper.toResponse(invoice);
    }

    // ============ USER QUERIES ============

    @Override
    public List<InvoiceResponse> getByUser(Long userId) {
        log.debug("Getting invoices for user ID: {}", userId);
        List<Invoice> invoices = invoiceRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return invoiceMapper.toResponseList(invoices);
    }

    @Override
    public PageResponse<InvoiceResponse> getByUserPaginated(Long userId, Pageable pageable) {
        log.debug("Getting paginated invoices for user ID: {}", userId);
        Page<Invoice> page = invoiceRepository.findByUserId(userId, pageable);
        List<InvoiceResponse> content = invoiceMapper.toResponseList(page.getContent());
        return pageMapper.toPageResponse(page, content);
    }

    @Override
    public List<InvoiceResponse> getByUserAndStatus(Long userId, InvoiceStatus status) {
        log.debug("Getting invoices for user ID: {} with status: {}", userId, status);
        List<Invoice> invoices = invoiceRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        return invoiceMapper.toResponseList(invoices);
    }

    @Override
    public List<InvoiceResponse> getPendingByUser(Long userId) {
        log.debug("Getting pending invoices for user ID: {}", userId);
        List<Invoice> invoices = invoiceRepository.findPendingInvoicesByUser(userId);
        return invoiceMapper.toResponseList(invoices);
    }

    // ============ ADMIN QUERIES ============

    @Override
    public List<InvoiceResponse> getByStatus(InvoiceStatus status) {
        log.debug("Getting invoices by status: {}", status);
        List<Invoice> invoices = invoiceRepository.findByStatusOrderByCreatedAtDesc(status);
        return invoiceMapper.toResponseList(invoices);
    }

    @Override
    public PageResponse<InvoiceResponse> getByStatusPaginated(InvoiceStatus status, Pageable pageable) {
        log.debug("Getting paginated invoices by status: {}", status);
        Page<Invoice> page = invoiceRepository.findByStatus(status, pageable);
        List<InvoiceResponse> content = invoiceMapper.toResponseList(page.getContent());
        return pageMapper.toPageResponse(page, content);
    }

    @Override
    public List<InvoiceResponse> getByProcessor(Integer processorId) {
        log.debug("Getting invoices for processor ID: {}", processorId);
        List<Invoice> invoices = invoiceRepository.findByProcessorIdOrderByCreatedAtDesc(processorId);
        return invoiceMapper.toResponseList(invoices);
    }

    // ============ TIME-BASED QUERIES ============

    @Override
    public List<InvoiceResponse> getInvoicesBetweenDates(LocalDateTime start, LocalDateTime end) {
        log.debug("Getting invoices between {} and {}", start, end);
        List<Invoice> invoices = invoiceRepository.findInvoicesBetweenDates(start, end);
        return invoiceMapper.toResponseList(invoices);
    }

    @Override
    public List<InvoiceResponse> getPaidInvoicesBetweenDates(LocalDateTime start, LocalDateTime end) {
        log.debug("Getting paid invoices between {} and {}", start, end);
        List<Invoice> invoices = invoiceRepository.findPaidInvoicesBetweenDates(start, end);
        return invoiceMapper.toResponseList(invoices);
    }

    @Override
    public List<InvoiceResponse> getExpiredPendingInvoices(LocalDateTime expiryTime) {
        log.debug("Getting expired pending invoices before: {}", expiryTime);
        List<Invoice> invoices = invoiceRepository.findExpiredPendingInvoices(expiryTime);
        return invoiceMapper.toResponseList(invoices);
    }

    // ============ STATUS OPERATIONS ============

    @Override
    @Transactional
    public InvoiceResponse updateStatus(Long id, InvoiceStatus status) {
        log.debug("Updating invoice ID: {} status to: {}", id, status);
        Invoice invoice = findInvoiceById(id);

        if (invoice.isFinal()) {
            throw new BadRequestException("Cannot update status of invoice in final state");
        }

        invoice.setStatus(status);

        if (status.isSuccessful()) {
            invoice.setPaidAt(LocalDateTime.now());
        }

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Updated invoice ID: {} status to: {}", id, status);
        return invoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse markAsProcessing(Long id, String processorInvoiceId, String paymentUrl) {
        log.debug("Marking invoice ID: {} as processing", id);
        Invoice invoice = findInvoiceById(id);

        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new BadRequestException("Invoice must be in PENDING status to mark as processing");
        }

        invoice.setStatus(InvoiceStatus.PROCESSING);
        invoice.setProcessorInvoiceId(processorInvoiceId);
        invoice.setPaymentUrl(paymentUrl);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Marked invoice ID: {} as processing", id);
        return invoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse completePayment(Long id) {
        log.debug("Completing payment for invoice ID: {}", id);

        // Use pessimistic lock to prevent race conditions (double credit)
        Invoice invoice = invoiceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (invoice.isFinal()) {
            // Already completed - return current state (idempotent)
            log.debug("Invoice {} already in final state: {}", id, invoice.getStatus());
            return invoiceMapper.toResponse(invoice);
        }

        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice.setPaidAt(LocalDateTime.now());

        // Add balance to user
        User user = invoice.getUser();
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(invoice.getNetAmount());
        user.setBalance(balanceAfter);
        userRepository.save(user);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(user)
                .type(TransactionType.DEPOSIT)
                .amount(invoice.getNetAmount())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceType("INVOICE")
                .referenceId(invoice.getId())
                .description("Deposit via " + invoice.getProcessor().getName())
                .build();
        transactionRepository.save(transaction);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Completed payment for invoice ID: {} - Added {} to user balance", id, invoice.getNetAmount());
        return invoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse checkPaymentStatus(Long invoiceId) {
        log.debug("Checking payment status for invoice ID: {}", invoiceId);
        Invoice invoice = findInvoiceById(invoiceId);

        // Already final - return current state (idempotency)
        if (invoice.isFinal()) {
            log.debug("Invoice {} already in final state: {}", invoiceId, invoice.getStatus());
            return invoiceMapper.toResponse(invoice);
        }

        // Must have token to verify with Paymento
        if (invoice.getProcessorInvoiceId() == null) {
            log.debug("Invoice {} has no token, skipping payment check", invoiceId);
            return invoiceMapper.toResponse(invoice);
        }

        // Only check PENDING or PROCESSING invoices (not already final)
        if (invoice.getStatus() != InvoiceStatus.PENDING &&
            invoice.getStatus() != InvoiceStatus.PROCESSING) {
            log.debug("Invoice {} in status {} is not eligible for payment check",
                invoiceId, invoice.getStatus());
            return invoiceMapper.toResponse(invoice);
        }

        // Verify with Paymento API
        PaymentProcessor processor = paymentProcessorRepository.findByCode(PAYMENTO_PROCESSOR_CODE)
            .orElseThrow(() -> new BadRequestException("Paymento processor not configured"));

        try {
            PaymentoVerifyResponse response = paymentoClient.verifyPayment(
                processor, invoice.getProcessorInvoiceId());

            if (response.isPaid()) {
                // Payment is complete - add balance and update status
                log.info("Payment verified as complete for invoice {}", invoiceId);
                return completePayment(invoiceId);
            }

            // Verify returned but not paid yet (pending)
            log.debug("Payment not yet complete for invoice {}", invoiceId);
        } catch (PaymentoApiException e) {
            // API error means payment not complete - this is expected for pending payments
            log.debug("Verify returned error for invoice {} (expected if pending): {}",
                invoiceId, e.getMessage());
        }

        // Still pending
        return invoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse cancelInvoice(Long id) {
        log.debug("Cancelling invoice ID: {}", id);
        Invoice invoice = findInvoiceById(id);

        if (invoice.isFinal()) {
            throw new BadRequestException("Cannot cancel invoice in final state");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Cancelled invoice ID: {}", id);
        return invoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InvoiceResponse expireInvoice(Long id) {
        log.debug("Expiring invoice ID: {}", id);
        Invoice invoice = findInvoiceById(id);

        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new BadRequestException("Only pending invoices can be expired");
        }

        invoice.setStatus(InvoiceStatus.EXPIRED);
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Expired invoice ID: {}", id);
        return invoiceMapper.toResponse(saved);
    }

    // ============ STATISTICS ============

    @Override
    public long countByStatus(InvoiceStatus status) {
        return invoiceRepository.countByStatus(status);
    }

    @Override
    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = invoiceRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getRevenueBetweenDates(LocalDateTime start, LocalDateTime end) {
        BigDecimal revenue = invoiceRepository.getRevenueBetweenDates(start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalRevenueByUser(Long userId) {
        BigDecimal revenue = invoiceRepository.getTotalRevenueByUser(userId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAverageInvoiceAmount() {
        BigDecimal avg = invoiceRepository.getAverageInvoiceAmount();
        return avg != null ? avg : BigDecimal.ZERO;
    }

    // ============ SUMMARIES ============

    @Override
    public List<InvoiceSummary> getAllSummaries() {
        log.debug("Getting all invoice summaries");
        List<Invoice> invoices = invoiceRepository.findAll();
        return invoiceMapper.toSummaryList(invoices);
    }

    // ============ HELPER METHODS ============

    private Invoice findInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }
}
