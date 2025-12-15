package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.orderrefill.OrderRefillCreateRequest;
import com.antipanel.backend.dto.orderrefill.OrderRefillResponse;
import com.antipanel.backend.dto.orderrefill.OrderRefillSummary;
import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.OrderRefill;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.RefillStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OrderRefillMapper.
 */
class OrderRefillMapperTest {

    private final OrderRefillMapper mapper = Mappers.getMapper(OrderRefillMapper.class);

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        OrderRefill refill = createTestRefill();

        // When
        OrderRefillResponse response = mapper.toResponse(refill);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(refill.getId());
        assertThat(response.getOrderId()).isEqualTo(refill.getOrder().getId());
        assertThat(response.getProviderRefillId()).isEqualTo(refill.getProviderRefillId());
        assertThat(response.getQuantity()).isEqualTo(refill.getQuantity());
        assertThat(response.getStatus()).isEqualTo(refill.getStatus());
        assertThat(response.getCreatedAt()).isEqualTo(refill.getCreatedAt());
        assertThat(response.getCompletedAt()).isEqualTo(refill.getCompletedAt());
    }

    @Test
    void toEntity_ShouldMapBasicFields() {
        // Given
        OrderRefillCreateRequest request = OrderRefillCreateRequest.builder()
                .orderId(1L)
                .build();

        // When
        OrderRefill refill = mapper.toEntity(request);

        // Then
        assertThat(refill).isNotNull();
        assertThat(refill.getId()).isNull();
        // All other fields should be null/set by service
        assertThat(refill.getOrder()).isNull();
        assertThat(refill.getQuantity()).isNull();
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        OrderRefill refill = createTestRefill();

        // When
        OrderRefillSummary summary = mapper.toSummary(refill);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(refill.getId());
        assertThat(summary.getOrderId()).isEqualTo(refill.getOrder().getId());
        assertThat(summary.getQuantity()).isEqualTo(refill.getQuantity());
        assertThat(summary.getStatus()).isEqualTo(refill.getStatus());
        assertThat(summary.getCreatedAt()).isEqualTo(refill.getCreatedAt());
    }

    @Test
    void toResponseList_ShouldMapAllRefills() {
        // Given
        List<OrderRefill> refills = List.of(
                createTestRefill(),
                createTestRefill()
        );
        refills.get(1).setId(2L);
        refills.get(1).setStatus(RefillStatus.COMPLETED);

        // When
        List<OrderRefillResponse> responses = mapper.toResponseList(refills);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
        assertThat(responses.get(0).getStatus()).isEqualTo(RefillStatus.PROCESSING);
        assertThat(responses.get(1).getStatus()).isEqualTo(RefillStatus.COMPLETED);
    }

    @Test
    void toSummaryList_ShouldMapAllRefills() {
        // Given
        List<OrderRefill> refills = List.of(createTestRefill());

        // When
        List<OrderRefillSummary> summaries = mapper.toSummaryList(refills);

        // Then
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).getId()).isEqualTo(1L);
    }

    private OrderRefill createTestRefill() {
        Order order = new Order();
        order.setId(100L);
        order.setServiceName("Instagram Followers");
        order.setQuantity(1000);
        order.setRemains(0);
        order.setStatus(OrderStatus.COMPLETED);
        order.setTotalCharge(BigDecimal.valueOf(2.50));

        OrderRefill refill = new OrderRefill();
        refill.setId(1L);
        refill.setOrder(order);
        refill.setProviderRefillId("REFILL-123");
        refill.setQuantity(1000);
        refill.setStatus(RefillStatus.PROCESSING);
        refill.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        refill.setCompletedAt(null);
        return refill;
    }
}
