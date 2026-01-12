package com.antipanel.backend.service.payment;

import com.antipanel.backend.config.PaymentoConfig;
import com.antipanel.backend.dto.paymento.PaymentoCoinDto;
import com.antipanel.backend.dto.paymento.PaymentoPaymentResponse;
import com.antipanel.backend.dto.paymento.PaymentoVerifyResponse;
import com.antipanel.backend.entity.Invoice;
import com.antipanel.backend.entity.PaymentProcessor;
import com.antipanel.backend.entity.User;
import com.antipanel.backend.entity.enums.InvoiceStatus;
import com.antipanel.backend.exception.PaymentoApiException;
import com.antipanel.backend.service.payment.impl.PaymentoClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for PaymentoClientImpl using WireMock to mock HTTP responses.
 * No real API calls are made - all responses are mocked.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableWireMock
@ActiveProfiles("test")
@DisplayName("PaymentoClientImpl Tests")
class PaymentoClientImplTest {

    @InjectWireMock
    private WireMockServer wireMock;

    private PaymentoClientImpl client;
    private PaymentProcessor testProcessor;
    private Invoice testInvoice;
    private User testUser;
    private ObjectMapper objectMapper;

    private static final String API_KEY = "test_api_key_12345";
    private static final String API_SECRET = "test_api_secret_12345";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        RestClient.Builder restClientBuilder = RestClient.builder();

        PaymentoConfig paymentoConfig = new PaymentoConfig(
                API_KEY,
                API_SECRET,
                wireMock.baseUrl(),
                "http://localhost:4200/wallet",
                0
        );

        client = new PaymentoClientImpl(restClientBuilder, objectMapper, paymentoConfig);

        testProcessor = PaymentProcessor.builder()
                .id(1)
                .name("Paymento")
                .code("paymento")
                .apiKey(API_KEY)
                .apiSecret(API_SECRET)
                .isActive(true)
                .minAmount(new BigDecimal("1.00"))
                .maxAmount(new BigDecimal("10000.00"))
                .feePercentage(new BigDecimal("0.50"))
                .feeFixed(BigDecimal.ZERO)
                .build();

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .build();

