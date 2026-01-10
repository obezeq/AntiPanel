-- ============================================================================
-- DRIPFEED PANEL PROVIDER SETUP
-- PostgreSQL 18
-- ============================================================================
-- Proyecto: AntiPanel
-- Descripcion: Configuracion del proveedor Dripfeed Panel con servicios
-- ============================================================================
-- IMPORTANTE: Ejecutar DESPUES de example.sql
-- ============================================================================

-- ============================================================================
-- 1. NUEVOS TIPOS DE SERVICIO PARA LINKEDIN
-- ============================================================================

-- LinkedIn (category_id = 6) - Nuevos tipos que no existen
INSERT INTO service_types (category_id, name, slug, sort_order, is_active) VALUES
(6, 'Company Followers', 'company-followers', 5, TRUE),
(6, 'Reposts', 'reposts', 6, TRUE)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- 2. PROVEEDOR DRIPFEED PANEL
-- ============================================================================

INSERT INTO providers (name, website, api_url, api_key, is_active, balance) VALUES
('DripfeedPanel', 'https://dripfeedpanel.com', 'https://dripfeedpanel.com/api/v2', '2fa121263f7f87057e47376fcb655b18', TRUE, 0.0000);

-- ============================================================================
-- 3. SERVICIOS DEL PROVEEDOR (provider_services)
-- ============================================================================

-- Obtener el ID del proveedor Dripfeed Panel
DO $$
DECLARE
    dripfeed_provider_id INTEGER;
    instagram_followers_st INTEGER;
    instagram_likes_st INTEGER;
    tiktok_followers_st INTEGER;
    tiktok_likes_st INTEGER;
    tiktok_views_st INTEGER;
    twitter_followers_st INTEGER;
    twitter_likes_st INTEGER;
    linkedin_followers_st INTEGER;
    linkedin_company_followers_st INTEGER;
    linkedin_connections_st INTEGER;
    linkedin_reposts_st INTEGER;
BEGIN
    -- Obtener ID del proveedor
    SELECT id INTO dripfeed_provider_id FROM providers WHERE name = 'DripfeedPanel';

    IF dripfeed_provider_id IS NULL THEN
        RAISE EXCEPTION 'DripfeedPanel provider not found';
    END IF;

    -- Obtener IDs de service_types
    -- Instagram (category_id = 1)
    SELECT id INTO instagram_followers_st FROM service_types WHERE category_id = 1 AND slug = 'followers';
    SELECT id INTO instagram_likes_st FROM service_types WHERE category_id = 1 AND slug = 'likes';

    -- TikTok (category_id = 2)
    SELECT id INTO tiktok_followers_st FROM service_types WHERE category_id = 2 AND slug = 'followers';
    SELECT id INTO tiktok_likes_st FROM service_types WHERE category_id = 2 AND slug = 'likes';
    SELECT id INTO tiktok_views_st FROM service_types WHERE category_id = 2 AND slug = 'views';

    -- Twitter (category_id = 4)
    SELECT id INTO twitter_followers_st FROM service_types WHERE category_id = 4 AND slug = 'followers';
    SELECT id INTO twitter_likes_st FROM service_types WHERE category_id = 4 AND slug = 'likes';

    -- LinkedIn (category_id = 6)
    SELECT id INTO linkedin_followers_st FROM service_types WHERE category_id = 6 AND slug = 'followers';
    SELECT id INTO linkedin_company_followers_st FROM service_types WHERE category_id = 6 AND slug = 'company-followers';
    SELECT id INTO linkedin_connections_st FROM service_types WHERE category_id = 6 AND slug = 'connections';
    SELECT id INTO linkedin_reposts_st FROM service_types WHERE category_id = 6 AND slug = 'reposts';

    -- ========================================================================
    -- PROVIDER SERVICES (11 servicios)
    -- ========================================================================

    -- Instagram Followers - Dripfeed ID: 13311, Cost: $1.00/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '13311', 'Instagram Followers [Real] [30 Days Refill]', 10, 300000, 1.0000, 30, TRUE);

    -- Instagram Likes - Dripfeed ID: 15856, Cost: $0.31/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '15856', 'Instagram Likes [Real] [Non Drop]', 10, 300000, 0.3100, 0, TRUE);

    -- TikTok Followers - Dripfeed ID: 16132, Cost: $1.69/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '16132', 'TikTok Followers [Real] [30 Days Refill]', 10, 100000, 1.6900, 30, TRUE);

    -- TikTok Likes - Dripfeed ID: 7169, Cost: $0.176/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '7169', 'TikTok Likes [Real]', 10, 100000, 0.1760, 0, TRUE);

    -- TikTok Views - Dripfeed ID: 8228, Cost: $0.02/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '8228', 'TikTok Views [Instant]', 100, 10000000, 0.0200, 0, TRUE);

    -- Twitter Followers - Dripfeed ID: 11811, Cost: $12.50/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '11811', 'Twitter Followers [Real] [30 Days Refill]', 10, 100000, 12.5000, 30, TRUE);

    -- Twitter Likes - Dripfeed ID: 3662, Cost: $4.22/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '3662', 'Twitter Likes [Real]', 10, 100000, 4.2200, 0, TRUE);

    -- LinkedIn Profile Followers - Dripfeed ID: 16136, Cost: $16.25/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '16136', 'LinkedIn Profile Followers [Real]', 10, 10000, 16.2500, 30, TRUE);

    -- LinkedIn Company Followers - Dripfeed ID: 16137, Cost: $16.25/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '16137', 'LinkedIn Company Followers [Real]', 10, 10000, 16.2500, 30, TRUE);

    -- LinkedIn Connections - Dripfeed ID: 16142, Cost: $18.75/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '16142', 'LinkedIn Connections [Real]', 10, 5000, 18.7500, 30, TRUE);

    -- LinkedIn Reposts - Dripfeed ID: 16141, Cost: $11.25/K
    INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active)
    VALUES (dripfeed_provider_id, '16141', 'LinkedIn Reposts [Real]', 10, 10000, 11.2500, 0, TRUE);

    RAISE NOTICE 'DripfeedPanel provider services created successfully';
