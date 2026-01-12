-- Migration V7: Configure DripFeedPanel services with exact user requirements
-- This migration sets up the production-ready service catalog
-- WARNING: This clears test orders/invoices for a fresh service catalog setup

BEGIN;

-- ============================================
-- 1. CLEAN EXISTING DATA (handle FK constraints)
-- ============================================

-- Clear test orders and related data first (FK constraint on services)
DELETE FROM order_refills;
DELETE FROM orders;
DELETE FROM invoices;
DELETE FROM transactions;

-- Delete existing services (now safe)
DELETE FROM services;

-- Delete existing provider services
DELETE FROM provider_services;

-- Delete all providers
DELETE FROM providers;

-- ============================================
-- 2. CREATE DRIPFEEDPANEL PROVIDER
-- ============================================

INSERT INTO providers (id, name, website, api_url, api_key, is_active, balance)
VALUES (1, 'DripFeedPanel', 'https://dripfeedpanel.com', 'https://dripfeedpanel.com/api/v2', NULL, true, 0)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    website = EXCLUDED.website,
    api_url = EXCLUDED.api_url,
    is_active = EXCLUDED.is_active;

-- Reset sequence
SELECT setval('providers_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM providers), false);

-- ============================================
-- 3. DEACTIVATE UNUSED CATEGORIES
-- ============================================

UPDATE categories SET is_active = false WHERE slug IN ('youtube', 'facebook', 'spotify', 'twitch');
UPDATE categories SET is_active = true WHERE slug IN ('instagram', 'tiktok', 'twitter', 'linkedin');

-- ============================================
-- 4. ADD LINKEDIN SERVICE TYPES (if missing)
-- ============================================

DO $$
DECLARE
    linkedin_id INTEGER;
    max_sort INTEGER;
BEGIN
    SELECT id INTO linkedin_id FROM categories WHERE slug = 'linkedin';
    SELECT COALESCE(MAX(sort_order), 0) INTO max_sort FROM service_types WHERE category_id = linkedin_id;

    -- Add company-followers if not exists
    INSERT INTO service_types (category_id, name, slug, sort_order, is_active)
    SELECT linkedin_id, 'Company Followers', 'company-followers', max_sort + 1, true
    WHERE NOT EXISTS (SELECT 1 FROM service_types WHERE category_id = linkedin_id AND slug = 'company-followers');

    -- Add reposts if not exists
    INSERT INTO service_types (category_id, name, slug, sort_order, is_active)
    SELECT linkedin_id, 'Reposts', 'reposts', max_sort + 2, true
    WHERE NOT EXISTS (SELECT 1 FROM service_types WHERE category_id = linkedin_id AND slug = 'reposts');
END $$;

-- ============================================
-- 5. INSERT PROVIDER SERVICES (DripFeedPanel IDs)
-- ============================================

INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active) VALUES
-- Instagram
(1, '13311', 'Instagram Followers', 10, 100000, 1.0000, 30, true),
(1, '15856', 'Instagram Likes', 10, 50000, 0.3100, 0, true),

-- TikTok
(1, '16132', 'TikTok Followers', 10, 100000, 1.6900, 30, true),
(1, '7169', 'TikTok Likes', 10, 50000, 0.1760, 0, true),
(1, '8228', 'TikTok Views', 100, 1000000, 0.0200, 0, true),

-- Twitter
(1, '11811', 'Twitter Followers', 10, 50000, 12.5000, 30, true),
(1, '3662', 'Twitter Likes', 10, 50000, 4.2200, 0, true),

-- LinkedIn
(1, '16136', 'LinkedIn Profile Followers', 10, 10000, 16.2500, 30, true),
(1, '16137', 'LinkedIn Company Followers', 10, 10000, 16.2500, 30, true),
(1, '16142', 'LinkedIn Connections', 10, 5000, 18.7500, 30, true),
(1, '16141', 'LinkedIn Reposts', 10, 10000, 11.2500, 0, true);

-- ============================================
-- 6. INSERT PUBLIC SERVICES (with selling prices)
-- ============================================

-- Instagram Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'Instagram Followers',
    'High quality Instagram followers with 30 days refill guarantee',
    1.9900, 'HIGH', 'MEDIUM', 10, 100000, 30, true, 1
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'instagram' AND st.slug = 'followers' AND st.category_id = c.id AND ps.provider_service_id = '13311';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'Instagram Likes',
    'Fast Instagram likes for posts and reels',
    0.4900, 'HIGH', 'FAST', 10, 50000, 0, true, 2
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'instagram' AND st.slug = 'likes' AND st.category_id = c.id AND ps.provider_service_id = '15856';

-- TikTok Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'TikTok Followers',
    'Real TikTok followers with 30 days refill guarantee',
    2.9900, 'HIGH', 'MEDIUM', 10, 100000, 30, true, 1
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'tiktok' AND st.slug = 'followers' AND st.category_id = c.id AND ps.provider_service_id = '16132';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'TikTok Likes',
    'Fast TikTok likes for videos',
    0.3300, 'HIGH', 'FAST', 10, 50000, 0, true, 2
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'tiktok' AND st.slug = 'likes' AND st.category_id = c.id AND ps.provider_service_id = '7169';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'TikTok Views',
    'High retention TikTok views',
    0.0500, 'HIGH', 'INSTANT', 100, 1000000, 0, true, 3
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'tiktok' AND st.slug = 'views' AND st.category_id = c.id AND ps.provider_service_id = '8228';

-- Twitter Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'Twitter Followers',
    'Premium Twitter/X followers with 30 days refill',
    15.0000, 'PREMIUM', 'MEDIUM', 10, 50000, 30, true, 1
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'twitter' AND st.slug = 'followers' AND st.category_id = c.id AND ps.provider_service_id = '11811';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'Twitter Likes',
    'Fast Twitter/X likes for tweets',
    4.9900, 'HIGH', 'FAST', 10, 50000, 0, true, 2
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'twitter' AND st.slug = 'likes' AND st.category_id = c.id AND ps.provider_service_id = '3662';

-- LinkedIn Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'LinkedIn Profile Followers',
    'Professional LinkedIn profile followers',
    19.9900, 'PREMIUM', 'SLOW', 10, 10000, 30, true, 1
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'linkedin' AND st.slug = 'followers' AND st.category_id = c.id AND ps.provider_service_id = '16136';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'LinkedIn Company Followers',
    'Boost your company page with followers',
    19.9900, 'PREMIUM', 'SLOW', 10, 10000, 30, true, 2
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'linkedin' AND st.slug = 'company-followers' AND st.category_id = c.id AND ps.provider_service_id = '16137';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'LinkedIn Connections',
    'Expand your professional network',
    25.0000, 'PREMIUM', 'SLOW', 10, 5000, 30, true, 3
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'linkedin' AND st.slug = 'connections' AND st.category_id = c.id AND ps.provider_service_id = '16142';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT
    c.id, st.id, ps.id,
    'LinkedIn Reposts',
    'Increase reach with post reposts',
    15.0000, 'HIGH', 'MEDIUM', 10, 10000, 0, true, 4
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'linkedin' AND st.slug = 'reposts' AND st.category_id = c.id AND ps.provider_service_id = '16141';

COMMIT;
