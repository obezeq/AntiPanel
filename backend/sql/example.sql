-- ============================================================================
-- ANTIPANEL SMM - DATOS DE EJEMPLO PARA TESTING
-- PostgreSQL 18
-- ============================================================================
-- Autor: Ezequiel Ortega Bravo
-- Proyecto: AntiPanel - 2º DAW
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
('YouTube', 'youtube', 'https://cdn.antipanel.com/icons/youtube.svg', 3, TRUE),
('Twitter', 'twitter', 'https://cdn.antipanel.com/icons/twitter.svg', 4, TRUE),
('Facebook', 'facebook', 'https://cdn.antipanel.com/icons/facebook.svg', 5, TRUE),
('LinkedIn', 'linkedin', 'https://cdn.antipanel.com/icons/linkedin.svg', 6, TRUE),
('Spotify', 'spotify', 'https://cdn.antipanel.com/icons/spotify.svg', 7, TRUE),
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
(6, 'Comments', 'comments', 4, TRUE);

-- Spotify (category_id = 7)
INSERT INTO service_types (category_id, name, slug, sort_order, is_active) VALUES
(7, 'Followers', 'followers', 1, TRUE),
(7, 'Plays', 'plays', 2, TRUE),
(7, 'Monthly Listeners', 'monthly-listeners', 3, TRUE),
(7, 'Playlist Followers', 'playlist-followers', 4, TRUE);

-- ============================================================================
-- 3. PROVEEDORES
-- ============================================================================

INSERT INTO providers (name, website, api_url, api_key, is_active, balance) VALUES
('SMMKings', 'https://smmkings.com', 'https://smmkings.com/api/v2', 'sk_live_abc123def456', TRUE, 500.0000),
('PerfectPanel', 'https://perfectpanel.com', 'https://perfectpanel.com/api/v1', 'pp_key_xyz789', TRUE, 250.0000),
('SocialBoost', 'https://socialboost.io', 'https://api.socialboost.io/v3', 'sb_prod_key_111', TRUE, 1000.0000),
('FastSMM', 'https://fastsmm.net', 'https://fastsmm.net/api', 'fs_apikey_222', TRUE, 150.0000),
('PromoSMM', 'https://promosmm.com', 'https://promosmm.com/api/v2', 'promo_333_key', FALSE, 0.0000);

-- ============================================================================
-- 4. SERVICIOS DEL PROVEEDOR (provider_services)
-- ============================================================================

-- SMMKings (provider_id = 1) - Instagram
INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active) VALUES
(1, '1001', 'Instagram Followers - HQ Real', 100, 100000, 0.4500, 30, TRUE),
(1, '1002', 'Instagram Followers - Premium', 50, 50000, 0.9000, 90, TRUE),
(1, '1003', 'Instagram Followers - Bot/Cheap', 500, 500000, 0.1500, 0, TRUE),
(1, '1010', 'Instagram Likes - Real', 50, 50000, 0.2500, 0, TRUE),
(1, '1011', 'Instagram Likes - Premium HQ', 20, 20000, 0.5000, 30, TRUE),
(1, '1020', 'Instagram Comments - Custom', 10, 5000, 2.5000, 0, TRUE),
(1, '1021', 'Instagram Comments - Random', 10, 10000, 1.2000, 0, TRUE),
(1, '1030', 'Instagram Views - Fast', 100, 10000000, 0.0500, 0, TRUE),
(1, '1031', 'Instagram Reels Views', 100, 5000000, 0.0800, 0, TRUE);

-- SMMKings (provider_id = 1) - TikTok
INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active) VALUES
(1, '2001', 'TikTok Followers - Real', 100, 100000, 0.5500, 30, TRUE),
(1, '2002', 'TikTok Followers - Premium', 50, 50000, 1.2000, 60, TRUE),
(1, '2010', 'TikTok Likes - Fast', 50, 100000, 0.2000, 0, TRUE),
(1, '2020', 'TikTok Views - Instant', 500, 50000000, 0.0200, 0, TRUE),
(1, '2021', 'TikTok Views - Retention', 500, 10000000, 0.0500, 0, TRUE);