END $$;

-- ============================================================================
-- 4. SERVICIOS PUBLICOS (services) - Lo que ve el usuario
-- ============================================================================

DO $$
DECLARE
    dripfeed_provider_id INTEGER;
    ps_instagram_followers INTEGER;
    ps_instagram_likes INTEGER;
    ps_tiktok_followers INTEGER;
    ps_tiktok_likes INTEGER;
    ps_tiktok_views INTEGER;
    ps_twitter_followers INTEGER;
    ps_twitter_likes INTEGER;
    ps_linkedin_profile_followers INTEGER;
    ps_linkedin_company_followers INTEGER;
    ps_linkedin_connections INTEGER;
    ps_linkedin_reposts INTEGER;

    -- Service type IDs
    st_instagram_followers INTEGER;
    st_instagram_likes INTEGER;
    st_tiktok_followers INTEGER;
    st_tiktok_likes INTEGER;
    st_tiktok_views INTEGER;
    st_twitter_followers INTEGER;
    st_twitter_likes INTEGER;
    st_linkedin_followers INTEGER;
    st_linkedin_company_followers INTEGER;
    st_linkedin_connections INTEGER;
    st_linkedin_reposts INTEGER;
BEGIN
    -- Obtener ID del proveedor
    SELECT id INTO dripfeed_provider_id FROM providers WHERE name = 'DripfeedPanel';

    -- Obtener IDs de provider_services
    SELECT id INTO ps_instagram_followers FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '13311';
    SELECT id INTO ps_instagram_likes FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '15856';
    SELECT id INTO ps_tiktok_followers FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '16132';
    SELECT id INTO ps_tiktok_likes FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '7169';
    SELECT id INTO ps_tiktok_views FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '8228';
    SELECT id INTO ps_twitter_followers FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '11811';
    SELECT id INTO ps_twitter_likes FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '3662';
    SELECT id INTO ps_linkedin_profile_followers FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '16136';
    SELECT id INTO ps_linkedin_company_followers FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '16137';
    SELECT id INTO ps_linkedin_connections FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '16142';
    SELECT id INTO ps_linkedin_reposts FROM provider_services WHERE provider_id = dripfeed_provider_id AND provider_service_id = '16141';

    -- Obtener IDs de service_types
    SELECT id INTO st_instagram_followers FROM service_types WHERE category_id = 1 AND slug = 'followers';
    SELECT id INTO st_instagram_likes FROM service_types WHERE category_id = 1 AND slug = 'likes';
    SELECT id INTO st_tiktok_followers FROM service_types WHERE category_id = 2 AND slug = 'followers';
    SELECT id INTO st_tiktok_likes FROM service_types WHERE category_id = 2 AND slug = 'likes';
    SELECT id INTO st_tiktok_views FROM service_types WHERE category_id = 2 AND slug = 'views';
    SELECT id INTO st_twitter_followers FROM service_types WHERE category_id = 4 AND slug = 'followers';
    SELECT id INTO st_twitter_likes FROM service_types WHERE category_id = 4 AND slug = 'likes';
    SELECT id INTO st_linkedin_followers FROM service_types WHERE category_id = 6 AND slug = 'followers';
    SELECT id INTO st_linkedin_company_followers FROM service_types WHERE category_id = 6 AND slug = 'company-followers';
    SELECT id INTO st_linkedin_connections FROM service_types WHERE category_id = 6 AND slug = 'connections';
    SELECT id INTO st_linkedin_reposts FROM service_types WHERE category_id = 6 AND slug = 'reposts';

    -- ========================================================================
    -- PUBLIC SERVICES (11 servicios)
    -- ========================================================================

    -- Instagram Followers - Sell: $1.99/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (1, st_instagram_followers, ps_instagram_followers,
            'Instagram Followers - Real',
            'High-quality real Instagram followers with profile pictures. 30-day refill guarantee.',
            'high', 'medium', 10, 300000, 1.99, 30, '1-24 hours', TRUE, 10);

    -- Instagram Likes - Sell: $0.49/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (1, st_instagram_likes, ps_instagram_likes,
            'Instagram Likes - Real',
            'Real Instagram likes from active accounts. Non-drop quality.',
            'high', 'fast', 10, 300000, 0.49, 0, '0-2 hours', TRUE, 10);

    -- TikTok Followers - Sell: $2.99/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (2, st_tiktok_followers, ps_tiktok_followers,
            'TikTok Followers - Real',
            'Real TikTok followers with profile pictures. 30-day refill guarantee.',
            'high', 'medium', 10, 100000, 2.99, 30, '1-24 hours', TRUE, 10);

    -- TikTok Likes - Sell: $0.33/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (2, st_tiktok_likes, ps_tiktok_likes,
            'TikTok Likes - Real',
            'Real TikTok likes from active users. Fast delivery.',
            'high', 'fast', 10, 100000, 0.33, 0, '0-1 hours', TRUE, 10);

    -- TikTok Views - Sell: $0.05/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (2, st_tiktok_views, ps_tiktok_views,
            'TikTok Views - Instant',
            'Instant TikTok views. Start within seconds.',
            'medium', 'instant', 100, 10000000, 0.05, 0, '0-5 minutes', TRUE, 10);

    -- Twitter Followers - Sell: $15.00/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (4, st_twitter_followers, ps_twitter_followers,
            'Twitter Followers - Real',
            'Premium Twitter followers with avatars and tweets. 30-day refill guarantee.',
            'premium', 'slow', 10, 100000, 15.00, 30, '24-72 hours', TRUE, 10);

    -- Twitter Likes - Sell: $4.99/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (4, st_twitter_likes, ps_twitter_likes,
            'Twitter Likes - Real',
            'Real Twitter likes from active accounts.',
            'high', 'medium', 10, 100000, 4.99, 0, '1-12 hours', TRUE, 10);

    -- LinkedIn Profile Followers - Sell: $19.99/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (6, st_linkedin_followers, ps_linkedin_profile_followers,
            'LinkedIn Profile Followers - Real',
            'Real LinkedIn followers for your personal profile. 30-day refill guarantee.',
            'premium', 'slow', 10, 10000, 19.99, 30, '24-72 hours', TRUE, 10);

    -- LinkedIn Company Followers - Sell: $19.99/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (6, st_linkedin_company_followers, ps_linkedin_company_followers,
            'LinkedIn Company Followers - Real',
            'Real LinkedIn followers for your company page. 30-day refill guarantee.',
            'premium', 'slow', 10, 10000, 19.99, 30, '24-72 hours', TRUE, 10);

    -- LinkedIn Connections - Sell: $25.00/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (6, st_linkedin_connections, ps_linkedin_connections,
            'LinkedIn Connections - Real',
            'Real LinkedIn connections. Expand your professional network. 30-day refill.',
            'premium', 'slow', 10, 5000, 25.00, 30, '24-72 hours', TRUE, 10);

    -- LinkedIn Reposts - Sell: $15.00/K
    INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order)
    VALUES (6, st_linkedin_reposts, ps_linkedin_reposts,
            'LinkedIn Reposts - Real',
            'Real LinkedIn reposts from active professional accounts.',
            'high', 'medium', 10, 10000, 15.00, 0, '1-24 hours', TRUE, 10);

    RAISE NOTICE 'DripfeedPanel public services created successfully';
