# Paymento API Provider Documentation

## Overview

Paymento is a decentralized cryptocurrency payment gateway that allows merchants to accept crypto payments directly to their wallets. This document serves as a comprehensive reference for integrating Paymento as a payment processor in the AntiPanel backend.

## API Base Information

| Property | Value |
|----------|-------|
| **Base URL** | `https://api.paymento.io/v1` |
| **HTTP Method** | `POST` (most endpoints), `GET` (read operations) |
| **Content-Type** | `application/json` |
| **Response Format** | `JSON` |
| **Payment Gateway URL** | `https://app.paymento.io/gateway?token={TOKEN}` |

## Authentication

All API requests require authentication via an API key header.

| Header | Type | Required | Description |
|--------|------|----------|-------------|
| `Api-Key` | String | Yes | Your merchant API key from Paymento dashboard |
| `Content-Type` | String | Yes | Must be `application/json` |
| `Accept` | String | Yes | Must be `text/plain` |

---

## API Endpoints

### 1. Create Payment Request

Creates a new payment invoice for cryptocurrency deposit.

**Endpoint:** `POST /payment/request`

**Request Body:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `fiatAmount` | String | No* | Amount in fiat currency |
| `fiatCurrency` | String | No* | Currency code (e.g., `USD`, `EUR`) |
| `ReturnUrl` | String | No | Redirect URL after payment |
| `orderId` | String | No | Your internal order/invoice ID |
| `Speed` | Integer | No | 0=High (mempool), 1=Low (confirmed blocks) |
| `cryptoAmount` | Object | No* | Specific crypto amounts |
| `additionalData` | Object | No | Key-value metadata |
| `EmailAddress` | String | No | Customer email address |

*Either `fiatAmount`/`fiatCurrency` OR `cryptoAmount` should be provided.

**Success Response (200):**

```json
{
  "success": true,
  "message": "",
  "body": "3256e147c6fe4d36a9341a5112ed2214"
}
```

**Error Response (400):**

```json
{
  "success": false,
  "error": "Invalid request"
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `success` | Boolean | Whether the request succeeded |
| `message` | String | Optional message |
| `body` | String | Payment token (used for redirect) |
| `error` | String | Error message (on failure) |

**Usage:**
After receiving the token, redirect the user to:
```
https://app.paymento.io/gateway?token={TOKEN}
```

---

### 2. Verify Payment

Verifies the status of a payment after receiving a callback.

**Endpoint:** `POST /payment/verify`

**Request Body:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `token` | String | Yes | Token received from payment request |

**Success Response (200):**

```json
{
  "success": true,
  "message": "",
  "body": {
    "token": "3256e147c6fe4d36a9341a5112ed2214",
    "orderId": "5855",
    "additionalData": [
      {
        "key": "invoice-number",
        "value": "A-578"
      }
    ]
  }
}
```

**Error Response (400):**

```json
{
  "success": false,
  "error": "Invalid request"
}
```

---

### 3. Set Payment Settings

Configures the webhook (IPN) URL for payment notifications.

**Endpoint:** `POST /payment/settings`

**Request Body:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `IPN_Url` | String | No | Webhook destination URL |
| `httpMethod` | Integer | No | HTTP method: 1=POST, 2=PUT |

**Success Response (200):**

```json
{
  "success": true,
  "message": "",
  "body": {
    "IPN_Url": "https://api.yoursite.com/webhooks/paymento",
    "IPN_httpMethod": 1
  }
}
```

---

### 4. Get Payment Settings

Retrieves the current webhook configuration.

**Endpoint:** `GET /payment/settings`

**Success Response (200):**

```json
{
  "success": true,
  "message": "",
  "body": {
    "IPN_Url": "https://api.yoursite.com/webhooks/paymento",
    "IPN_httpMethod": 1
  }
}
```

---

### 5. Get Accepted Coins

Retrieves the list of cryptocurrencies accepted by the merchant.

**Endpoint:** `GET /payment/coins`

**Success Response (200):**

```json
{
  "success": true,
  "message": "",
  "body": [
    {
      "name": "bitcoin",
      "shortcut": "btc"
    },
    {
      "name": "ethereum",
      "shortcut": "eth"
    },
    {
      "name": "tether",
      "shortcut": "usdt"
    }
  ]
}
```

---

## Webhook (IPN) Callback

Paymento sends payment status updates to your configured IPN URL.

### Callback Headers

| Header | Description |
|--------|-------------|
| `X-HMAC-SHA256-SIGNATURE` | HMAC-SHA256 signature of the payload (uppercase hex) |

### Callback Body

| Parameter | Type | Description |
|-----------|------|-------------|
| `Token` | String | Unique payment token |
| `PaymentId` | String | Paymento internal payment ID |
| `OrderId` | String | Your internal order ID |
| `OrderStatus` | Integer | Payment status code |
| `AdditionalData` | Object | Metadata from original request |

### Order Status Codes

| Code | Status | Description |
|------|--------|-------------|
| 0 | Initialize | Payment request accepted by API |
| 1 | Pending | Customer selected cryptocurrency |
| 2 | PartialPaid | Received amount below order total |
| 3 | WaitingToConfirm | Transaction in blockchain mempool/blocks |
| 4 | Timeout | Payment deadline expired |
| 5 | UserCanceled | Customer clicked cancel button |
| 7 | Paid | Transaction confirmed on blockchain |
| 8 | Approve | Merchant verified the payment |
| 9 | Reject | Address no longer monitored / verification failed |

### Status Mapping to Internal InvoiceStatus

| Paymento Status | Internal Status |
|-----------------|-----------------|
| 0, 1, 2, 3 | PROCESSING |
| 4 | EXPIRED |
| 5 | CANCELLED |
| 7, 8 | COMPLETED |
| 9 | FAILED |

---

## Signature Verification

All webhook callbacks must be verified using HMAC-SHA256.

### Verification Process

1. Extract raw POST request body (JSON string)
2. Retrieve your API secret from Paymento dashboard (stored in `payment_processors.api_secret`)
3. Calculate HMAC-SHA256 hash using the payload and secret key
4. Convert result to **uppercase hexadecimal** format
5. Compare calculated signature with `X-HMAC-SHA256-SIGNATURE` header

### Java Implementation

```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public boolean verifySignature(String payload, String receivedSignature, String secret) {
    try {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(keySpec);

        byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String calculatedSignature = bytesToHex(hash).toUpperCase();

        return calculatedSignature.equals(receivedSignature);
    } catch (Exception e) {
        return false;
    }
}