-- PerfectPanel (provider_id = 2) - YouTube
INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active) VALUES
(2, '3001', 'YouTube Subscribers - Real', 100, 50000, 2.0000, 30, TRUE),
(2, '3002', 'YouTube Subscribers - Premium', 50, 10000, 5.0000, 90, TRUE),
(2, '3010', 'YouTube Views - Fast', 500, 1000000, 0.8000, 0, TRUE),
(2, '3011', 'YouTube Views - High Retention', 100, 100000, 2.5000, 0, TRUE),
(2, '3020', 'YouTube Likes - Real', 50, 50000, 1.0000, 0, TRUE),
(2, '3030', 'YouTube Comments - Custom', 10, 5000, 8.0000, 0, TRUE);

-- SocialBoost (provider_id = 3) - Twitter
INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active) VALUES
(3, '4001', 'Twitter Followers - HQ', 100, 100000, 0.6000, 30, TRUE),
(3, '4010', 'Twitter Likes - Fast', 50, 50000, 0.3000, 0, TRUE),
(3, '4020', 'Twitter Retweets', 50, 50000, 0.4000, 0, TRUE),
(3, '4030', 'Twitter Views', 500, 10000000, 0.0100, 0, TRUE);

-- SocialBoost (provider_id = 3) - Facebook
INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active) VALUES
(3, '5001', 'Facebook Page Likes', 100, 100000, 0.8000, 30, TRUE),
(3, '5010', 'Facebook Post Likes', 50, 50000, 0.2500, 0, TRUE),
(3, '5020', 'Facebook Video Views', 500, 10000000, 0.0300, 0, TRUE);

-- FastSMM (provider_id = 4) - LinkedIn & Spotify
INSERT INTO provider_services (provider_id, provider_service_id, name, min_quantity, max_quantity, cost_per_k, refill_days, is_active) VALUES
(4, '6001', 'LinkedIn Followers - Real', 100, 50000, 3.0000, 30, TRUE),
(4, '6010', 'LinkedIn Post Likes', 20, 10000, 1.5000, 0, TRUE),
(4, '7001', 'Spotify Followers', 100, 100000, 1.0000, 30, TRUE),
(4, '7010', 'Spotify Plays', 1000, 10000000, 0.5000, 0, TRUE),
(4, '7020', 'Spotify Monthly Listeners', 1000, 1000000, 2.0000, 0, TRUE);

-- ============================================================================
-- 5. SERVICIOS PÚBLICOS (services) - Lo que ve el usuario
-- ============================================================================

-- Instagram Followers
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(1, 1, 1, 'Instagram Followers - HQ', 'Seguidores de alta calidad con fotos de perfil reales. Ideal para cuentas personales y pequeños negocios.', 'high', 'medium', 100, 100000, 0.99, 30, '1-24 hours', TRUE, 1),
(1, 1, 2, 'Instagram Followers - Premium', 'Seguidores premium con perfiles completos, fotos y publicaciones. Máxima calidad disponible.', 'premium', 'slow', 50, 50000, 1.99, 90, '24-72 hours', TRUE, 2),
(1, 1, 3, 'Instagram Followers - Starter', 'Seguidores económicos para empezar. Sin garantía de refill.', 'low', 'fast', 500, 500000, 0.35, 0, '0-6 hours', TRUE, 3);

-- Instagram Likes
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(1, 2, 4, 'Instagram Likes - Real', 'Likes de cuentas reales. Entrega rápida y estable.', 'high', 'fast', 50, 50000, 0.55, 0, '0-1 hours', TRUE, 1),
(1, 2, 5, 'Instagram Likes - Premium', 'Likes de cuentas premium con engagement real. Incluye garantía.', 'premium', 'medium', 20, 20000, 1.10, 30, '1-12 hours', TRUE, 2);

