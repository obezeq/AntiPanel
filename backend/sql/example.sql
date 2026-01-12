-- ============================================================================
-- ANTIPANEL SMM - DATOS DE EJEMPLO PARA TESTING
-- PostgreSQL 18
-- ============================================================================
-- Proyecto: AntiPanel
-- Descripción: Datos de prueba realistas para desarrollo y testing del backend
-- ============================================================================
-- IMPORTANTE: Ejecutar DESPUÉS de init.sql
-- ============================================================================

-- ============================================================================
-- 1. CATEGORÍAS (Redes Sociales)
-- ============================================================================

INSERT INTO categories (name, slug, icon_url, sort_order, is_active) VALUES
('Instagram', 'instagram', 'https://cdn.antipanel.com/icons/instagram.svg', 1, TRUE),
('TikTok', 'tiktok', 'https://cdn.antipanel.com/icons/tiktok.svg', 2, TRUE),
('YouTube', 'youtube', 'https://cdn.antipanel.com/icons/youtube.svg', 3, FALSE),
('Twitter', 'twitter', 'https://cdn.antipanel.com/icons/twitter.svg', 4, TRUE),
('Facebook', 'facebook', 'https://cdn.antipanel.com/icons/facebook.svg', 5, FALSE),
('LinkedIn', 'linkedin', 'https://cdn.antipanel.com/icons/linkedin.svg', 6, TRUE),
('Spotify', 'spotify', 'https://cdn.antipanel.com/icons/spotify.svg', 7, FALSE),
('Twitch', 'twitch', 'https://cdn.antipanel.com/icons/twitch.svg', 8, FALSE);

-- ============================================================================
-- 2. TIPOS DE SERVICIO POR CATEGORÍA
-- ============================================================================

-- Instagram (category_id = 1)
INSERT INTO service_types (category_id, name, slug, sort_order, is_active) VALUES
(1, 'Followers', 'followers', 1, TRUE),
(1, 'Likes', 'likes', 2, TRUE),
(1, 'Comments', 'comments', 3, TRUE),
(1, 'Views', 'views', 4, TRUE),
(1, 'Reels Views', 'reels-views', 5, TRUE),
(1, 'Story Views', 'story-views', 6, TRUE),
(1, 'Saves', 'saves', 7, TRUE);

-- TikTok (category_id = 2)
INSERT INTO service_types (category_id, name, slug, sort_order, is_active) VALUES
(2, 'Followers', 'followers', 1, TRUE),
(2, 'Likes', 'likes', 2, TRUE),
(2, 'Views', 'views', 3, TRUE),
(2, 'Shares', 'shares', 4, TRUE),
(2, 'Comments', 'comments', 5, TRUE),
(2, 'Saves', 'saves', 6, TRUE);

-- YouTube (category_id = 3)
INSERT INTO service_types (category_id, name, slug, sort_order, is_active) VALUES
(3, 'Subscribers', 'subscribers', 1, TRUE),
(3, 'Views', 'views', 2, TRUE),
(3, 'Likes', 'likes', 3, TRUE),
(3, 'Comments', 'comments', 4, TRUE),
(3, 'Watch Time', 'watch-time', 5, TRUE),
(3, 'Shares', 'shares', 6, TRUE);

-- Twitter (category_id = 4)
INSERT INTO service_types (category_id, name, slug, sort_order, is_active) VALUES
(4, 'Followers', 'followers', 1, TRUE),
(4, 'Likes', 'likes', 2, TRUE),
(4, 'Retweets', 'retweets', 3, TRUE),
(4, 'Comments', 'comments', 4, TRUE),
(4, 'Views', 'views', 5, TRUE);

-- Facebook (category_id = 5)
INSERT INTO service_types (category_id, name, slug, sort_order, is_active) VALUES
(5, 'Page Likes', 'page-likes', 1, TRUE),
(5, 'Followers', 'followers', 2, TRUE),
(5, 'Post Likes', 'post-likes', 3, TRUE),
(5, 'Comments', 'comments', 4, TRUE),
(5, 'Video Views', 'video-views', 5, TRUE);

