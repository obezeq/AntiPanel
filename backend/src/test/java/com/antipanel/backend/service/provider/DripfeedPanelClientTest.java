package com.antipanel.backend.service.provider;

import com.antipanel.backend.dto.provider.api.*;
import com.antipanel.backend.entity.Provider;
import com.antipanel.backend.exception.ProviderApiException;
import com.antipanel.backend.service.provider.impl.DripfeedPanelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for DripfeedPanelClient using WireMock to mock HTTP responses.
 * No real API calls are made - all responses are mocked.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableWireMock
@ActiveProfiles("test")
@DisplayName("DripfeedPanelClient Tests")
class DripfeedPanelClientTest {

    @InjectWireMock
    private WireMockServer wireMock;

    private DripfeedPanelClient client;
    private Provider testProvider;
    private ObjectMapper objectMapper;

    private static final String API_KEY = "test_api_key_12345";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        RestClient.Builder restClientBuilder = RestClient.builder();
        client = new DripfeedPanelClient(restClientBuilder, objectMapper);

        testProvider = Provider.builder()
                .id(1)
                .name("DripfeedPanel")
                .apiUrl(wireMock.baseUrl())
                .apiKey(API_KEY)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Get Services")
    class GetServicesTests {

        @Test
        @DisplayName("Should fetch services successfully")
        void shouldFetchServicesSuccessfully() {
            String mockResponse = """
                [
                    {"service": 13311, "name": "Instagram Followers [Real]", "type": "Default", "category": "Instagram", "rate": "1.00", "min": "10", "max": "300000", "refill": true, "cancel": true},
                    {"service": 15856, "name": "Instagram Likes [Real]", "type": "Default", "category": "Instagram", "rate": "0.31", "min": "10", "max": "300000", "refill": false, "cancel": true},
                    {"service": 16132, "name": "TikTok Followers [Real]", "type": "Default", "category": "TikTok", "rate": "1.69", "min": "10", "max": "100000", "refill": true, "cancel": false}
                ]
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=services"))
                    .withRequestBody(containing("key=" + API_KEY))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            List<DripfeedServiceDto> services = client.getServices(testProvider);

            assertThat(services).hasSize(3);
            assertThat(services.get(0).getServiceId()).isEqualTo(13311);
            assertThat(services.get(0).getName()).isEqualTo("Instagram Followers [Real]");
            assertThat(services.get(0).getRateAsDecimal()).isEqualByComparingTo(new BigDecimal("1.00"));
            assertThat(services.get(0).getRefill()).isTrue();

            assertThat(services.get(1).getServiceId()).isEqualTo(15856);
            assertThat(services.get(1).getRateAsDecimal()).isEqualByComparingTo(new BigDecimal("0.31"));

            assertThat(services.get(2).getServiceId()).isEqualTo(16132);
            assertThat(services.get(2).getCategory()).isEqualTo("TikTok");
        }

        @Test
        @DisplayName("Should return empty list when no services available")
        void shouldReturnEmptyListWhenNoServices() {
            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=services"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("[]")));

            List<DripfeedServiceDto> services = client.getServices(testProvider);

            assertThat(services).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Balance")
    class GetBalanceTests {

        @Test
        @DisplayName("Should fetch balance successfully")
        void shouldFetchBalanceSuccessfully() {
            String mockResponse = """
                {"balance": "125.50", "currency": "USD"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=balance"))
                    .withRequestBody(containing("key=" + API_KEY))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedBalanceResponse response = client.getBalance(testProvider);

            assertThat(response.getBalance()).isEqualTo("125.50");
            assertThat(response.getCurrency()).isEqualTo("USD");
            assertThat(response.getBalanceAsDecimal()).isEqualByComparingTo(new BigDecimal("125.50"));
        }

        @Test
        @DisplayName("Should throw exception when balance request fails")
        void shouldThrowExceptionWhenBalanceRequestFails() {
            String mockResponse = """
                {"error": "Invalid API key"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=balance"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            assertThatThrownBy(() -> client.getBalance(testProvider))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Invalid API key");
        }
    }

    @Nested
    @DisplayName("Create Order")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully")
        void shouldCreateOrderSuccessfully() {
            String mockResponse = """
                {"order": 12345}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=add"))
                    .withRequestBody(containing("service=13311"))
                    .withRequestBody(containing("link=https%3A%2F%2Finstagram.com%2Ftestuser"))
                    .withRequestBody(containing("quantity=1000"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedOrderRequest request = DripfeedOrderRequest.builder()
                    .serviceId(13311)
                    .link("https://instagram.com/testuser")
                    .quantity(1000)
                    .build();

            DripfeedOrderResponse response = client.createOrder(testProvider, request);

            assertThat(response.getOrder()).isEqualTo(12345);
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when order creation fails - insufficient balance")
        void shouldThrowExceptionWhenInsufficientBalance() {
            String mockResponse = """
                {"error": "Not enough funds on balance"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=add"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedOrderRequest request = DripfeedOrderRequest.builder()
                    .serviceId(13311)
                    .link("https://instagram.com/testuser")
                    .quantity(1000000)
                    .build();

            assertThatThrownBy(() -> client.createOrder(testProvider, request))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Not enough funds");
        }

        @Test
        @DisplayName("Should throw exception when invalid service ID")
        void shouldThrowExceptionWhenInvalidServiceId() {
            String mockResponse = """
                {"error": "Invalid service ID"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=add"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedOrderRequest request = DripfeedOrderRequest.builder()
                    .serviceId(99999)
                    .link("https://instagram.com/testuser")
                    .quantity(1000)
                    .build();

            assertThatThrownBy(() -> client.createOrder(testProvider, request))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Invalid service");
        }

        @Test
        @DisplayName("Should create order with drip-feed parameters")
        void shouldCreateOrderWithDripfeedParams() {
            String mockResponse = """
                {"order": 12346}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=add"))
                    .withRequestBody(containing("runs=5"))
                    .withRequestBody(containing("interval=60"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedOrderRequest request = DripfeedOrderRequest.builder()
                    .serviceId(13311)
                    .link("https://instagram.com/testuser")
                    .quantity(5000)
                    .runs(5)
                    .interval(60)
                    .build();

            DripfeedOrderResponse response = client.createOrder(testProvider, request);

            assertThat(response.getOrder()).isEqualTo(12346);
        }
    }

    @Nested
    @DisplayName("Get Order Status")
    class GetOrderStatusTests {

        @Test
        @DisplayName("Should fetch order status successfully")
        void shouldFetchOrderStatusSuccessfully() {
            String mockResponse = """
                {"charge": "1.50", "start_count": "500", "status": "In progress", "remains": "250", "currency": "USD"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=status"))
                    .withRequestBody(containing("order=12345"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedStatusResponse response = client.getOrderStatus(testProvider, "12345");

            assertThat(response.getStatus()).isEqualTo("In progress");
            assertThat(response.getStartCount()).isEqualTo("500");
            assertThat(response.getRemains()).isEqualTo("250");
            assertThat(response.getCharge()).isEqualTo("1.50");
        }

        @Test
        @DisplayName("Should handle completed order status")
        void shouldHandleCompletedOrderStatus() {
            String mockResponse = """
                {"charge": "1.50", "start_count": "500", "status": "Completed", "remains": "0", "currency": "USD"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=status"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedStatusResponse response = client.getOrderStatus(testProvider, "12345");

            assertThat(response.getStatus()).isEqualTo("Completed");
            assertThat(response.getRemains()).isEqualTo("0");
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            String mockResponse = """
                {"error": "Incorrect order ID"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=status"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            assertThatThrownBy(() -> client.getOrderStatus(testProvider, "99999"))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Incorrect order");
        }
    }

    @Nested
    @DisplayName("Get Multiple Order Status")
    class GetMultipleOrderStatusTests {

        @Test
        @DisplayName("Should fetch multiple order statuses successfully")
        void shouldFetchMultipleOrderStatuses() {
            String mockResponse = """
                {
                    "12345": {"charge": "1.50", "start_count": "500", "status": "Completed", "remains": "0", "currency": "USD"},
                    "12346": {"charge": "2.00", "start_count": "1000", "status": "In progress", "remains": "500", "currency": "USD"}
                }
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=status"))
                    .withRequestBody(containing("orders=12345%2C12346"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            Map<String, DripfeedStatusResponse> responses = client.getMultipleOrderStatus(
                    testProvider, List.of("12345", "12346"));

            assertThat(responses).hasSize(2);
            assertThat(responses.get("12345").getStatus()).isEqualTo("Completed");
            assertThat(responses.get("12346").getStatus()).isEqualTo("In progress");
        }

        @Test
        @DisplayName("Should return empty map for empty order list")
        void shouldReturnEmptyMapForEmptyOrderList() {
            Map<String, DripfeedStatusResponse> responses = client.getMultipleOrderStatus(
                    testProvider, List.of());

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when exceeding max orders limit")
        void shouldThrowExceptionWhenExceedingMaxLimit() {
            List<String> tooManyOrders = java.util.stream.IntStream.range(0, 101)
                    .mapToObj(String::valueOf)
                    .toList();

            assertThatThrownBy(() -> client.getMultipleOrderStatus(testProvider, tooManyOrders))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Maximum 100 orders");
        }
    }

    @Nested
    @DisplayName("Request Refill")
    class RequestRefillTests {

        @Test
        @DisplayName("Should request refill successfully")
        void shouldRequestRefillSuccessfully() {
            String mockResponse = """
                {"refill": "67890"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=refill"))
                    .withRequestBody(containing("order=12345"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedRefillResponse response = client.requestRefill(testProvider, "12345");

            assertThat(response.getRefill()).isEqualTo("67890");
            assertThat(response.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when refill not available")
        void shouldThrowExceptionWhenRefillNotAvailable() {
            String mockResponse = """
                {"error": "Refill is not available for this order"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=refill"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            assertThatThrownBy(() -> client.requestRefill(testProvider, "12345"))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Refill is not available");
        }
    }

    @Nested
    @DisplayName("Get Refill Status")
    class GetRefillStatusTests {

        @Test
        @DisplayName("Should fetch refill status successfully")
        void shouldFetchRefillStatusSuccessfully() {
            String mockResponse = """
                {"status": "Completed"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=refill_status"))
                    .withRequestBody(containing("refill=67890"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedRefillStatusResponse response = client.getRefillStatus(testProvider, "67890");

            assertThat(response.getStatus()).isEqualTo("Completed");
        }

        @Test
        @DisplayName("Should throw exception when refill ID not found")
        void shouldThrowExceptionWhenRefillNotFound() {
            String mockResponse = """
                {"error": "Incorrect refill ID"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=refill_status"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            assertThatThrownBy(() -> client.getRefillStatus(testProvider, "99999"))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Incorrect refill");
        }
    }

    @Nested
    @DisplayName("Cancel Orders")
    class CancelOrdersTests {

        @Test
        @DisplayName("Should cancel orders successfully")
        void shouldCancelOrdersSuccessfully() {
            String mockResponse = """
                [
                    {"order": 12345, "cancel": {"status": "success"}},
                    {"order": 12346, "cancel": {"error": "Order is already completed"}}
                ]
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=cancel"))
                    .withRequestBody(containing("orders=12345%2C12346"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            List<DripfeedCancelResponse> responses = client.cancelOrders(
                    testProvider, List.of("12345", "12346"));

            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list for empty order list")
        void shouldReturnEmptyListForEmptyOrderList() {
            List<DripfeedCancelResponse> responses = client.cancelOrders(
                    testProvider, List.of());

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when exceeding max orders limit for cancel")
        void shouldThrowExceptionWhenExceedingMaxLimitForCancel() {
            List<String> tooManyOrders = java.util.stream.IntStream.range(0, 101)
                    .mapToObj(String::valueOf)
                    .toList();

            assertThatThrownBy(() -> client.cancelOrders(testProvider, tooManyOrders))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Maximum 100 orders");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle HTTP timeout gracefully")
        void shouldHandleHttpTimeoutGracefully() {
            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .willReturn(aResponse()
                            .withFixedDelay(35000))); // 35 seconds delay

            assertThatThrownBy(() -> client.getBalance(testProvider))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("HTTP error");
        }

        @Test
        @DisplayName("Should handle server error gracefully")
        void shouldHandleServerErrorGracefully() {
            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")));

            assertThatThrownBy(() -> client.getBalance(testProvider))
                    .isInstanceOf(ProviderApiException.class);
        }

        @Test
        @DisplayName("Should handle empty response gracefully")
        void shouldHandleEmptyResponseGracefully() {
            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("")));

            assertThatThrownBy(() -> client.getBalance(testProvider))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Empty response");
        }

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() {
            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .withRequestBody(containing("action=balance"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("{invalid json")));

            assertThatThrownBy(() -> client.getBalance(testProvider))
                    .isInstanceOf(ProviderApiException.class)
                    .hasMessageContaining("Failed to parse");
        }
    }

    @Nested
    @DisplayName("URL Normalization")
    class UrlNormalizationTests {

        @Test
        @DisplayName("Should handle API URL with trailing slash")
        void shouldHandleApiUrlWithTrailingSlash() {
            testProvider.setApiUrl(wireMock.baseUrl() + "/");

            String mockResponse = """
                {"balance": "100.00", "currency": "USD"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedBalanceResponse response = client.getBalance(testProvider);

            assertThat(response.getBalance()).isEqualTo("100.00");
        }

        @Test
        @DisplayName("Should handle API URL with /api/v2 suffix")
        void shouldHandleApiUrlWithApiV2Suffix() {
            testProvider.setApiUrl(wireMock.baseUrl() + "/api/v2");

            String mockResponse = """
                {"balance": "100.00", "currency": "USD"}
                """;

            wireMock.stubFor(post(urlEqualTo("/api/v2"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            DripfeedBalanceResponse response = client.getBalance(testProvider);

            assertThat(response.getBalance()).isEqualTo("100.00");
        }
    }
}
