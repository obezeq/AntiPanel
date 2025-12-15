package com.antipanel.backend.service.impl;

import com.antipanel.backend.dto.common.PageResponse;
import com.antipanel.backend.dto.order.OrderCreateRequest;
import com.antipanel.backend.dto.order.OrderDetailResponse;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.dto.order.OrderSummary;
import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.ProviderService;
import com.antipanel.backend.entity.Service;
import com.antipanel.backend.entity.Transaction;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.TransactionType;
import com.antipanel.backend.exception.BadRequestException;
import com.antipanel.backend.exception.InsufficientBalanceException;
import com.antipanel.backend.exception.ResourceNotFoundException;
import com.antipanel.backend.mapper.OrderMapper;
import com.antipanel.backend.mapper.PageMapper;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.repository.ServiceRepository;
import com.antipanel.backend.repository.TransactionRepository;
import com.antipanel.backend.repository.UserRepository;
import com.antipanel.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of OrderService.
 * Handles core business logic for order creation, status management, and statistics.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final TransactionRepository transactionRepository;
    private final OrderMapper orderMapper;
    private final PageMapper pageMapper;

    // ============ CREATE OPERATIONS ============

    @Override
    @Transactional
    public OrderResponse create(Long userId, OrderCreateRequest request) {
        log.debug("Creating order for user ID: {} with service ID: {}", userId, request.getServiceId());

        // Validate user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getIsBanned()) {
            throw new BadRequestException("User is banned and cannot place orders");
        }

        // Validate service
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        if (!service.getIsActive()) {
            throw new BadRequestException("Service is not active");
        }

        // Validate quantity
        if (!service.isQuantityValid(request.getQuantity())) {
            throw new BadRequestException(String.format(
                    "Quantity must be between %d and %d",
                    service.getMinQuantity(), service.getMaxQuantity()));
        }

        // Calculate prices
        ProviderService providerService = service.getProviderService();
        BigDecimal pricePerK = service.getPricePerK();
        BigDecimal costPerK = providerService.getCostPerK();

        BigDecimal totalCharge = calculateTotalAmount(pricePerK, request.getQuantity());
        BigDecimal totalCost = calculateTotalAmount(costPerK, request.getQuantity());
        BigDecimal profit = totalCharge.subtract(totalCost);

        // Validate user balance
        if (user.getBalance().compareTo(totalCharge) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Required: " + totalCharge);
        }

        // Create order
        Order order = Order.builder()
                .user(user)
                .service(service)
                .serviceName(service.getName())
                .providerService(providerService)
                .target(request.getTarget())
                .quantity(request.getQuantity())
                .remains(request.getQuantity())
                .status(OrderStatus.PENDING)
                .pricePerK(pricePerK)
                .costPerK(costPerK)
                .totalCharge(totalCharge)
                .totalCost(totalCost)
                .profit(profit)
                .isRefillable(service.getRefillDays() > 0)
                .refillDays(service.getRefillDays())
                .build();

        Order saved = orderRepository.save(order);

        // Deduct balance and create transaction
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(totalCharge);
        user.setBalance(balanceAfter);
        userRepository.save(user);

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(TransactionType.ORDER)
                .amount(totalCharge.negate())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceType("ORDER")
                .referenceId(saved.getId())
                .description("Order #" + saved.getId() + " - " + service.getName())
                .build();
        transactionRepository.save(transaction);

        log.info("Created order ID: {} for user ID: {}", saved.getId(), userId);
        return orderMapper.toResponse(saved);
    }

    // ============ READ OPERATIONS ============

    @Override
    public OrderResponse getById(Long id) {
        log.debug("Getting order by ID: {}", id);
        Order order = findOrderById(id);
        return orderMapper.toResponse(order);
    }

    @Override
    public OrderDetailResponse getDetailById(Long id) {
        log.debug("Getting order detail by ID: {}", id);
        Order order = findOrderById(id);
        return orderMapper.toDetailResponse(order);
    }

    @Override
    public OrderResponse getByProviderOrderId(String providerOrderId) {
        log.debug("Getting order by provider order ID: {}", providerOrderId);
        Order order = orderRepository.findByProviderOrderId(providerOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "providerOrderId", providerOrderId));
        return orderMapper.toResponse(order);
    }

    // ============ USER ORDER QUERIES ============

    @Override
    public List<OrderResponse> getByUser(Long userId) {
        log.debug("Getting orders for user ID: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public PageResponse<OrderResponse> getByUserPaginated(Long userId, Pageable pageable) {
        log.debug("Getting paginated orders for user ID: {}", userId);
        Page<Order> page = orderRepository.findByUserId(userId, pageable);
        List<OrderResponse> content = orderMapper.toResponseList(page.getContent());
        return pageMapper.toPageResponse(page, content);
    }

    @Override
    public List<OrderResponse> getByUserAndStatus(Long userId, OrderStatus status) {
        log.debug("Getting orders for user ID: {} with status: {}", userId, status);
        List<Order> orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public List<OrderResponse> getActiveByUser(Long userId) {
        log.debug("Getting active orders for user ID: {}", userId);
        List<Order> orders = orderRepository.findActiveOrdersByUser(userId);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public List<OrderResponse> getRefillableByUser(Long userId) {
        log.debug("Getting refillable orders for user ID: {}", userId);
        List<Order> orders = orderRepository.findRefillableOrdersByUser(userId, LocalDateTime.now());
        return orderMapper.toResponseList(orders);
    }

    // ============ ADMIN ORDER QUERIES ============

    @Override
    public List<OrderResponse> getByStatus(OrderStatus status) {
        log.debug("Getting orders by status: {}", status);
        List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public PageResponse<OrderResponse> getByStatusPaginated(OrderStatus status, Pageable pageable) {
        log.debug("Getting paginated orders by status: {}", status);
        Page<Order> page = orderRepository.findByStatus(status, pageable);
        List<OrderResponse> content = orderMapper.toResponseList(page.getContent());
        return pageMapper.toPageResponse(page, content);
    }

    @Override
    public List<OrderResponse> getByService(Integer serviceId) {
        log.debug("Getting orders for service ID: {}", serviceId);
        List<Order> orders = orderRepository.findByServiceIdOrderByCreatedAtDesc(serviceId);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public List<OrderResponse> getByProviderService(Integer providerServiceId) {
        log.debug("Getting orders for provider service ID: {}", providerServiceId);
        List<Order> orders = orderRepository.findByProviderServiceIdOrderByCreatedAtDesc(providerServiceId);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public List<OrderResponse> getOrdersNeedingUpdate(LocalDateTime threshold) {
        log.debug("Getting orders needing update before: {}", threshold);
        List<Order> orders = orderRepository.findOrdersNeedingUpdate(threshold);
        return orderMapper.toResponseList(orders);
    }

    // ============ TIME-BASED QUERIES ============

    @Override
    public List<OrderResponse> getOrdersBetweenDates(LocalDateTime start, LocalDateTime end) {
        log.debug("Getting orders between {} and {}", start, end);
        List<Order> orders = orderRepository.findOrdersBetweenDates(start, end);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public List<OrderResponse> getCompletedOrdersBetweenDates(LocalDateTime start, LocalDateTime end) {
        log.debug("Getting completed orders between {} and {}", start, end);
        List<Order> orders = orderRepository.findCompletedOrdersBetweenDates(start, end);
        return orderMapper.toResponseList(orders);
    }

    // ============ REFILL MANAGEMENT ============

    @Override
    public List<OrderResponse> getOrdersWithRefillExpiringSoon(LocalDateTime deadline) {
        log.debug("Getting orders with refill expiring before: {}", deadline);
        List<Order> orders = orderRepository.findOrdersWithRefillExpiringSoon(LocalDateTime.now(), deadline);
        return orderMapper.toResponseList(orders);
    }

    @Override
    public List<OrderResponse> getOrdersWithExpiredRefill() {
        log.debug("Getting orders with expired refill");
        List<Order> orders = orderRepository.findOrdersWithExpiredRefill(LocalDateTime.now());
        return orderMapper.toResponseList(orders);
    }

    // ============ STATUS OPERATIONS ============

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        log.debug("Updating order ID: {} status to: {}", id, status);
        Order order = findOrderById(id);

        if (order.isFinal()) {
            throw new BadRequestException("Cannot update status of order in final state");
        }

        order.setStatus(status);

        if (status == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
            order.setRemains(0);
            if (order.getIsRefillable() && order.getRefillDays() > 0) {
                order.setRefillDeadline(LocalDateTime.now().plusDays(order.getRefillDays()));
            }
        }

        Order saved = orderRepository.save(order);
        log.info("Updated order ID: {} status to: {}", id, status);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse markAsProcessing(Long id, String providerOrderId) {
        log.debug("Marking order ID: {} as processing with provider order ID: {}", id, providerOrderId);
        Order order = findOrderById(id);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order must be in PENDING status to mark as processing");
        }

        order.setStatus(OrderStatus.PROCESSING);
        order.setProviderOrderId(providerOrderId);

        Order saved = orderRepository.save(order);
        log.info("Marked order ID: {} as processing", id);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse updateProgress(Long id, Integer startCount, Integer remains) {
        log.debug("Updating order ID: {} progress - startCount: {}, remains: {}", id, startCount, remains);
        Order order = findOrderById(id);

        if (order.isFinal()) {
            throw new BadRequestException("Cannot update progress of order in final state");
        }

        if (startCount != null) {
            order.setStartCount(startCount);
        }
        if (remains != null) {
            order.setRemains(remains);
        }

        // Auto-complete if remains reaches 0
        if (order.getRemains() != null && order.getRemains() == 0 && order.getStatus() == OrderStatus.IN_PROGRESS) {
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(LocalDateTime.now());
            if (order.getIsRefillable() && order.getRefillDays() > 0) {
                order.setRefillDeadline(LocalDateTime.now().plusDays(order.getRefillDays()));
            }
        }

        Order saved = orderRepository.save(order);
        log.info("Updated order ID: {} progress", id);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse completeOrder(Long id) {
        log.debug("Completing order ID: {}", id);
        return updateStatus(id, OrderStatus.COMPLETED);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        log.debug("Cancelling order ID: {}", id);
        Order order = findOrderById(id);

        if (order.isFinal()) {
            throw new BadRequestException("Cannot cancel order in final state");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Refund user balance
        User user = order.getUser();
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(order.getTotalCharge());
        user.setBalance(balanceAfter);
        userRepository.save(user);

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(TransactionType.REFUND)
                .amount(order.getTotalCharge())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceType("ORDER")
                .referenceId(order.getId())
                .description("Order #" + order.getId() + " cancelled - refund")
                .build();
        transactionRepository.save(transaction);

        Order saved = orderRepository.save(order);
        log.info("Cancelled order ID: {} and refunded balance", id);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse refundOrder(Long id) {
        log.debug("Refunding order ID: {}", id);
        Order order = findOrderById(id);

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException("Only completed orders can be refunded");
        }

        order.setStatus(OrderStatus.REFUNDED);

        // Refund user balance
        User user = order.getUser();
        BigDecimal balanceBefore = user.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(order.getTotalCharge());
        user.setBalance(balanceAfter);
        userRepository.save(user);

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(TransactionType.REFUND)
                .amount(order.getTotalCharge())
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceType("ORDER")
                .referenceId(order.getId())
                .description("Order #" + order.getId() + " refunded")
                .build();
        transactionRepository.save(transaction);

        Order saved = orderRepository.save(order);
        log.info("Refunded order ID: {}", id);
        return orderMapper.toResponse(saved);
    }

    // ============ STATISTICS ============

    @Override
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public long countByUser(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    @Override
    public long countByUserAndStatus(Long userId, OrderStatus status) {
        return orderRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalCost() {
        BigDecimal cost = orderRepository.getTotalCost();
        return cost != null ? cost : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalProfit() {
        BigDecimal profit = orderRepository.getTotalProfit();
        return profit != null ? profit : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getRevenueBetweenDates(LocalDateTime start, LocalDateTime end) {
        BigDecimal revenue = orderRepository.getRevenueBetweenDates(start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getProfitBetweenDates(LocalDateTime start, LocalDateTime end) {
        BigDecimal profit = orderRepository.getProfitBetweenDates(start, end);
        return profit != null ? profit : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getAverageOrderValue() {
        BigDecimal avg = orderRepository.getAverageOrderValue();
        return avg != null ? avg : BigDecimal.ZERO;
    }

    // ============ SUMMARIES ============

    @Override
    public List<OrderSummary> getAllSummaries() {
        log.debug("Getting all order summaries");
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toSummaryList(orders);
    }

    // ============ HELPER METHODS ============

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    private BigDecimal calculateTotalAmount(BigDecimal pricePerK, Integer quantity) {
        return pricePerK
                .multiply(BigDecimal.valueOf(quantity))
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
    }
}