-- Instagram Comments
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(1, 3, 6, 'Instagram Comments - Custom', 'Comentarios personalizados. Puedes elegir los textos que quieras.', 'premium', 'slow', 10, 5000, 5.50, 0, '1-24 hours', TRUE, 1),
(1, 3, 7, 'Instagram Comments - Random', 'Comentarios aleatorios positivos (emojis, felicitaciones, etc.).', 'medium', 'medium', 10, 10000, 2.65, 0, '0-6 hours', TRUE, 2);

-- Instagram Views
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(1, 4, 8, 'Instagram Views - Fast', 'Views rápidas para posts y videos. Entrega inmediata.', 'medium', 'instant', 100, 10000000, 0.12, 0, '0-30 minutes', TRUE, 1);

-- Instagram Reels Views
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(1, 5, 9, 'Instagram Reels Views', 'Views para Reels. Ayuda a impulsar el algoritmo.', 'high', 'fast', 100, 5000000, 0.18, 0, '0-1 hours', TRUE, 1);

-- TikTok Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(2, 8, 10, 'TikTok Followers - Real', 'Seguidores reales de TikTok con fotos de perfil.', 'high', 'medium', 100, 100000, 1.20, 30, '1-24 hours', TRUE, 1),
(2, 8, 11, 'TikTok Followers - Premium', 'Seguidores premium con videos propios y engagement activo.', 'premium', 'slow', 50, 50000, 2.50, 60, '24-72 hours', TRUE, 2),
(2, 9, 12, 'TikTok Likes - Fast', 'Likes rápidos para tus videos de TikTok.', 'high', 'instant', 50, 100000, 0.45, 0, '0-30 minutes', TRUE, 1),
(2, 10, 13, 'TikTok Views - Instant', 'Views instantáneas. Empieza a recibir en segundos.', 'medium', 'instant', 500, 50000000, 0.05, 0, '0-5 minutes', TRUE, 1),
(2, 10, 14, 'TikTok Views - High Retention', 'Views con alta retención. Mejor para el algoritmo.', 'high', 'fast', 500, 10000000, 0.12, 0, '0-1 hours', TRUE, 2);

-- YouTube Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(3, 14, 15, 'YouTube Subscribers - Real', 'Suscriptores reales con canales activos.', 'high', 'medium', 100, 50000, 4.50, 30, '1-48 hours', TRUE, 1),
(3, 14, 16, 'YouTube Subscribers - Premium', 'Suscriptores premium con historial de actividad.', 'premium', 'slow', 50, 10000, 11.00, 90, '48-96 hours', TRUE, 2),
(3, 15, 17, 'YouTube Views - Fast', 'Views rápidas para tu video.', 'medium', 'fast', 500, 1000000, 1.80, 0, '0-12 hours', TRUE, 1),
(3, 15, 18, 'YouTube Views - High Retention', 'Views con alta retención (60-80%). Mejor para monetización.', 'premium', 'medium', 100, 100000, 5.50, 0, '1-24 hours', TRUE, 2),
(3, 16, 19, 'YouTube Likes - Real', 'Likes de cuentas reales de YouTube.', 'high', 'medium', 50, 50000, 2.20, 0, '0-12 hours', TRUE, 1);

-- Twitter Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(4, 21, 21, 'Twitter Followers - HQ', 'Seguidores de alta calidad con avatares y tweets.', 'high', 'medium', 100, 100000, 1.30, 30, '1-24 hours', TRUE, 1),
(4, 22, 22, 'Twitter Likes - Fast', 'Likes rápidos para tus tweets.', 'high', 'instant', 50, 50000, 0.65, 0, '0-30 minutes', TRUE, 1),
(4, 23, 23, 'Twitter Retweets', 'Retweets de cuentas reales.', 'high', 'fast', 50, 50000, 0.85, 0, '0-2 hours', TRUE, 1),
(4, 25, 24, 'Twitter Views', 'Views para tus tweets y videos.', 'medium', 'instant', 500, 10000000, 0.03, 0, '0-15 minutes', TRUE, 1);

