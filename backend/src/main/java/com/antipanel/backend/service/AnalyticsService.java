package com.antipanel.backend.service;

import com.antipanel.backend.dto.analytics.AnalyticResponse;
import com.antipanel.backend.repository.OrderRepository;
import com.antipanel.backend.repository.TransactionRepository;
import com.antipanel.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for global analytics operations.
 * Provides aggregated statistics across all users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Get global analytics for all users.
     * Returns Money Spent, Orders Made, and Users Registered.
     *
     * @return List of analytics metrics
     */
    @Transactional(readOnly = true)
    public List<AnalyticResponse> getGlobalAnalytics() {
        log.debug("Calculating global analytics");

        List<AnalyticResponse> analytics = new ArrayList<>();

        // 1. Money Spent - Total revenue from completed orders
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        analytics.add(AnalyticResponse.builder()
                .title("Money Spent")
                .amount(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .build());

        // 2. Orders Made - Total count of all orders
        long totalOrders = orderRepository.count();
        analytics.add(AnalyticResponse.builder()
                .title("Orders Made")
                .amount(BigDecimal.valueOf(totalOrders))
                .build());

        // 3. Users Registered - Total count of all users
        long totalUsers = userRepository.count();
        analytics.add(AnalyticResponse.builder()
                .title("Users Registered")
                .amount(BigDecimal.valueOf(totalUsers))
                .build());

        log.debug("Global analytics calculated: Money Spent={}, Orders Made={}, Users Registered={}",
                analytics.get(0).getAmount(), totalOrders, totalUsers);

        return analytics;
    }
}
