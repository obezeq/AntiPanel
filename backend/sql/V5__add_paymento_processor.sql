-- Migration V5: Add Paymento payment processor
-- Purpose: Configure Paymento cryptocurrency payment gateway

-- Insert Paymento payment processor
INSERT INTO payment_processors (
    name,
    code,
    website,
    api_key,
    api_secret,
    config_json,
    min_amount,
    max_amount,
    fee_percentage,
    fee_fixed,
    is_active,
    sort_order
) VALUES (
    'Paymento',
    'paymento',
    'https://paymento.io',
    'MzE1OERFRDczMkEyMUM2QzRGMEMzNzk3MjAzQTY2REE=',
    'MzExOERDN0ZDM0I3NzZFNDhDMUJGRjY1MDNBNjhCQ0M=',
    '{"baseUrl": "https://api.paymento.io/v1", "speed": 0, "returnUrl": "http://localhost:4200/wallet"}',
    1.00,
    10000.00,
    0.50,
    0.00,
    true,
    1
) ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    website = EXCLUDED.website,
    api_key = EXCLUDED.api_key,
    api_secret = EXCLUDED.api_secret,
    config_json = EXCLUDED.config_json,
    min_amount = EXCLUDED.min_amount,
    max_amount = EXCLUDED.max_amount,
    fee_percentage = EXCLUDED.fee_percentage,
    fee_fixed = EXCLUDED.fee_fixed,
    is_active = EXCLUDED.is_active,
    sort_order = EXCLUDED.sort_order;

-- Add comment for documentation
COMMENT ON COLUMN payment_processors.api_secret IS 'API secret for webhook HMAC verification (Paymento uses X-HMAC-SHA256-SIGNATURE header)';