-- Facebook Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(5, 26, 25, 'Facebook Page Likes', 'Likes para tu página de Facebook.', 'high', 'medium', 100, 100000, 1.75, 30, '1-24 hours', TRUE, 1),
(5, 28, 26, 'Facebook Post Likes', 'Likes para tus publicaciones.', 'high', 'fast', 50, 50000, 0.55, 0, '0-2 hours', TRUE, 1),
(5, 30, 27, 'Facebook Video Views', 'Views para tus videos de Facebook.', 'medium', 'instant', 500, 10000000, 0.08, 0, '0-1 hours', TRUE, 1);

-- LinkedIn Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(6, 31, 28, 'LinkedIn Followers - Real', 'Seguidores profesionales reales para tu perfil o página.', 'premium', 'slow', 100, 50000, 6.50, 30, '24-72 hours', TRUE, 1),
(6, 33, 29, 'LinkedIn Post Likes', 'Likes profesionales para tus publicaciones.', 'high', 'medium', 20, 10000, 3.25, 0, '1-12 hours', TRUE, 1);

-- Spotify Services
INSERT INTO services (category_id, service_type_id, provider_service_id, name, description, quality, speed, min_quantity, max_quantity, price_per_k, refill_days, average_time, is_active, sort_order) VALUES
(7, 35, 30, 'Spotify Followers', 'Seguidores para tu perfil de artista o playlist.', 'high', 'medium', 100, 100000, 2.20, 30, '1-24 hours', TRUE, 1),
(7, 36, 31, 'Spotify Plays', 'Reproducciones para tus canciones.', 'high', 'fast', 1000, 10000000, 1.10, 0, '0-12 hours', TRUE, 1),
(7, 37, 32, 'Spotify Monthly Listeners', 'Oyentes mensuales para tu perfil de artista.', 'premium', 'slow', 1000, 1000000, 4.40, 0, '24-72 hours', TRUE, 2);

-- ============================================================================
-- 6. PROCESADORES DE PAGO
-- ============================================================================

INSERT INTO payment_processors (name, code, website, api_key, api_secret, min_amount, max_amount, fee_percentage, fee_fixed, is_active, sort_order) VALUES
('PayPal', 'paypal', 'https://paypal.com', 'paypal_client_id_xxx', 'paypal_secret_xxx', 5.00, 1000.00, 2.90, 0.30, TRUE, 1),
('Stripe', 'stripe', 'https://stripe.com', 'sk_live_xxx', 'whsec_xxx', 1.00, 5000.00, 2.90, 0.30, TRUE, 2),
('Coinbase Commerce', 'coinbase', 'https://commerce.coinbase.com', 'coinbase_api_key_xxx', NULL, 10.00, NULL, 1.00, 0.00, TRUE, 3),
('PayPal Friends', 'paypal-friends', 'https://paypal.com', NULL, NULL, 10.00, 500.00, 0.00, 0.00, TRUE, 4),
('Bank Transfer', 'bank-transfer', NULL, NULL, NULL, 50.00, NULL, 0.00, 0.00, FALSE, 5);

-- ============================================================================
-- 7. USUARIOS
-- ============================================================================

