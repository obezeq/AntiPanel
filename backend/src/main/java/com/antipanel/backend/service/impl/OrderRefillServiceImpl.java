package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillCreateRequest;
import com.antipanel.backend.dto.orderrefill.OrderRefillResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillSummary;
import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.OrderRefill;
import com.antipanel.backend.entity.enums.RefillStatus;
import com.antipanel.backend.exception.BadRequestException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.OrderRefillMapper;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.repository.OrderRefillRepository;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.service.OrderRefillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of OrderRefillService.
 * Handles refill request creation, status management, and queries.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderRefillServiceImpl implements OrderRefillService {

    private final OrderRefillRepository orderRefillRepository;
    private final OrderRepository orderRepository;
    private final OrderRefillMapper orderRefillMapper;
    private final PageMapper pageMapper;

    // ============ CREATE OPERATIONS ============

    @Override
    @Transactional
    public OrderRefillResponse create(Long userId, OrderRefillCreateRequest request) {
        log.debug("Creating refill for order ID: {} by user ID: {}", request.getOrderId(), userId);

        // Validate order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        // Validate order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to user");
        }

        // Validate order can be refilled
        if (!order.canRequestRefill()) {
            throw new BadRequestException("Order is not eligible for refill");
        }

        // Check for existing pending refill
        if (orderRefillRepository.hasPendingRefill(order.getId())) {
            throw new BadRequestException("Order already has a pending refill request");
        }

        // Create refill request
        OrderRefill refill = OrderRefill.builder()
                .order(order)
                .quantity(order.getQuantity())
                .status(RefillStatus.PENDING)
                .build();

        OrderRefill saved = orderRefillRepository.save(refill);
        log.info("Created refill ID: {} for order ID: {}", saved.getId(), order.getId());
        return orderRefillMapper.toResponse(saved);
    }

    // ============ READ OPERATIONS ============

    @Override
    public OrderRefillResponse getById(Long id) {
        log.debug("Getting refill by ID: {}", id);
        OrderRefill refill = findRefillById(id);
        return orderRefillMapper.toResponse(refill);
    }

    @Override
    public OrderRefillResponse getByProviderRefillId(String providerRefillId) {
        log.debug("Getting refill by provider refill ID: {}", providerRefillId);
        OrderRefill refill = orderRefillRepository.findByProviderRefillId(providerRefillId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderRefill", "providerRefillId", providerRefillId));
        return orderRefillMapper.toResponse(refill);
    }

    // ============ BY ORDER ============

    @Override
    public List<OrderRefillResponse> getByOrder(Long orderId) {
        log.debug("Getting refills for order ID: {}", orderId);
        List<OrderRefill> refills = orderRefillRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        return orderRefillMapper.toResponseList(refills);
    }

    @Override
    public boolean hasPendingRefill(Long orderId) {
        return orderRefillRepository.hasPendingRefill(orderId);
    }

    @Override
    public long countByOrder(Long orderId) {
        return orderRefillRepository.countByOrderId(orderId);
    }

    // ============ BY STATUS ============

    @Override
    public List<OrderRefillResponse> getByStatus(RefillStatus status) {
        log.debug("Getting refills by status: {}", status);
        List<OrderRefill> refills = orderRefillRepository.findByStatusOrderByCreatedAtDesc(status);
        return orderRefillMapper.toResponseList(refills);
    }

    @Override
    public PageResponse<OrderRefillResponse> getByStatusPaginated(RefillStatus status, Pageable pageable) {
        log.debug("Getting paginated refills by status: {}", status);
        Page<OrderRefill> page = orderRefillRepository.findByStatus(status, pageable);
        List<OrderRefillResponse> content = orderRefillMapper.toResponseList(page.getContent());
        return pageMapper.toPageResponse(page, content);
    }

    @Override
    public List<OrderRefillResponse> getPendingRefills() {
        log.debug("Getting pending refills");
        List<OrderRefill> refills = orderRefillRepository.findPendingRefills();
        return orderRefillMapper.toResponseList(refills);
    }

    // ============ BY USER ============

    @Override
    public List<OrderRefillResponse> getByUser(Long userId) {
        log.debug("Getting refills for user ID: {}", userId);
        List<OrderRefill> refills = orderRefillRepository.findByUserId(userId);
        return orderRefillMapper.toResponseList(refills);
    }

    @Override
    public PageResponse<OrderRefillResponse> getByUserPaginated(Long userId, Pageable pageable) {
        log.debug("Getting paginated refills for user ID: {}", userId);
        Page<OrderRefill> page = orderRefillRepository.findByOrderUserId(userId, pageable);
        List<OrderRefillResponse> content = orderRefillMapper.toResponseList(page.getContent());
        return pageMapper.toPageResponse(page, content);
    }

    // ============ TIME-BASED QUERIES ============

    @Override
    public List<OrderRefillResponse> getRefillsBetweenDates(LocalDateTime start, LocalDateTime end) {
        log.debug("Getting refills between {} and {}", start, end);
        List<OrderRefill> refills = orderRefillRepository.findRefillsBetweenDates(start, end);
        return orderRefillMapper.toResponseList(refills);
    }

    // ============ STATUS OPERATIONS ============

    @Override
    @Transactional
    public OrderRefillResponse updateStatus(Long id, RefillStatus status) {
        log.debug("Updating refill ID: {} status to: {}", id, status);
        OrderRefill refill = findRefillById(id);

        if (refill.getStatus().isFinal()) {
            throw new BadRequestException("Cannot update status of refill in final state");
        }

        refill.setStatus(status);

        if (status == RefillStatus.COMPLETED) {
            refill.setCompletedAt(LocalDateTime.now());
        }

        OrderRefill saved = orderRefillRepository.save(refill);
        log.info("Updated refill ID: {} status to: {}", id, status);
        return orderRefillMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderRefillResponse markAsProcessing(Long id, String providerRefillId) {
        log.debug("Marking refill ID: {} as processing with provider refill ID: {}", id, providerRefillId);
        OrderRefill refill = findRefillById(id);

        if (refill.getStatus() != RefillStatus.PENDING) {
            throw new BadRequestException("Refill must be in PENDING status to mark as processing");
        }

        refill.setStatus(RefillStatus.PROCESSING);
        refill.setProviderRefillId(providerRefillId);

        OrderRefill saved = orderRefillRepository.save(refill);
        log.info("Marked refill ID: {} as processing", id);
        return orderRefillMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderRefillResponse completeRefill(Long id) {
        log.debug("Completing refill ID: {}", id);
        OrderRefill refill = findRefillById(id);

        if (refill.getStatus().isFinal()) {
            throw new BadRequestException("Refill is already in final state");
        }

        refill.setStatus(RefillStatus.COMPLETED);
        refill.setCompletedAt(LocalDateTime.now());

        OrderRefill saved = orderRefillRepository.save(refill);
        log.info("Completed refill ID: {}", id);
        return orderRefillMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderRefillResponse cancelRefill(Long id) {
        log.debug("Cancelling refill ID: {}", id);
        OrderRefill refill = findRefillById(id);

        if (refill.getStatus().isFinal()) {
            throw new BadRequestException("Cannot cancel refill in final state");
        }

        refill.setStatus(RefillStatus.CANCELLED);
        OrderRefill saved = orderRefillRepository.save(refill);
        log.info("Cancelled refill ID: {}", id);
        return orderRefillMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderRefillResponse rejectRefill(Long id) {
        log.debug("Rejecting refill ID: {}", id);
        OrderRefill refill = findRefillById(id);

        if (refill.getStatus().isFinal()) {
            throw new BadRequestException("Cannot reject refill in final state");
        }

        refill.setStatus(RefillStatus.REJECTED);
        OrderRefill saved = orderRefillRepository.save(refill);
        log.info("Rejected refill ID: {}", id);
        return orderRefillMapper.toResponse(saved);
    }

    // ============ STATISTICS ============

    @Override
    public long countByStatus(RefillStatus status) {
        return orderRefillRepository.countByStatus(status);
    }

    // ============ SUMMARIES ============

    @Override
    public List<OrderRefillSummary> getAllSummaries() {
        log.debug("Getting all refill summaries");
        List<OrderRefill> refills = orderRefillRepository.findAll();
        return orderRefillMapper.toSummaryList(refills);
    }

    // ============ HELPER METHODS ============

    private OrderRefill findRefillById(Long id) {
        return orderRefillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrderRefill", "id", id));
    }
}