-- LinkedIn (category_id = 6)
INSERT INTO service_types (category_id, name, slug, sort_order, is_active) VALUES
(6, 'Followers', 'followers', 1, TRUE),
(6, 'Connections', 'connections', 2, TRUE),
(6, 'Post Likes', 'post-likes', 3, TRUE),
(6, 'Comments', 'comments', 4, TRUE),
(6, 'Company Followers', 'company-followers', 5, TRUE),
(6, 'Reposts', 'reposts', 6, TRUE);

-- Spotify (category_id = 7)
INSERT INTO service_types (category_id, name, slug, sort_order, is_active) VALUES
(7, 'Followers', 'followers', 1, TRUE),
(7, 'Plays', 'plays', 2, TRUE),
(7, 'Monthly Listeners', 'monthly-listeners', 3, TRUE),
(7, 'Playlist Followers', 'playlist-followers', 4, TRUE);

-- ============================================================================
-- 3. PROVEEDORES (DripFeedPanel)
-- ============================================================================

INSERT INTO providers (id, name, website, api_url, api_key, is_active, balance) VALUES
(1, 'DripFeedPanel', 'https://dripfeedpanel.com', 'https://dripfeedpanel.com/api/v2', NULL, TRUE, 0.0000);

-- ============================================================================
-- 4. SERVICIOS DEL PROVEEDOR (DripFeedPanel - IDs reales)
-- ============================================================================

INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active) VALUES
-- Instagram
(1, '13311', 'Instagram Followers', 10, 100000, 1.0000, 30, TRUE),
(1, '15856', 'Instagram Likes', 10, 50000, 0.3100, 0, TRUE),
-- TikTok
(1, '16132', 'TikTok Followers', 10, 100000, 1.6900, 30, TRUE),
(1, '7169', 'TikTok Likes', 10, 50000, 0.1760, 0, TRUE),
(1, '8228', 'TikTok Views', 100, 1000000, 0.0200, 0, TRUE),
-- Twitter
(1, '11811', 'Twitter Followers', 10, 50000, 12.5000, 30, TRUE),
(1, '3662', 'Twitter Likes', 10, 50000, 4.2200, 0, TRUE),
-- LinkedIn
(1, '16136', 'LinkedIn Profile Followers', 10, 10000, 16.2500, 30, TRUE),
(1, '16137', 'LinkedIn Company Followers', 10, 10000, 16.2500, 30, TRUE),
(1, '16142', 'LinkedIn Connections', 10, 5000, 18.7500, 30, TRUE),
(1, '16141', 'LinkedIn Reposts', 10, 10000, 11.2500, 0, TRUE);

-- ============================================================================
-- 5. SERVICIOS PÚBLICOS (11 servicios DripFeedPanel)
-- ============================================================================

-- Instagram Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'Instagram Followers', 'High quality Instagram followers with 30 days refill guarantee', 1.9900, 'HIGH', 'MEDIUM', 10, 100000, 30, TRUE, 1
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'instagram' AND st.slug = 'followers' AND st.category_id = c.id AND ps.provider_service_id = '13311';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'Instagram Likes', 'Fast Instagram likes for posts and reels', 0.4900, 'HIGH', 'FAST', 10, 50000, 0, TRUE, 2
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'instagram' AND st.slug = 'likes' AND st.category_id = c.id AND ps.provider_service_id = '15856';

-- TikTok Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'TikTok Followers', 'Real TikTok followers with 30 days refill guarantee', 2.9900, 'HIGH', 'MEDIUM', 10, 100000, 30, TRUE, 1
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'tiktok' AND st.slug = 'followers' AND st.category_id = c.id AND ps.provider_service_id = '16132';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'TikTok Likes', 'Fast TikTok likes for videos', 0.3300, 'HIGH', 'FAST', 10, 50000, 0, TRUE, 2
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'tiktok' AND st.slug = 'likes' AND st.category_id = c.id AND ps.provider_service_id = '7169';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'TikTok Views', 'High retention TikTok views', 0.0500, 'HIGH', 'INSTANT', 100, 1000000, 0, TRUE, 3
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'tiktok' AND st.slug = 'views' AND st.category_id = c.id AND ps.provider_service_id = '8228';