        testInvoice = Invoice.builder()
                .id(123L)
                .user(testUser)
                .processor(testProcessor)
                .amount(new BigDecimal("100.00"))
                .fee(new BigDecimal("0.50"))
                .netAmount(new BigDecimal("99.50"))
                .currency("USD")
                .status(InvoiceStatus.PENDING)
                .build();
    }

    @Nested
    @DisplayName("Create Payment")
    class CreatePaymentTests {

        @Test
        @DisplayName("Should create payment successfully")
        void shouldCreatePaymentSuccessfully() {
            String mockResponse = """
                {
                    "success": true,
                    "message": "",
                    "body": "abc123def456token"
                }
                """;

            wireMock.stubFor(post(urlEqualTo("/payment/request"))
                    .withHeader("Api-Key", equalTo(API_KEY))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            PaymentoPaymentResponse response = client.createPayment(testProcessor, testInvoice);

            assertThat(response).isNotNull();
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getToken()).isEqualTo("abc123def456token");
            assertThat(response.getPaymentUrl()).isEqualTo("https://app.paymento.io/gateway?token=abc123def456token");
        }

        @Test
        @DisplayName("Should throw exception on payment creation error")
        void shouldThrowExceptionOnPaymentCreationError() {
            String mockResponse = """
                {
                    "success": false,
                    "error": "Invalid amount"
                }
                """;

            wireMock.stubFor(post(urlEqualTo("/payment/request"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            assertThatThrownBy(() -> client.createPayment(testProcessor, testInvoice))
                    .isInstanceOf(PaymentoApiException.class)
                    .hasMessageContaining("Invalid amount");
        }

        @Test
        @DisplayName("Should throw exception on empty response")
        void shouldThrowExceptionOnEmptyResponse() {
            wireMock.stubFor(post(urlEqualTo("/payment/request"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody("")));

            assertThatThrownBy(() -> client.createPayment(testProcessor, testInvoice))
                    .isInstanceOf(PaymentoApiException.class)
                    .hasMessageContaining("Empty response");
        }

        @Test
        @DisplayName("Should throw exception on HTTP error")
        void shouldThrowExceptionOnHttpError() {
            wireMock.stubFor(post(urlEqualTo("/payment/request"))
                    .willReturn(aResponse()
                            .withStatus(500)));

            assertThatThrownBy(() -> client.createPayment(testProcessor, testInvoice))
                    .isInstanceOf(PaymentoApiException.class)
                    .hasMessageContaining("HTTP error");
        }
    }

    @Nested
    @DisplayName("Verify Payment")
    class VerifyPaymentTests {

        @Test
        @DisplayName("Should verify payment successfully")
        void shouldVerifyPaymentSuccessfully() {
            String mockResponse = """
                {
                    "success": true,
                    "message": "",
                    "body": {
                        "token": "abc123def456token",
                        "orderId": "123",
                        "additionalData": [
                            {"key": "user_id", "value": "1"}
                        ]
                    }
                }
                """;

            wireMock.stubFor(post(urlEqualTo("/payment/verify"))
                    .withHeader("Api-Key", equalTo(API_KEY))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            PaymentoVerifyResponse response = client.verifyPayment(testProcessor, "abc123def456token");

            assertThat(response).isNotNull();
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getToken()).isEqualTo("abc123def456token");
            assertThat(response.getBody().getOrderId()).isEqualTo("123");
        }

        @Test
        @DisplayName("Should return error response on verification failure")
        void shouldReturnErrorResponseOnVerificationFailure() {
            String mockResponse = """
                {
                    "success": false,
                    "error": "Token not found"
                }
                """;

            wireMock.stubFor(post(urlEqualTo("/payment/verify"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            PaymentoVerifyResponse response = client.verifyPayment(testProcessor, "invalid_token");

            assertThat(response).isNotNull();
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.hasError()).isTrue();
        }
    }

    @Nested
    @DisplayName("Get Coins")
    class GetCoinsTests {

        @Test
        @DisplayName("Should fetch coins successfully")
        void shouldFetchCoinsSuccessfully() {
            String mockResponse = """
                {
                    "success": true,
                    "message": "",
                    "body": [
                        {"name": "bitcoin", "shortcut": "btc"},
                        {"name": "ethereum", "shortcut": "eth"},
                        {"name": "tether", "shortcut": "usdt"}
                    ]
                }
                """;

            wireMock.stubFor(get(urlEqualTo("/payment/coins"))
                    .withHeader("Api-Key", equalTo(API_KEY))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            List<PaymentoCoinDto> coins = client.getCoins(testProcessor);

            assertThat(coins).hasSize(3);
            assertThat(coins.get(0).getName()).isEqualTo("bitcoin");
            assertThat(coins.get(0).getShortcut()).isEqualTo("btc");
            assertThat(coins.get(0).getDisplayName()).isEqualTo("Bitcoin");
            assertThat(coins.get(0).getSymbol()).isEqualTo("BTC");
        }

        @Test
        @DisplayName("Should return empty list when no coins available")
        void shouldReturnEmptyListWhenNoCoinsAvailable() {
            String mockResponse = """
                {
                    "success": true,
                    "body": []
                }
                """;

            wireMock.stubFor(get(urlEqualTo("/payment/coins"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            List<PaymentoCoinDto> coins = client.getCoins(testProcessor);

            assertThat(coins).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception on coins API error")
        void shouldThrowExceptionOnCoinsApiError() {
            String mockResponse = """
                {
                    "success": false,
                    "error": "Invalid API key"
                }
                """;

            wireMock.stubFor(get(urlEqualTo("/payment/coins"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            assertThatThrownBy(() -> client.getCoins(testProcessor))
                    .isInstanceOf(PaymentoApiException.class)
                    .hasMessageContaining("Invalid API key");
        }
    }

    @Nested
    @DisplayName("Configure Webhook")
    class ConfigureWebhookTests {

        @Test
        @DisplayName("Should configure webhook successfully")
        void shouldConfigureWebhookSuccessfully() {
            String mockResponse = """
                {
                    "success": true,
                    "message": "",
                    "body": {
                        "IPN_Url": "https://api.example.com/webhooks/paymento",
                        "IPN_httpMethod": 1
                    }
                }
                """;

            wireMock.stubFor(post(urlEqualTo("/payment/settings"))
                    .withHeader("Api-Key", equalTo(API_KEY))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            var response = client.configureWebhook(testProcessor, "https://api.example.com/webhooks/paymento");

            assertThat(response).isNotNull();
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getBody().getIpnUrl()).isEqualTo("https://api.example.com/webhooks/paymento");
        }

        @Test
        @DisplayName("Should throw exception on webhook configuration error")
        void shouldThrowExceptionOnWebhookConfigurationError() {
            String mockResponse = """
                {
                    "success": false,
                    "error": "Invalid URL format"
                }
                """;

            wireMock.stubFor(post(urlEqualTo("/payment/settings"))
                    .willReturn(aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mockResponse)));

            assertThatThrownBy(() -> client.configureWebhook(testProcessor, "invalid-url"))
                    .isInstanceOf(PaymentoApiException.class)
                    .hasMessageContaining("Invalid URL format");
        }
    }
}