-- Admin principal
INSERT INTO users (email, password_hash, role, department, balance, is_banned, login_count) VALUES
('admin@antipanel.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'admin', 'Management', 0.0000, FALSE, 150);

-- Soporte
INSERT INTO users (email, password_hash, role, department, balance, is_banned, login_count) VALUES
('support@antipanel.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'support', 'Customer Support', 0.0000, FALSE, 89);

-- Usuarios de prueba
INSERT INTO users (email, password_hash, role, balance, is_banned, login_count, last_login_at) VALUES
('marcos.garcia@gmail.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'user', 45.5000, FALSE, 23, NOW() - INTERVAL '2 hours'),
('laura.tienda@outlook.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'user', 150.0000, FALSE, 67, NOW() - INTERVAL '1 day'),
('carlos.influencer@gmail.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'user', 500.0000, FALSE, 156, NOW() - INTERVAL '30 minutes'),
('maria.startup@empresa.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'user', 25.0000, FALSE, 12, NOW() - INTERVAL '3 days'),
('test.user@test.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'user', 100.0000, FALSE, 5, NOW() - INTERVAL '5 hours'),
('banned.user@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'user', 0.0000, TRUE, 3, NOW() - INTERVAL '30 days'),
('nuevo.usuario@gmail.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4P1VH9ggPJvXYvMK', 'user', 0.0000, FALSE, 1, NOW());

-- Actualizar banned_reason para usuario baneado
UPDATE users SET banned_reason = 'Fraude con tarjeta de crédito' WHERE email = 'banned.user@example.com';

-- ============================================================================
-- 8. FACTURAS (INVOICES)
-- ============================================================================

-- Facturas completadas
INSERT INTO invoices (user_id, processor_id, processor_invoice_id, amount, fee, net_amount, currency, status, paid_at, created_at) VALUES
(3, 1, 'PAY-1AB23456CD789012E', 50.00, 1.75, 48.25, 'USD', 'completed', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
(3, 2, 'pi_3NxyzABCDEFGHIJK', 25.00, 1.03, 23.97, 'USD', 'completed', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
(4, 1, 'PAY-2BC34567DE890123F', 100.00, 3.20, 96.80, 'USD', 'completed', NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
(4, 3, 'COINBASE-ABC123', 100.00, 1.00, 99.00, 'USD', 'completed', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
(5, 2, 'pi_4OyzaBCDEFGHIJKL', 500.00, 14.80, 485.20, 'USD', 'completed', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
(5, 1, 'PAY-3CD45678EF901234G', 200.00, 6.10, 193.90, 'USD', 'completed', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
(6, 2, 'pi_5PzabCDEFGHIJKLM', 50.00, 1.75, 48.25, 'USD', 'completed', NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
(7, 1, 'PAY-4DE56789FG012345H', 100.00, 3.20, 96.80, 'USD', 'completed', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

-- Factura pendiente
INSERT INTO invoices (user_id, processor_id, processor_invoice_id, amount, fee, net_amount, currency, status, payment_url, created_at) VALUES
(6, 3, 'COINBASE-PENDING123', 50.00, 0.50, 49.50, 'USD', 'pending', 'https://commerce.coinbase.com/charges/PENDING123', NOW() - INTERVAL '2 hours');

-- Factura fallida
INSERT INTO invoices (user_id, processor_id, processor_invoice_id, amount, fee, net_amount, currency, status, created_at) VALUES
(8, 2, 'pi_FAILED123', 25.00, 1.03, 23.97, 'USD', 'failed', NOW() - INTERVAL '35 days');

-- ============================================================================
-- 9. TRANSACCIONES
-- ============================================================================

-- Depósitos (corresponden a las facturas completadas)
INSERT INTO transactions (user_id, type, amount, balance_before, balance_after, reference_type, reference_id, description, created_at) VALUES
(3, 'deposit', 48.2500, 0.0000, 48.2500, 'invoice', 1, 'Depósito vía PayPal', NOW() - INTERVAL '7 days'),
(3, 'deposit', 23.9700, 48.2500, 72.2200, 'invoice', 2, 'Depósito vía Stripe', NOW() - INTERVAL '3 days'),
(4, 'deposit', 96.8000, 0.0000, 96.8000, 'invoice', 3, 'Depósito vía PayPal', NOW() - INTERVAL '14 days'),
(4, 'deposit', 99.0000, 96.8000, 195.8000, 'invoice', 4, 'Depósito vía Coinbase', NOW() - INTERVAL '5 days'),
(5, 'deposit', 485.2000, 0.0000, 485.2000, 'invoice', 5, 'Depósito vía Stripe', NOW() - INTERVAL '2 days'),
(5, 'deposit', 193.9000, 485.2000, 679.1000, 'invoice', 6, 'Depósito vía PayPal', NOW() - INTERVAL '10 days'),
(6, 'deposit', 48.2500, 0.0000, 48.2500, 'invoice', 7, 'Depósito vía Stripe', NOW() - INTERVAL '20 days'),
(7, 'deposit', 96.8000, 0.0000, 96.8000, 'invoice', 8, 'Depósito vía PayPal', NOW() - INTERVAL '1 day');

-- ============================================================================
-- 10. ÓRDENES
-- ============================================================================

-- Órdenes de Marcos (user_id = 3) - El buscador de validación social
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at) VALUES
(3, 1, 'Instagram Followers - HQ', 1, 'SMM-10001', 'https://instagram.com/marcos_garcia', 1000, 369, 0, 'completed', 0.99, 0.45, 0.9900, 0.4500, 0.5400, TRUE, 30, NOW() + INTERVAL '23 days', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days' + INTERVAL '18 hours'),
(3, 4, 'Instagram Likes - Real', 4, 'SMM-10002', 'https://instagram.com/p/ABC123', 500, NULL, 0, 'completed', 0.55, 0.25, 0.2750, 0.1250, 0.1500, FALSE, 0, NULL, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '45 minutes'),
(3, 4, 'Instagram Likes - Real', 4, 'SMM-10003', 'https://instagram.com/p/DEF456', 500, NULL, 0, 'completed', 0.55, 0.25, 0.2750, 0.1250, 0.1500, FALSE, 0, NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '30 minutes'),
(3, 9, 'Instagram Reels Views', 9, 'SMM-10004', 'https://instagram.com/reel/GHI789', 10000, NULL, 0, 'completed', 0.18, 0.08, 1.8000, 0.8000, 1.0000, FALSE, 0, NULL, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '2 hours');

-- Órdenes de Laura (user_id = 4) - La dueña de tienda
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at) VALUES
(4, 2, 'Instagram Followers - Premium', 2, 'SMM-20001', 'https://instagram.com/laura_tienda', 10000, 11, 0, 'completed', 1.99, 0.90, 19.9000, 9.0000, 10.9000, TRUE, 90, NOW() + INTERVAL '76 days', NOW() - INTERVAL '14 days', NOW() - INTERVAL '12 days'),
(4, 1, 'Instagram Followers - HQ', 1, 'SMM-20002', 'https://instagram.com/laura_tienda', 5000, 10011, 0, 'completed', 0.99, 0.45, 4.9500, 2.2500, 2.7000, TRUE, 30, NOW() + INTERVAL '16 days', NOW() - INTERVAL '14 days', NOW() - INTERVAL '13 days' + INTERVAL '20 hours'),
(4, 5, 'Instagram Likes - Premium', 5, 'SMM-20003', 'https://instagram.com/p/TIENDA001', 1000, NULL, 0, 'completed', 1.10, 0.50, 1.1000, 0.5000, 0.6000, TRUE, 30, NOW() + INTERVAL '25 days', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '8 hours'),
(4, 8, 'Instagram Views - Fast', 8, 'SMM-20004', 'https://instagram.com/p/TIENDA002', 50000, NULL, 0, 'completed', 0.12, 0.05, 6.0000, 2.5000, 3.5000, FALSE, 0, NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '25 minutes');

-- Órdenes de Carlos (user_id = 5) - El influencer
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at) VALUES
(5, 10, 'TikTok Followers - Real', 10, 'SMM-30001', 'https://tiktok.com/@carlos_influencer', 50000, 125000, 0, 'completed', 1.20, 0.55, 60.0000, 27.5000, 32.5000, TRUE, 30, NOW() + INTERVAL '20 days', NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days'),
(5, 13, 'TikTok Views - Instant', 13, 'SMM-30002', 'https://tiktok.com/@carlos_influencer/video/123', 1000000, NULL, 0, 'completed', 0.05, 0.02, 50.0000, 20.0000, 30.0000, FALSE, 0, NULL, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days' + INTERVAL '15 minutes'),
(5, 12, 'TikTok Likes - Fast', 12, 'SMM-30003', 'https://tiktok.com/@carlos_influencer/video/123', 25000, NULL, 0, 'completed', 0.45, 0.20, 11.2500, 5.0000, 6.2500, FALSE, 0, NULL, NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days' + INTERVAL '20 minutes'),
(5, 15, 'YouTube Subscribers - Real', 15, 'SMM-30004', 'https://youtube.com/@CarlosInfluencer', 5000, 45000, 1250, 'in_progress', 4.50, 2.00, 22.5000, 10.0000, 12.5000, TRUE, 30, NOW() + INTERVAL '28 days', NOW() - INTERVAL '2 days', NULL),
(5, 17, 'YouTube Views - Fast', 17, NULL, 'https://youtube.com/watch?v=ABC123XYZ', 100000, NULL, 100000, 'pending', 1.80, 0.80, 180.0000, 80.0000, 100.0000, FALSE, 0, NULL, NOW() - INTERVAL '1 hour', NULL);

-- Órdenes de María (user_id = 6) - La startup
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, refill_deadline, created_at, completed_at) VALUES
(6, 28, 'LinkedIn Followers - Real', 28, 'SMM-40001', 'https://linkedin.com/company/maria-startup', 1000, 50, 0, 'completed', 6.50, 3.00, 6.5000, 3.0000, 3.5000, TRUE, 30, NOW() + INTERVAL '10 days', NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days'),
(6, 29, 'LinkedIn Post Likes', 29, 'SMM-40002', 'https://linkedin.com/posts/activity-123', 500, NULL, 0, 'completed', 3.25, 1.50, 1.6250, 0.7500, 0.8750, FALSE, 0, NULL, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days' + INTERVAL '6 hours');

-- Orden cancelada
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at) VALUES
(6, 1, 'Instagram Followers - HQ', 1, NULL, 'https://instagram.com/invalid_user_404', 1000, 1000, 'cancelled', 0.99, 0.45, 0.9900, 0.4500, 0.5400, TRUE, 30, NOW() - INTERVAL '10 days');

-- Orden reembolsada
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at, completed_at) VALUES
(7, 3, 'Instagram Followers - Starter', 3, 'SMM-50001', 'https://instagram.com/test_account', 5000, 100, 3500, 'refunded', 0.35, 0.15, 1.7500, 0.7500, 1.0000, FALSE, 0, NOW() - INTERVAL '5 days', NULL);

-- Orden parcialmente completada
INSERT INTO orders (user_id, service_id, service_name, provider_service_id, provider_order_id, target, quantity, start_count, remains, status, price_per_k, cost_per_k, total_charge, total_cost, profit, is_refillable, refill_days, created_at, completed_at) VALUES
(7, 4, 'Instagram Likes - Real', 4, 'SMM-50002', 'https://instagram.com/p/TEST123', 1000, NULL, 250, 'partial', 0.55, 0.25, 0.5500, 0.2500, 0.3000, FALSE, 0, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days');

-- ============================================================================
-- 11. TRANSACCIONES DE ÓRDENES
-- ============================================================================

-- Cargos por órdenes (balance negativo)
INSERT INTO transactions (user_id, type, amount, balance_before, balance_after, reference_type, reference_id, description, created_at) VALUES
-- Marcos
(3, 'order', -0.9900, 48.2500, 47.2600, 'order', 1, 'Order #1 - Instagram Followers - HQ', NOW() - INTERVAL '7 days'),
(3, 'order', -0.2750, 72.2200, 71.9450, 'order', 2, 'Order #2 - Instagram Likes - Real', NOW() - INTERVAL '5 days'),
(3, 'order', -0.2750, 71.9450, 71.6700, 'order', 3, 'Order #3 - Instagram Likes - Real', NOW() - INTERVAL '3 days'),
(3, 'order', -1.8000, 71.6700, 69.8700, 'order', 4, 'Order #4 - Instagram Reels Views', NOW() - INTERVAL '2 days'),
-- Laura
(4, 'order', -19.9000, 195.8000, 175.9000, 'order', 5, 'Order #5 - Instagram Followers - Premium', NOW() - INTERVAL '14 days'),
(4, 'order', -4.9500, 175.9000, 170.9500, 'order', 6, 'Order #6 - Instagram Followers - HQ', NOW() - INTERVAL '14 days'),
(4, 'order', -1.1000, 170.9500, 169.8500, 'order', 7, 'Order #7 - Instagram Likes - Premium', NOW() - INTERVAL '5 days'),
(4, 'order', -6.0000, 169.8500, 163.8500, 'order', 8, 'Order #8 - Instagram Views - Fast', NOW() - INTERVAL '3 days'),
-- Carlos
(5, 'order', -60.0000, 679.1000, 619.1000, 'order', 9, 'Order #9 - TikTok Followers - Real', NOW() - INTERVAL '10 days'),
(5, 'order', -50.0000, 619.1000, 569.1000, 'order', 10, 'Order #10 - TikTok Views - Instant', NOW() - INTERVAL '8 days'),
(5, 'order', -11.2500, 569.1000, 557.8500, 'order', 11, 'Order #11 - TikTok Likes - Fast', NOW() - INTERVAL '8 days'),
(5, 'order', -22.5000, 557.8500, 535.3500, 'order', 12, 'Order #12 - YouTube Subscribers - Real', NOW() - INTERVAL '2 days'),
(5, 'order', -180.0000, 535.3500, 355.3500, 'order', 13, 'Order #13 - YouTube Views - Fast', NOW() - INTERVAL '1 hour'),
-- María
(6, 'order', -6.5000, 48.2500, 41.7500, 'order', 14, 'Order #14 - LinkedIn Followers - Real', NOW() - INTERVAL '20 days'),
(6, 'order', -1.6250, 41.7500, 40.1250, 'order', 15, 'Order #15 - LinkedIn Post Likes', NOW() - INTERVAL '15 days'),
(6, 'order', -0.9900, 40.1250, 39.1350, 'order', 16, 'Order #16 - Instagram Followers - HQ (Cancelled)', NOW() - INTERVAL '10 days'),
-- Test user
(7, 'order', -1.7500, 96.8000, 95.0500, 'order', 17, 'Order #17 - Instagram Followers - Starter', NOW() - INTERVAL '5 days'),
(7, 'order', -0.5500, 95.0500, 94.5000, 'order', 18, 'Order #18 - Instagram Likes - Real', NOW() - INTERVAL '3 days');

-- Reembolsos
INSERT INTO transactions (user_id, type, amount, balance_before, balance_after, reference_type, reference_id, description, created_at) VALUES
(6, 'refund', 0.9900, 39.1350, 40.1250, 'order', 16, 'Refund for Order #16 - Target not found', NOW() - INTERVAL '10 days' + INTERVAL '1 hour'),
(7, 'refund', 1.2250, 94.5000, 95.7250, 'order', 17, 'Partial refund for Order #17 - 70% delivered', NOW() - INTERVAL '4 days');

-- Ajuste manual de admin
INSERT INTO transactions (user_id, type, amount, balance_before, balance_after, reference_type, reference_id, description, created_at) VALUES
(7, 'adjustment', 4.2750, 95.7250, 100.0000, NULL, NULL, 'Compensation for service delay - Admin adjustment', NOW() - INTERVAL '3 days');

-- ============================================================================
-- 12. ORDER REFILLS
-- ============================================================================

INSERT INTO order_refills (order_id, provider_refill_id, quantity, status, created_at, completed_at) VALUES
(1, 'REFILL-10001', 150, 'completed', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '12 hours'),
(5, 'REFILL-20001', 500, 'completed', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days'),
(5, 'REFILL-20002', 200, 'pending', NOW() - INTERVAL '1 day', NULL),
(9, NULL, 1000, 'processing', NOW() - INTERVAL '2 hours', NULL);

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
BEGIN
    SELECT COUNT(*) INTO users_count FROM users;
    SELECT COUNT(*) INTO orders_count FROM orders;
    SELECT COUNT(*) INTO services_count FROM services;
    SELECT COUNT(*) INTO invoices_count FROM invoices;
    SELECT COUNT(*) INTO transactions_count FROM transactions;
    
    RAISE NOTICE '============================================';
    RAISE NOTICE 'DATOS DE EJEMPLO INSERTADOS CORRECTAMENTE';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Usuarios: %', users_count;
    RAISE NOTICE 'Servicios: %', services_count;
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
    RAISE NOTICE '(bcrypt hash de ejemplo, cambiar en producción)';
    RAISE NOTICE '============================================';
END $$;

-- ============================================================================
-- FIN DEL SCRIPT DE DATOS DE EJEMPLO
-- ============================================================================