private String bytesToHex(byte[] bytes) {
    StringBuilder hex = new StringBuilder();
    for (byte b : bytes) {
        hex.append(String.format("%02x", b));
    }
    return hex.toString();
}
```

---

## Fee Structure

| Tier | Fee | Condition |
|------|-----|-----------|
| Free | 0% | First $20,000 USD in transactions |
| Standard | 0.5% | After free tier |
| PMO Token | 0.4% | Pay fees with PMO tokens (20% discount) |

---

## Testing

### Test Networks Supported

| Network | Testnet |
|---------|---------|
| Bitcoin | Testnet |
| Ethereum | Sepolia |
| Tron | Shasta |

### Testing Workflow

1. Create testnet wallet compatible with supported networks
2. Acquire testnet cryptocurrency from faucets
3. Configure Paymento to use testnet in merchant dashboard
4. Create test payments and verify webhook handling

---

## Error Handling

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| `Invalid request` | Malformed request body | Check JSON format and required fields |
| `Invalid API key` | Wrong or missing Api-Key header | Verify API key in merchant dashboard |
| `Invalid signature` | HMAC verification failed | Check secret key and signature algorithm |
| `Order not found` | Unknown orderId in webhook | Ensure orderId matches internal invoice ID |

### Best Practices

1. **Always verify webhooks** - Never trust webhook data without HMAC verification
2. **Idempotent processing** - Check invoice status before processing (avoid duplicate balance updates)
3. **Log all callbacks** - Keep audit trail of all webhook calls for debugging
4. **Verify independently** - Call `/payment/verify` after receiving "Paid" status for double confirmation
5. **Handle timeouts** - Implement retry logic for API calls with exponential backoff

---

## Integration Flow

```
1. User initiates deposit
   └── Frontend calls POST /api/v1/invoices

2. Backend creates invoice
   └── InvoiceService.create() saves PENDING invoice

3. Backend calls Paymento API
   └── PaymentoClient.createPayment() → POST /payment/request
   └── Update invoice with token, payment URL, status=PROCESSING

4. Frontend redirects user
   └── window.location.href = invoice.paymentUrl

5. User completes payment on Paymento
   └── Paymento processes blockchain transaction

6. Paymento sends webhook
   └── POST /api/v1/webhooks/paymento
   └── Verify HMAC signature
   └── PaymentoWebhookService.processWebhook()

7. Backend processes payment
   └── If PAID: InvoiceService.completePayment()
       └── Update invoice status=COMPLETED
       └── Add netAmount to user.balance
       └── Create Transaction record (DEPOSIT)
   └── If FAILED/EXPIRED/CANCELLED: Update invoice status

8. User redirected to return URL
   └── Frontend refreshes wallet data
```

---

## Database Schema

### payment_processors Table Entry

```sql
INSERT INTO payment_processors (
    name, code, website, api_key, api_secret, config_json,
    min_amount, max_amount, fee_percentage, fee_fixed,
    is_active, sort_order
) VALUES (
    'Paymento',
    'paymento',
    'https://paymento.io',
    'API_KEY_HERE',
    'API_SECRET_HERE',
    '{"baseUrl":"https://api.paymento.io/v1","speed":0}',
    1.00,
    10000.00,
    0.50,
    0.00,
    true,
    1
);
```

### Configuration Fields in config_json

| Field | Type | Description |
|-------|------|-------------|
| `baseUrl` | String | API base URL (for testing/production) |
| `speed` | Integer | Default transaction speed (0=High, 1=Low) |
| `returnUrl` | String | Override default return URL |

---

## References

- [Paymento Documentation](https://docs.paymento.io/)
- [API Overview](https://docs.paymento.io/api-documention/api-overview)
- [Payment Request API](https://docs.paymento.io/api-documention/payment-request)
- [Payment Callback API](https://docs.paymento.io/api-documention/payment-callback)
- [Payment Verify API](https://docs.paymento.io/api-documention/payment-verify)
