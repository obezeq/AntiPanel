package com.antipanel.backend.mapper;

import com.antipanel.backend.dto.order.OrderCreateRequest;
import com.antipanel.backend.dto.order.OrderDetailResponse;
import com.antipanel.backend.dto.order.OrderResponse;
import com.antipanel.backend.dto.order.OrderSummary;
import com.antipanel.backend.entity.Category;
import com.antipanel.backend.entity.Order;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.entity.ProviderService;
import com.antipanel.backend.entity.Service;
import com.antipanel.backend.entity.ServiceType;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.OrderStatus;
import com.antipanel.backend.entity.enums.ServiceQuality;
import com.antipanel.backend.entity.enums.ServiceSpeed;
import com.antipanel.backend.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OrderMapper.
 */
@SpringBootTest(classes = {
        OrderMapperImpl.class,
        UserMapperImpl.class,
        ServiceMapperImpl.class,
        CategoryMapperImpl.class,
        ServiceTypeMapperImpl.class,
        ProviderServiceMapperImpl.class,
        ProviderMapperImpl.class
})
class OrderMapperTest {

    @Autowired
    private OrderMapper mapper;

    @Test
    void toResponse_ShouldMapAllFields() {
        // Given
        Order order = createTestOrder();

        // When
        OrderResponse response = mapper.toResponse(order);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(order.getId());
        assertThat(response.getServiceId()).isEqualTo(order.getService().getId());
        assertThat(response.getServiceName()).isEqualTo(order.getServiceName());
        assertThat(response.getTarget()).isEqualTo(order.getTarget());
        assertThat(response.getQuantity()).isEqualTo(order.getQuantity());
        assertThat(response.getRemains()).isEqualTo(order.getRemains());
        assertThat(response.getStatus()).isEqualTo(order.getStatus());
        assertThat(response.getTotalCharge()).isEqualByComparingTo(order.getTotalCharge());
        assertThat(response.getIsRefillable()).isEqualTo(order.getIsRefillable());
        assertThat(response.getRefillDays()).isEqualTo(order.getRefillDays());
        // Computed fields
        assertThat(response.getProgress()).isEqualTo(order.getProgress());
        assertThat(response.getCanRequestRefill()).isEqualTo(order.canRequestRefill());
        // User nested
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(order.getUser().getId());
    }

    @Test
    void toDetailResponse_ShouldMapNestedEntities() {
        // Given
        Order order = createTestOrder();

        // When
        OrderDetailResponse response = mapper.toDetailResponse(order);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(order.getId());
        assertThat(response.getTarget()).isEqualTo(order.getTarget());
        // Service nested
        assertThat(response.getService()).isNotNull();
        assertThat(response.getService().getId()).isEqualTo(order.getService().getId());
        // Computed fields
        assertThat(response.getProgress()).isEqualTo(order.getProgress());
        assertThat(response.getCanRequestRefill()).isEqualTo(order.canRequestRefill());
    }

    @Test
    void toEntity_ShouldMapBasicFields() {
        // Given
        OrderCreateRequest request = OrderCreateRequest.builder()
                .serviceId(1)
                .target("https://instagram.com/test")
                .quantity(1000)
                .build();

        // When
        Order order = mapper.toEntity(request);

        // Then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isNull();
        assertThat(order.getTarget()).isEqualTo(request.getTarget());
        assertThat(order.getQuantity()).isEqualTo(request.getQuantity());
        // All other fields should be null/set by service
        assertThat(order.getUser()).isNull();
        assertThat(order.getService()).isNull();
    }

    @Test
    void toSummary_ShouldMapEssentialFieldsOnly() {
        // Given
        Order order = createTestOrder();

        // When
        OrderSummary summary = mapper.toSummary(order);

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.getId()).isEqualTo(order.getId());
        assertThat(summary.getServiceName()).isEqualTo(order.getServiceName());
        assertThat(summary.getQuantity()).isEqualTo(order.getQuantity());
        assertThat(summary.getRemains()).isEqualTo(order.getRemains());
        assertThat(summary.getStatus()).isEqualTo(order.getStatus());
        assertThat(summary.getTotalCharge()).isEqualByComparingTo(order.getTotalCharge());
    }

    @Test
    void toResponseList_ShouldMapAllOrders() {
        // Given
        List<Order> orders = List.of(
                createTestOrder(),
                createTestOrder()
        );
        orders.get(1).setId(2L);
        orders.get(1).setTarget("https://instagram.com/test2");

        // When
        List<OrderResponse> responses = mapper.toResponseList(orders);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
    }

    private Order createTestOrder() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(UserRole.USER);
        user.setBalance(BigDecimal.valueOf(100));

        Category category = new Category();
        category.setId(1);
        category.setName("Instagram");
        category.setSlug("instagram");

        ServiceType serviceType = new ServiceType();
        serviceType.setId(1);
        serviceType.setName("Followers");
        serviceType.setSlug("followers");

        Provider provider = new Provider();
        provider.setId(1);
        provider.setName("SMM Panel Pro");

        ProviderService providerService = new ProviderService();
        providerService.setId(1);
        providerService.setProvider(provider);
        providerService.setProviderServiceId("12345");
        providerService.setName("IG Followers HQ");
        providerService.setCostPerK(BigDecimal.valueOf(1.50));

        Service service = new Service();
        service.setId(1);
        service.setCategory(category);
        service.setServiceType(serviceType);
        service.setProviderService(providerService);
        service.setName("Instagram Followers");
        service.setQuality(ServiceQuality.HIGH);
        service.setSpeed(ServiceSpeed.FAST);
        service.setMinQuantity(100);
        service.setMaxQuantity(10000);
        service.setPricePerK(BigDecimal.valueOf(2.50));
        service.setRefillDays(30);

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setService(service);
        order.setServiceName("Instagram Followers");
        order.setProviderService(providerService);
        order.setTarget("https://instagram.com/test");
        order.setQuantity(1000);
        order.setRemains(500);
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setPricePerK(BigDecimal.valueOf(2.50));
        order.setCostPerK(BigDecimal.valueOf(1.50));
        order.setTotalCharge(BigDecimal.valueOf(2.50));
        order.setTotalCost(BigDecimal.valueOf(1.50));
        order.setProfit(BigDecimal.valueOf(1.00));
        order.setIsRefillable(true);
        order.setRefillDays(30);
        order.setRefillDeadline(LocalDateTime.now().plusDays(30));
        order.setCreatedAt(LocalDateTime.now().minusHours(1));
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }
}
