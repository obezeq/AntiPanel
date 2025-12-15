package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorCreateRequest;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorResponse;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorSummary;
import com.antipanel.backend.dto.paymentprocessor.PaymentProcessorUpdateRequest;
import com.antipanel.backend.entity.PaymentProcessor;
import com.antipanel.backend.exception.ConflictException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.PaymentProcessorMapper;
import com.antipanel.backend.repository.PaymentProcessorRepository;
import com.antipanel.backend.service.PaymentProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of PaymentProcessorService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentProcessorServiceImpl implements PaymentProcessorService {

    private final PaymentProcessorRepository paymentProcessorRepository;
    private final PaymentProcessorMapper paymentProcessorMapper;

    // ============ CRUD OPERATIONS ============

    @Override
    @Transactional
    public PaymentProcessorResponse create(PaymentProcessorCreateRequest request) {
        log.debug("Creating payment processor with code: {}", request.getCode());

        if (paymentProcessorRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Payment processor code already exists: " + request.getCode());
        }

        PaymentProcessor processor = paymentProcessorMapper.toEntity(request);

        PaymentProcessor saved = paymentProcessorRepository.save(processor);
        log.info("Created payment processor with ID: {}", saved.getId());

        return paymentProcessorMapper.toResponse(saved);
    }

    @Override
    public PaymentProcessorResponse getById(Integer id) {
        log.debug("Getting payment processor by ID: {}", id);
        PaymentProcessor processor = findPaymentProcessorById(id);
        return paymentProcessorMapper.toResponse(processor);
    }

    @Override
    public PaymentProcessorResponse getByCode(String code) {
        log.debug("Getting payment processor by code: {}", code);
        PaymentProcessor processor = paymentProcessorRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProcessor", "code", code));
        return paymentProcessorMapper.toResponse(processor);
    }

    @Override
    @Transactional
    public PaymentProcessorResponse update(Integer id, PaymentProcessorUpdateRequest request) {
        log.debug("Updating payment processor with ID: {}", id);

        PaymentProcessor processor = findPaymentProcessorById(id);
        paymentProcessorMapper.updateEntityFromDto(request, processor);

        PaymentProcessor saved = paymentProcessorRepository.save(processor);
        log.info("Updated payment processor with ID: {}", saved.getId());

        return paymentProcessorMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.debug("Deleting payment processor with ID: {}", id);
        PaymentProcessor processor = findPaymentProcessorById(id);
        paymentProcessorRepository.delete(processor);
        log.info("Deleted payment processor with ID: {}", id);
    }

    // ============ LISTING ============

    @Override
    public List<PaymentProcessorResponse> getAll() {
        log.debug("Getting all payment processors");
        List<PaymentProcessor> processors = paymentProcessorRepository.findAll();
        return paymentProcessorMapper.toResponseList(processors);
    }

    @Override
    public List<PaymentProcessorResponse> getAllActive() {
        log.debug("Getting all active payment processors");
        List<PaymentProcessor> processors = paymentProcessorRepository.findAllActiveProcessors();
        return paymentProcessorMapper.toResponseList(processors);
    }

    @Override
    public List<PaymentProcessorSummary> getAllSummaries() {
        log.debug("Getting all payment processor summaries");
        List<PaymentProcessor> processors = paymentProcessorRepository.findAllActiveProcessors();
        return paymentProcessorMapper.toSummaryList(processors);
    }

    @Override
    public List<PaymentProcessorResponse> getProcessorsForAmount(BigDecimal amount) {
        log.debug("Getting payment processors for amount: {}", amount);
        List<PaymentProcessor> processors = paymentProcessorRepository.findProcessorsForAmount(amount);
        return paymentProcessorMapper.toResponseList(processors);
    }

    // ============ STATUS OPERATIONS ============

    @Override
    @Transactional
    public PaymentProcessorResponse toggleActive(Integer id) {
        log.debug("Toggling active status for payment processor ID: {}", id);
        PaymentProcessor processor = findPaymentProcessorById(id);
        processor.setIsActive(!processor.getIsActive());
        PaymentProcessor saved = paymentProcessorRepository.save(processor);
        log.info("Toggled active status for payment processor ID: {} to {}", id, saved.getIsActive());
        return paymentProcessorMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentProcessorResponse activate(Integer id) {
        log.debug("Activating payment processor ID: {}", id);
        PaymentProcessor processor = findPaymentProcessorById(id);
        processor.setIsActive(true);
        PaymentProcessor saved = paymentProcessorRepository.save(processor);
        log.info("Activated payment processor ID: {}", id);
        return paymentProcessorMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentProcessorResponse deactivate(Integer id) {
        log.debug("Deactivating payment processor ID: {}", id);
        PaymentProcessor processor = findPaymentProcessorById(id);
        processor.setIsActive(false);
        PaymentProcessor saved = paymentProcessorRepository.save(processor);
        log.info("Deactivated payment processor ID: {}", id);
        return paymentProcessorMapper.toResponse(saved);
    }

    // ============ STATISTICS ============

    @Override
    public long countActive() {
        log.debug("Counting active payment processors");
        return paymentProcessorRepository.countActiveProcessors();
    }

    // ============ VALIDATION ============

    @Override
    public boolean existsByCode(String code) {
        return paymentProcessorRepository.existsByCode(code);
    }

    // ============ HELPER METHODS ============

    private PaymentProcessor findPaymentProcessorById(Integer id) {
        return paymentProcessorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentProcessor", "id", id));
    }
}