END $$;

-- ============================================================================
-- VERIFICACION DE DATOS
-- ============================================================================

DO $$
DECLARE
    provider_count INTEGER;
    provider_services_count INTEGER;
    public_services_count INTEGER;
    dripfeed_provider_id INTEGER;
BEGIN
    SELECT id INTO dripfeed_provider_id FROM providers WHERE name = 'DripfeedPanel';
    SELECT COUNT(*) INTO provider_count FROM providers WHERE name = 'DripfeedPanel';
    SELECT COUNT(*) INTO provider_services_count FROM provider_services WHERE provider_id = dripfeed_provider_id;
    SELECT COUNT(*) INTO public_services_count FROM services ps
        JOIN provider_services prs ON ps.provider_service_id = prs.id
        WHERE prs.provider_id = dripfeed_provider_id;

    RAISE NOTICE '============================================';
    RAISE NOTICE 'DRIPFEED PANEL SETUP COMPLETED';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Provider created: %', provider_count;
    RAISE NOTICE 'Provider services: %', provider_services_count;
    RAISE NOTICE 'Public services: %', public_services_count;
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Services by platform:';
    RAISE NOTICE '  - Instagram: Followers, Likes';
    RAISE NOTICE '  - TikTok: Followers, Likes, Views';
    RAISE NOTICE '  - Twitter: Followers, Likes';
    RAISE NOTICE '  - LinkedIn: Profile Followers, Company Followers, Connections, Reposts';
    RAISE NOTICE '============================================';
END $$;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
