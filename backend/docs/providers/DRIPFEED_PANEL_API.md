# Dripfeed Panel API Provider Documentation

## Overview

Dripfeed Panel is an external SMM (Social Media Marketing) service provider that offers various engagement services through their API. This document serves as a comprehensive reference for integrating Dripfeed Panel as a provider in the AntiPanel backend.

## API Base Information

| Property | Value |
|----------|-------|
| **Base URL** | `https://dripfeedpanel.com/api/v2` |
| **HTTP Method** | `POST` |
| **Content-Type** | `application/x-www-form-urlencoded` |
| **Response Format** | `JSON` |

## Authentication

All API requests require authentication via an API key parameter.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | Your unique API key from Dripfeed Panel |

---

## API Endpoints

### 1. Get Services List

Retrieves all available services with their specifications.

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `services` |

**Response:**

```json
[
  {
    "service": 1,
    "name": "Followers",
    "type": "Default",
    "category": "First Category",
    "rate": "0.90",
    "min": "50",
    "max": "10000",
    "refill": true,
    "cancel": true
  }
]
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `service` | Integer | Unique service ID |
| `name` | String | Service display name |
| `type` | String | Service type (Default, Custom Comments, etc.) |
| `category` | String | Category grouping |
| `rate` | String | Cost per 1000 units (decimal string) |
| `min` | String | Minimum order quantity |
| `max` | String | Maximum order quantity |
| `refill` | Boolean | Whether refill is supported |
| `cancel` | Boolean | Whether cancellation is supported |

---

### 2. Add Order

Creates a new order for a service. Parameters vary based on service type.

#### Common Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `add` |
| `service` | Integer | Yes | Service ID |
| `link` | String | Yes | Target URL/link |

#### Service Type Specific Parameters

##### Standard Services (Default Type)

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `quantity` | Integer | Yes | Order quantity |
| `runs` | Integer | No | Number of runs (for drip-feed) |
| `interval` | Integer | No | Interval between runs in minutes |

##### Keyword-based Services

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `keywords` | String | Yes | Keywords separated by `\r\n` or `\n` |

##### Custom Comments

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `comments` | String | Yes | Comments separated by `\r\n` or `\n` |

##### Mentions Services

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `usernames` | String | Yes | Usernames separated by `\r\n` or `\n` |

##### Mention by Hashtag

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `quantity` | Integer | Yes | Number of mentions |
| `hashtag` | String | Yes | Hashtag to scrape usernames from |

##### User Followers

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `quantity` | Integer | Yes | Number of followers |
| `username` | String | Yes | Target username URL |

##### Media Likers

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `quantity` | Integer | Yes | Number of likes |
| `media` | String | Yes | Media URL to scrape from |

##### Subscription Services

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `username` | String | Yes | Target username |
| `min` | Integer | Yes | Minimum quantity |
| `max` | Integer | Yes | Maximum quantity |
| `posts` | Integer | No | Number of posts |
| `old_posts` | Integer | No | Include old posts (0 or 1) |
| `delay` | Integer | No | Delay in minutes |
| `expiry` | String | No | Expiry date (d/m/Y format) |

**Valid delay values:** `0, 5, 10, 15, 20, 30, 40, 50, 60, 90, 120, 150, 180, 210, 240, 270, 300, 360, 420, 480, 540, 600`

##### Web Traffic Services

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `quantity` | Integer | Yes | Number of visits |
| `country` | String | Yes | Target country code |
| `device` | Integer | Yes | Device type (see below) |
| `type_of_traffic` | Integer | Yes | Traffic source type (see below) |
| `google_keyword` | String | Conditional | Required if type_of_traffic = 1 |
| `referring_url` | String | Conditional | Required if type_of_traffic = 2 |

**Device Types:**

| Value | Description |
|-------|-------------|
| 1 | Desktop |
| 2 | Mobile Android |
| 3 | Mobile iOS |
| 4 | Mixed Mobile (Android + iOS) |
| 5 | Mixed Mobile & Desktop |

**Traffic Types:**

| Value | Description |
|-------|-------------|
| 1 | Google Keyword Search |
| 2 | Custom Referrer URL |
| 3 | Blank Referrer (Direct) |

##### Poll Services

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `quantity` | Integer | Yes | Number of votes |
| `answer_number` | Integer | Yes | Poll answer number to vote for |

##### Group Services

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `quantity` | Integer | Yes | Number of joins |
| `groups` | String | Yes | Group URLs separated by `\r\n` or `\n` |

**Success Response:**

```json
{
  "order": 23501
}
```

---

### 3. Order Status

Retrieves the status of one or more orders.

#### Single Order Status

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `status` |
| `order` | Integer | Yes | Order ID |

**Response:**

```json
{
  "charge": "0.27819",
  "start_count": "3572",
  "status": "Partial",
  "remains": "157",
  "currency": "USD"
}
```

#### Multiple Order Status (up to 100)

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `status` |
| `orders` | String | Yes | Comma-separated order IDs |

**Response:**

```json
{
  "1": {
    "charge": "0.27819",
    "start_count": "3572",
    "status": "Partial",
    "remains": "157",
    "currency": "USD"
  },
  "10": {
    "error": "Incorrect order ID"
  }
}
```

**Status Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `charge` | String | Amount charged for the order |
| `start_count` | String | Initial count when order started |
| `status` | String | Current order status |
| `remains` | String | Remaining quantity to be delivered |
| `currency` | String | Currency code (usually USD) |
| `error` | String | Error message if order not found |

**Possible Status Values:**

- `Pending` - Order waiting to be processed
- `In progress` - Order is being processed
- `Completed` - Order fully delivered
- `Partial` - Order partially completed
- `Canceled` - Order was cancelled
- `Processing` - Being prepared for delivery

---

### 4. Request Refill

Initiates a refill request for orders with refill support.

#### Single Refill

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `refill` |
| `order` | Integer | Yes | Order ID |

**Response:**

```json
{
  "refill": "1"
}
```

#### Multiple Refills (up to 100)

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `refill` |
| `orders` | String | Yes | Comma-separated order IDs |

**Response:**

```json
[
  {
    "order": 1,
    "refill": 1
  },
  {
    "order": 2,
    "refill": 2
  },
  {
    "order": 3,
    "refill": {
      "error": "Incorrect order ID"
    }
  }
]
```

---

### 5. Refill Status

Checks the status of refill requests.

#### Single Refill Status

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `refill_status` |
| `refill` | Integer | Yes | Refill ID |

**Response:**

```json
{
  "status": "Completed"
}
```

#### Multiple Refill Status (up to 100)

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `refill_status` |
| `refills` | String | Yes | Comma-separated refill IDs |

**Response:**

```json
{
  "1": {
    "status": "Completed"
  },
  "2": {
    "status": "Rejected"
  }
}
```

**Refill Status Values:**

- `Pending` - Refill request waiting
- `In progress` - Refill being processed
- `Completed` - Refill delivered
- `Rejected` - Refill request denied
- `Error` - Something went wrong

---

### 6. Cancel Order

Cancels one or more orders (if cancellation is supported).

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `cancel` |
| `orders` | String | Yes | Comma-separated order IDs (up to 100) |

**Response:**

```json
[
  {
    "order": 2,
    "cancel": 1
  },
  {
    "order": 3,
    "cancel": {
      "error": "Incorrect order ID"
    }
  }
]
```

**Cancel Response:**

| Value | Meaning |
|-------|---------|
| `1` | Successfully cancelled |
| `error` object | Cancellation failed |

---

### 7. Get Balance

Retrieves the current account balance.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `key` | String | Yes | API key |
| `action` | String | Yes | Must be `balance` |

**Response:**

```json
{
  "balance": "100.84292",
  "currency": "USD"
}
```

---

## Error Handling

The API does not provide a standardized error code system. Errors are typically returned as:

1. **In response body:**
   ```json
   {
     "error": "Error message description"
   }
   ```

2. **Within array responses:**
   ```json
   {
     "order": 10,
     "cancel": {
       "error": "Incorrect order ID"
     }
   }
   ```

**Common Error Messages:**

| Error Message | Likely Cause |
|---------------|--------------|
| `Incorrect order ID` | Order doesn't exist or doesn't belong to your account |
| `Not enough funds` | Insufficient balance for the order |
| `Invalid service` | Service ID doesn't exist |
| `Min/Max quantity` | Quantity outside allowed range |
| `Invalid API key` | Wrong or expired API key |
| `Link is invalid` | Malformed or unsupported URL |

---

## Rate Limits

The API documentation does not explicitly mention rate limits. However, best practices suggest:

- Implement exponential backoff on errors
- Batch status checks using multi-order endpoints
- Cache service list responses (refresh periodically)
- Avoid excessive polling (use reasonable intervals)

---

## Implementation Recommendations

### 1. Service Synchronization

Create a scheduled job to sync services from the provider:

```
Schedule: Every 6-12 hours
Action: Fetch services list and update local ProviderService records
Track: lastSyncedAt timestamp on Provider entity
```

### 2. Order Status Tracking

Implement efficient polling for active orders:

```
Pending orders: Poll every 1-2 minutes
In-progress orders: Poll every 5 minutes
Use batch status endpoint (up to 100 orders per request)
```

### 3. Balance Monitoring

Track provider balance for operational alerts:

```
Check: Before creating orders
Alert: When balance falls below threshold
Update: After each order/status check
```

### 4. Error Handling Strategy

```
1. Parse response for "error" field
2. Map known errors to appropriate exceptions
3. Log unknown errors for investigation
4. Implement retry logic for transient failures
```

---

## Service Type Mapping

When integrating with our internal service system, map Dripfeed service types:

| Dripfeed Type | Internal ServiceType | Parameters |
|---------------|---------------------|------------|
| Default | `DEFAULT` | quantity, runs, interval |
| Custom Comments | `CUSTOM_COMMENTS` | comments |
| Mentions | `MENTIONS` | usernames |
| Mentions with Hashtag | `MENTIONS_HASHTAG` | quantity, hashtag |
| Subscriptions | `SUBSCRIPTION` | username, min, max, delay, expiry |
| Web Traffic | `WEB_TRAFFIC` | quantity, country, device, type_of_traffic |
| Poll | `POLL` | quantity, answer_number |
| Keywords | `KEYWORDS` | keywords |
| Groups | `GROUPS` | quantity, groups |

---

## Security Considerations

1. **API Key Storage**: Store encrypted in database, never in logs
2. **Request Logging**: Mask sensitive data (API keys, user links)
3. **Balance Validation**: Always verify balance before placing orders
4. **Input Validation**: Validate all user inputs before sending to provider
5. **SSL/TLS**: Always use HTTPS (provider enforces this)

---

## Testing Strategy

### Test Endpoints (if available)

Some providers offer sandbox/test modes. Check with Dripfeed Panel support.

### Integration Testing

1. Test service list retrieval
2. Test balance check
3. Test order creation (with small quantity)
4. Test order status polling
5. Test refill flow
6. Test cancellation flow

### Error Scenarios

1. Invalid API key
2. Insufficient balance
3. Invalid service ID
4. Quantity out of range
5. Invalid link format
6. Network timeouts

---

## Appendix A: Complete Request Examples

### Get Services

```http
POST /api/v2 HTTP/1.1
Host: dripfeedpanel.com
Content-Type: application/x-www-form-urlencoded