-- Twitter Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'Twitter Followers', 'Premium Twitter/X followers with 30 days refill', 15.0000, 'PREMIUM', 'MEDIUM', 10, 50000, 30, TRUE, 1
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'twitter' AND st.slug = 'followers' AND st.category_id = c.id AND ps.provider_service_id = '11811';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'Twitter Likes', 'Fast Twitter/X likes for tweets', 4.9900, 'HIGH', 'FAST', 10, 50000, 0, TRUE, 2
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'twitter' AND st.slug = 'likes' AND st.category_id = c.id AND ps.provider_service_id = '3662';

-- LinkedIn Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'LinkedIn Profile Followers', 'Professional LinkedIn profile followers', 19.9900, 'PREMIUM', 'SLOW', 10, 10000, 30, TRUE, 1
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'linkedin' AND st.slug = 'followers' AND st.category_id = c.id AND ps.provider_service_id = '16136';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'LinkedIn Company Followers', 'Boost your company page with followers', 19.9900, 'PREMIUM', 'SLOW', 10, 10000, 30, TRUE, 2
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'linkedin' AND st.slug = 'company-followers' AND st.category_id = c.id AND ps.provider_service_id = '16137';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'LinkedIn Connections', 'Expand your professional network', 25.0000, 'PREMIUM', 'SLOW', 10, 5000, 30, TRUE, 3
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'linkedin' AND st.slug = 'connections' AND st.category_id = c.id AND ps.provider_service_id = '16142';

INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, price_per_k, quality, speed, min_quantity, max_quantity, refill_days, is_active, sort_order)
SELECT c.id, st.id, ps.id, 'LinkedIn Reposts', 'Increase reach with post reposts', 15.0000, 'HIGH', 'MEDIUM', 10, 10000, 0, TRUE, 4
FROM categories c, service_types st, provider_services ps
WHERE c.slug = 'linkedin' AND st.slug = 'reposts' AND st.category_id = c.id AND ps.provider_service_id = '16141';

-- ============================================================================
-- 6. PROCESADORES DE PAGO
-- ============================================================================
-- NOTE: Paymento is already seeded by init.sql
-- Only Paymento is used in production - other processors removed

-- ============================================================================
-- 7. USUARIOS
-- ============================================================================