key=YOUR_API_KEY&action=services
```

### Create Standard Order

```http
POST /api/v2 HTTP/1.1
Host: dripfeedpanel.com
Content-Type: application/x-www-form-urlencoded

key=YOUR_API_KEY&action=add&service=1&link=https://instagram.com/example&quantity=1000
```

### Create Drip-feed Order

```http
POST /api/v2 HTTP/1.1
Host: dripfeedpanel.com
Content-Type: application/x-www-form-urlencoded

key=YOUR_API_KEY&action=add&service=1&link=https://instagram.com/example&quantity=1000&runs=10&interval=60
```

### Check Multiple Order Status

```http
POST /api/v2 HTTP/1.1
Host: dripfeedpanel.com
Content-Type: application/x-www-form-urlencoded

key=YOUR_API_KEY&action=status&orders=1,2,3,4,5
```

### Get Balance

```http
POST /api/v2 HTTP/1.1
Host: dripfeedpanel.com
Content-Type: application/x-www-form-urlencoded

key=YOUR_API_KEY&action=balance
```

---

## Appendix B: Response Status Flow

```
Order Lifecycle:
Pending → In progress → Completed
                     → Partial (if not fully delivered)
                     → Canceled (if cancelled)

Refill Lifecycle:
Pending → In progress → Completed
                     → Rejected (if denied)
```

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-10 | AntiPanel Team | Initial documentation |