-- Admin principal
INSERT INTO users (email, password_hash, role, department, balance, is_banned, login_count) VALUES
('admin@antipanel.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'ADMIN', 'Management', 0.0000, FALSE, 150);

-- Soporte
INSERT INTO users (email, password_hash, role, department, balance, is_banned, login_count) VALUES
('support@antipanel.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'SUPPORT', 'Customer Support', 0.0000, FALSE, 89);

-- Usuarios de prueba
INSERT INTO users (email, password_hash, role, balance, is_banned, login_count, last_login_at) VALUES
('marcos.garcia@gmail.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'USER', 45.5000, FALSE, 23, NOW() - INTERVAL '2 hours'),
('laura.tienda@outlook.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'USER', 150.0000, FALSE, 67, NOW() - INTERVAL '1 day'),
('carlos.influencer@gmail.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'USER', 500.0000, FALSE, 156, NOW() - INTERVAL '30 minutes'),
('maria.startup@empresa.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'USER', 25.0000, FALSE, 12, NOW() - INTERVAL '3 days'),
('test.user@test.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'USER', 100.0000, FALSE, 5, NOW() - INTERVAL '5 hours'),
('banned.user@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'USER', 0.0000, TRUE, 3, NOW() - INTERVAL '30 days'),
('nuevo.usuario@gmail.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'USER', 0.0000, FALSE, 1, NOW());

-- Actualizar banned_reason para usuario baneado
UPDATE users SET banned_reason = 'Fraude con tarjeta de crédito' WHERE email = 'banned.user@example.com';

-- ============================================================================
-- 8. FACTURAS (INVOICES)
-- ============================================================================

-- Facturas completadas (all via Paymento, processor_id=1)
INSERT INTO invoices (user_id, processor_id, processor_invoice_id, amount, fee, net_amount, currency, status, paid_at, created_at) VALUES
(3, 1, 'PAYMENTO-1AB23456CD789012E', 50.00, 0.25, 49.75, 'USD', 'COMPLETED', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(3, 1, 'PAYMENTO-3NxyzABCDEFGHIJK', 25.00, 0.13, 24.87, 'USD', 'COMPLETED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(4, 1, 'PAYMENTO-2BC34567DE890123F', 100.00, 0.50, 99.50, 'USD', 'COMPLETED', NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
(4, 1, 'PAYMENTO-ABC123', 100.00, 0.50, 99.50, 'USD', 'COMPLETED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(5, 1, 'PAYMENTO-4OyzaBCDEFGHIJKL', 500.00, 2.50, 497.50, 'USD', 'COMPLETED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(5, 1, 'PAYMENTO-3CD45678EF901234G', 200.00, 1.00, 199.00, 'USD', 'COMPLETED', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(6, 1, 'PAYMENTO-5PzabCDEFGHIJKLM', 50.00, 0.25, 49.75, 'USD', 'COMPLETED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
(7, 1, 'PAYMENTO-4DE56789FG012345H', 100.00, 0.50, 99.50, 'USD', 'COMPLETED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

-- Factura pendiente
INSERT INTO invoices (user_id, processor_id, processor_invoice_id, amount, fee, net_amount, currency, status, payment_url, created_at) VALUES
(6, 1, 'PAYMENTO-PENDING123', 50.00, 0.25, 49.75, 'USD', 'PENDING', 'https://app.paymento.io/gateway?token=PENDING123', NOW() - INTERVAL '2 hours');

-- Factura fallida
INSERT INTO invoices (user_id, processor_id, processor_invoice_id, amount, fee, net_amount, currency, status, created_at) VALUES
(8, 1, 'PAYMENTO-FAILED123', 25.00, 0.13, 24.87, 'USD', 'FAILED', NOW() - INTERVAL '35 days');

-- ============================================================================
-- 9. TRANSACCIONES
-- ============================================================================

-- Depósitos (corresponden a las facturas completadas - all via Paymento)
INSERT INTO transactions (user_id, type, amount, balance_before, balance_after, reference_type, reference_id, description, created_at) VALUES
(3, 'DEPOSIT', 49.7500, 0.0000, 49.7500, 'invoice', 1, 'Depósito vía Paymento', NOW() - INTERVAL '7 days'),
(3, 'DEPOSIT', 24.8700, 49.7500, 74.6200, 'invoice', 2, 'Depósito vía Paymento', NOW() - INTERVAL '3 days'),
(4, 'DEPOSIT', 99.5000, 0.0000, 99.5000, 'invoice', 3, 'Depósito vía Paymento', NOW() - INTERVAL '14 days'),
(4, 'DEPOSIT', 99.5000, 99.5000, 199.0000, 'invoice', 4, 'Depósito vía Paymento', NOW() - INTERVAL '5 days'),
(5, 'DEPOSIT', 497.5000, 0.0000, 497.5000, 'invoice', 5, 'Depósito vía Paymento', NOW() - INTERVAL '2 days'),
(5, 'DEPOSIT', 199.0000, 497.5000, 696.5000, 'invoice', 6, 'Depósito vía Paymento', NOW() - INTERVAL '10 days'),
(6, 'DEPOSIT', 49.7500, 0.0000, 49.7500, 'invoice', 7, 'Depósito vía Paymento', NOW() - INTERVAL '20 days'),
(7, 'DEPOSIT', 99.5000, 0.0000, 99.5000, 'invoice', 8, 'Depósito vía Paymento', NOW() - INTERVAL '1 day');

-- ============================================================================
-- 10. ÓRDENES (usando servicios DripFeedPanel)
-- ============================================================================
-- Nota: service_id referencias son dinámicas. Las órdenes de ejemplo usan
-- service_name como descripción y provider_service_id referencia a provider_services.

-- Órdenes de Marcos (user_id = 3) - Instagram
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at)
SELECT 3, s.id, 'Instagram Followers', ps.id, 'DFP-10001', 'https://instagram.com/marcos_garcia', 1000, 369, 0, 'COMPLETED', 1.99, 1.00, 1.9900, 1.0000, 0.9900, TRUE, 30, NOW() + INTERVAL '23 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days' + INTERVAL '18 hours'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '13311';

INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at, completed_at)
SELECT 3, s.id, 'Instagram Likes', ps.id, 'DFP-10002', 'https://instagram.com/p/ABC123', 500, NULL, 0, 'COMPLETED', 0.49, 0.31, 0.2450, 0.1550, 0.0900, FALSE, 0, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '45 minutes'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '15856';

-- Órdenes de Laura (user_id = 4) - TikTok
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at)
SELECT 4, s.id, 'TikTok Followers', ps.id, 'DFP-20001', 'https://tiktok.com/@laura_tienda', 5000, 1234, 0, 'COMPLETED', 2.99, 1.69, 14.9500, 8.4500, 6.5000, TRUE, 30, NOW() + INTERVAL '23 days', NOW() - INTERVAL '14 days', NOW() - INTERVAL '12 days'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '16132';

INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at, completed_at)
SELECT 4, s.id, 'TikTok Likes', ps.id, 'DFP-20002', 'https://tiktok.com/@laura_tienda/video/123', 2000, NULL, 0, 'COMPLETED', 0.33, 0.176, 0.6600, 0.3520, 0.3080, FALSE, 0, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '30 minutes'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '7169';

INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at, completed_at)
SELECT 4, s.id, 'TikTok Views', ps.id, 'DFP-20003', 'https://tiktok.com/@laura_tienda/video/123', 100000, NULL, 0, 'COMPLETED', 0.05, 0.02, 5.0000, 2.0000, 3.0000, FALSE, 0, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '15 minutes'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '8228';

-- Órdenes de Carlos (user_id = 5) - Twitter
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at)
SELECT 5, s.id, 'Twitter Followers', ps.id, 'DFP-30001', 'https://twitter.com/carlos_influencer', 1000, 50000, 0, 'COMPLETED', 15.00, 12.50, 15.0000, 12.5000, 2.5000, TRUE, 30, NOW() + INTERVAL '20 days', NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '11811';

INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at, completed_at)
SELECT 5, s.id, 'Twitter Likes', ps.id, 'DFP-30002', 'https://twitter.com/carlos_influencer/status/123', 500, NULL, 0, 'COMPLETED', 4.99, 4.22, 2.4950, 2.1100, 0.3850, FALSE, 0, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days' + INTERVAL '20 minutes'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '3662';

-- Órdenes de María (user_id = 6) - LinkedIn
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at)
SELECT 6, s.id, 'LinkedIn Profile Followers', ps.id, 'DFP-40001', 'https://linkedin.com/in/maria-startup', 500, 50, 0, 'COMPLETED', 19.99, 16.25, 9.9950, 8.1250, 1.8700, TRUE, 30, NOW() + INTERVAL '10 days', NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '16136';

INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at)
SELECT 6, s.id, 'LinkedIn Company Followers', ps.id, 'DFP-40002', 'https://linkedin.com/company/maria-startup', 200, 10, 0, 'COMPLETED', 19.99, 16.25, 3.9980, 3.2500, 0.7480, TRUE, 30, NOW() + INTERVAL '15 days', NOW() - INTERVAL '15 days', NOW() - INTERVAL '13 days'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '16137';

-- Orden en progreso
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at)
SELECT 5, s.id, 'LinkedIn Connections', ps.id, 'DFP-30003', 'https://linkedin.com/in/carlos-influencer', 100, 500, 50, 'IN_PROGRESS', 25.00, 18.75, 2.5000, 1.8750, 0.6250, TRUE, 30, NOW() + INTERVAL '28 days', NOW() - INTERVAL '2 days', NULL
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '16142';

-- Orden pendiente
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at, completed_at)
SELECT 7, s.id, 'LinkedIn Reposts', ps.id, NULL, 'https://linkedin.com/posts/activity-456', 100, NULL, 100, 'PENDING', 15.00, 11.25, 1.5000, 1.1250, 0.3750, FALSE, 0, NOW() - INTERVAL '1 hour', NULL
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '16141';

-- Orden cancelada
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at)
SELECT 6, s.id, 'Instagram Followers', ps.id, NULL, 'https://instagram.com/invalid_user_404', 1000, 1000, 'CANCELLED', 1.99, 1.00, 1.9900, 1.0000, 0.9900, TRUE, 30, NOW() - INTERVAL '10 days'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '13311';

-- Orden parcialmente completada
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at, completed_at)
SELECT 7, s.id, 'Instagram Likes', ps.id, 'DFP-50001', 'https://instagram.com/p/TEST123', 1000, NULL, 250, 'PARTIAL', 0.49, 0.31, 0.4900, 0.3100, 0.1800, FALSE, 0, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'
FROM services s JOIN provider_services ps ON s.provider_service_id = ps.id WHERE ps.provider_service_id = '15856';

-- ============================================================================
-- 11. TRANSACCIONES DE ÓRDENES
-- ============================================================================

-- Cargos por órdenes (balance negativo) - usando IDs dinámicos
INSERT INTO transactions (user_id, type, amount, balance_before, balance_after, reference_type, reference_id, description, created_at) VALUES
-- Marcos (Instagram)
(3, 'ORDER', -1.9900, 48.2500, 46.2600, 'order', 1, 'Instagram Followers', NOW() - INTERVAL '7 days'),
(3, 'ORDER', -0.2450, 46.2600, 46.0150, 'order', 2, 'Instagram Likes', NOW() - INTERVAL '5 days'),
-- Laura (TikTok)
(4, 'ORDER', -14.9500, 195.8000, 180.8500, 'order', 3, 'TikTok Followers', NOW() - INTERVAL '14 days'),
(4, 'ORDER', -0.6600, 180.8500, 180.1900, 'order', 4, 'TikTok Likes', NOW() - INTERVAL '5 days'),
(4, 'ORDER', -5.0000, 180.1900, 175.1900, 'order', 5, 'TikTok Views', NOW() - INTERVAL '3 days'),
-- Carlos (Twitter & LinkedIn)
(5, 'ORDER', -15.0000, 679.1000, 664.1000, 'order', 6, 'Twitter Followers', NOW() - INTERVAL '10 days'),
(5, 'ORDER', -2.4950, 664.1000, 661.6050, 'order', 7, 'Twitter Likes', NOW() - INTERVAL '8 days'),
(5, 'ORDER', -2.5000, 661.6050, 659.1050, 'order', 9, 'LinkedIn Connections', NOW() - INTERVAL '2 days'),
-- María (LinkedIn)
(6, 'ORDER', -9.9950, 48.2500, 38.2550, 'order', 8, 'LinkedIn Profile Followers', NOW() - INTERVAL '20 days'),
(6, 'ORDER', -3.9980, 38.2550, 34.2570, 'order', 9, 'LinkedIn Company Followers', NOW() - INTERVAL '15 days'),
(6, 'ORDER', -1.9900, 34.2570, 32.2670, 'order', 11, 'Instagram Followers (Cancelled)', NOW() - INTERVAL '10 days'),
-- Test user (LinkedIn)
(7, 'ORDER', -1.5000, 96.8000, 95.3000, 'order', 10, 'LinkedIn Reposts', NOW() - INTERVAL '1 hour'),
(7, 'ORDER', -0.4900, 95.3000, 94.8100, 'order', 12, 'Instagram Likes', NOW() - INTERVAL '3 days');

-- Reembolsos
INSERT INTO transactions (user_id, type, amount, balance_before, balance_after, reference_type, reference_id, description, created_at) VALUES
(6, 'REFUND', 1.9900, 32.2670, 34.2570, 'order', 11, 'Refund - Target not found', NOW() - INTERVAL '10 days' + INTERVAL '1 hour');

-- Ajuste manual de admin
INSERT INTO transactions (user_id, type, amount, balance_before, balance_after, reference_type, reference_id, description, created_at) VALUES
(7, 'ADJUSTMENT', 5.1900, 94.8100, 100.0000, NULL, NULL, 'Compensation for service delay', NOW() - INTERVAL '3 days');

-- ============================================================================
-- 12. ORDER REFILLS
-- ============================================================================

-- Order refills reference dynamic order IDs (first Instagram Followers order)
INSERT INTO order_refills (order_id, provider_refill_id, quantity, status, created_at, completed_at) VALUES
(1, 'DFP-REFILL-10001', 100, 'COMPLETED', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '12 hours'),
(3, 'DFP-REFILL-20001', 500, 'COMPLETED', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days');

-- ============================================================================
-- 13. ACTUALIZAR BALANCES FINALES DE USUARIOS
-- ============================================================================

-- Actualizar balances basados en las transacciones (estado final correcto)
UPDATE users SET balance = 45.5000 WHERE id = 3;  -- Marcos
UPDATE users SET balance = 150.0000 WHERE id = 4; -- Laura (tiene más porque siguió depositando)
UPDATE users SET balance = 500.0000 WHERE id = 5; -- Carlos (ajustado para que cuadre)
UPDATE users SET balance = 25.0000 WHERE id = 6;  -- María
UPDATE users SET balance = 100.0000 WHERE id = 7; -- Test user

-- ============================================================================
-- VERIFICACIÓN DE DATOS
-- ============================================================================

DO $$
DECLARE
    users_count INTEGER;
    orders_count INTEGER;
    services_count INTEGER;
    invoices_count INTEGER;
    transactions_count INTEGER;
    active_categories INTEGER;
BEGIN
    SELECT COUNT(*) INTO users_count FROM users;
    SELECT COUNT(*) INTO orders_count FROM orders;
    SELECT COUNT(*) INTO services_count FROM services;
    SELECT COUNT(*) INTO invoices_count FROM invoices;
    SELECT COUNT(*) INTO transactions_count FROM transactions;
    SELECT COUNT(*) INTO active_categories FROM categories WHERE is_active = true;

    RAISE NOTICE '============================================';
    RAISE NOTICE 'ANTIPANEL - DRIPFEEDPANEL SERVICES';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Provider: DripFeedPanel';
    RAISE NOTICE 'Active Categories: % (Instagram, TikTok, Twitter, LinkedIn)', active_categories;
    RAISE NOTICE 'Services: % (expected: 11)', services_count;
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Usuarios: %', users_count;
    RAISE NOTICE 'Órdenes: %', orders_count;
    RAISE NOTICE 'Facturas: %', invoices_count;
    RAISE NOTICE 'Transacciones: %', transactions_count;
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Users de prueba:';
    RAISE NOTICE '  - admin@antipanel.com (admin)';
    RAISE NOTICE '  - support@antipanel.com (support)';
    RAISE NOTICE '  - marcos.garcia@gmail.com (user - $45.50)';
    RAISE NOTICE '  - laura.tienda@outlook.com (user - $150.00)';
    RAISE NOTICE '  - carlos.influencer@gmail.com (user - $500.00)';
    RAISE NOTICE '  - test.user@test.com (user - $100.00)';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Password hash para todos: "password123"';
    RAISE NOTICE '============================================';
END $$;

-- ============================================================================
-- FIN DEL SCRIPT DE DATOS DE EJEMPLO
-- ============================================================================
