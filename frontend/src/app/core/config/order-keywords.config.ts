import { InjectionToken } from '@angular/core';
import type { KeywordMapping, DisplayNameMapping } from '../models/order-parser.models';

/**
 * Injection token for order keyword mappings.
 * Enables testing with different keyword sets and future dynamic loading.
 */
export const ORDER_KEYWORDS = new InjectionToken<KeywordMapping>('ORDER_KEYWORDS', {
  providedIn: 'root',
  factory: () => DEFAULT_KEYWORDS
});

/**
 * Injection token for display name mappings.
 */
export const DISPLAY_NAMES = new InjectionToken<DisplayNameMapping>('DISPLAY_NAMES', {
  providedIn: 'root',
  factory: () => DEFAULT_DISPLAY_NAMES
});

/**
 * Default multilingual keyword mappings.
 * Supports: English (default), Spanish, German, French, Hindi, Indonesian, Arabic, Portuguese
 *
 * All keywords map to canonical English slugs used by the API.
 * UI remains in English - only parser understands multilingual input.
 */
export const DEFAULT_KEYWORDS: KeywordMapping = {
  platforms: {
    // =====================
    // Instagram
    // =====================
    instagram: 'instagram',
    insta: 'instagram',
    ig: 'instagram',

    // =====================
    // TikTok
    // =====================
    tiktok: 'tiktok',
    'tik-tok': 'tiktok',
    tik: 'tiktok',
    tok: 'tiktok',
    tt: 'tiktok',

    // =====================
    // Twitter/X
    // =====================
    twitter: 'twitter',
    x: 'twitter',
    tweet: 'twitter',

    // =====================
    // YouTube
    // =====================
    youtube: 'youtube',
    yt: 'youtube',

    // =====================
    // Snapchat
    // =====================
    snapchat: 'snapchat',
    snap: 'snapchat',

    // =====================
    // Facebook
    // =====================
    facebook: 'facebook',
    fb: 'facebook',

    // =====================
    // Discord
    // =====================
    discord: 'discord',

    // =====================
    // LinkedIn
    // =====================
    linkedin: 'linkedin',
    li: 'linkedin'
  },

  serviceTypes: {
    // =====================
    // FOLLOWERS
    // =====================
    // English
    followers: 'followers',
    follower: 'followers',
    follow: 'followers',
    // Spanish (also Portuguese)
    seguidores: 'followers',
    seguidor: 'followers',
    // German
    'anhänger': 'followers',
    anhanger: 'followers', // ASCII fallback
    // French
    'abonnés': 'followers',
    abonnes: 'followers', // ASCII fallback
    suiveurs: 'followers',
    suiveur: 'followers',
    // Hindi
    'फॉलोअर्स': 'followers',
    'अनुयायी': 'followers',
    anuyayi: 'followers',
    // Indonesian
    pengikut: 'followers',
    // Arabic
    'متابعين': 'followers',
    'متابع': 'followers',
    mutabiin: 'followers',
    mutabi: 'followers',

    // =====================
    // LIKES
    // =====================
    // English
    likes: 'likes',
    like: 'likes',
    // Spanish
    'me gusta': 'likes',
    megusta: 'likes',
    // German
    'gefällt mir': 'likes',
    'gefallt mir': 'likes', // ASCII fallback
    'gefällt': 'likes',
    gefallt: 'likes',
    // French
    "j'aime": 'likes',
    jaime: 'likes',
    // Hindi
    'लाइक्स': 'likes',
    'पसंद': 'likes',
    pasand: 'likes',
    // Indonesian
    suka: 'likes',
    // Arabic
    'إعجابات': 'likes',
    'لايكات': 'likes',
    ijabat: 'likes',
    laykat: 'likes',
    // Portuguese
    curtidas: 'likes',
    curtir: 'likes',

    // =====================
    // COMMENTS
    // =====================
    // English
    comments: 'comments',
    comment: 'comments',
    // Spanish (also Portuguese)
    comentarios: 'comments',
    comentario: 'comments',
    // German
    kommentare: 'comments',
    kommentar: 'comments',
    // French
    commentaires: 'comments',
    commentaire: 'comments',
    // Hindi
    'टिप्पणियां': 'comments',
    'कमेंट्स': 'comments',
    tippaniyaan: 'comments',
    // Indonesian
    komentar: 'comments',
    // Arabic
    'تعليقات': 'comments',
    taaliqat: 'comments',

    // =====================
    // VIEWS
    // =====================
    // English
    views: 'views',
    view: 'views',
    // Spanish
    vistas: 'views',
    vista: 'views',
    visualizaciones: 'views',
    visualizacion: 'views',
    // German
    aufrufe: 'views',
    aufruf: 'views',
    ansichten: 'views',
    ansicht: 'views',
    // French
    vues: 'views',
    vue: 'views',
    // Hindi
    'व्यूज': 'views',
    'देखे': 'views',
    dekhe: 'views',
    // Indonesian
    tayangan: 'views',
    tontonan: 'views',
    dilihat: 'views',
    // Arabic
    'مشاهدات': 'views',
    mushahadat: 'views',
    // Portuguese
    visualizacoes: 'views',

    // =====================
    // SUBSCRIBERS
    // =====================
    // English
    subscribers: 'subscribers',
    subscriber: 'subscribers',
    subs: 'subscribers',
    sub: 'subscribers',
    // Spanish
    suscriptores: 'subscribers',
    suscriptor: 'subscribers',
    // German
    abonnenten: 'subscribers',
    abonnent: 'subscribers',
    // French (uses abonnés - already mapped to followers, context-dependent)
    // Hindi
    'सब्सक्राइबर्स': 'subscribers',
    'सदस्य': 'subscribers',
    sadasya: 'subscribers',
    // Indonesian
    pelanggan: 'subscribers',
    // Arabic
    'مشتركين': 'subscribers',
    mushtarikin: 'subscribers',
    // Portuguese
    inscritos: 'subscribers',
    assinantes: 'subscribers',

    // =====================
    // SHARES
    // =====================
    // English
    shares: 'shares',
    share: 'shares',
    // Spanish
    compartidos: 'shares',
    compartido: 'shares',
    compartir: 'shares',
    // German
    teilen: 'shares',
    geteilt: 'shares',
    // French
    partages: 'shares',
    partage: 'shares',
    partager: 'shares',
    // Hindi
    'शेयर्स': 'shares',
    'साझा': 'shares',
    sajha: 'shares',
    // Indonesian
    bagikan: 'shares',
    berbagi: 'shares',
    // Arabic
    'مشاركات': 'shares',
    musharakat: 'shares',
    // Portuguese
    compartilhamentos: 'shares',

    // =====================
    // RETWEETS (Twitter/X specific)
    // =====================
    retweets: 'retweets',
    retweet: 'retweets',

    // =====================
    // CONNECTIONS (LinkedIn specific)
    // =====================
    connections: 'connections',
    connection: 'connections',
    connect: 'connections',

    // =====================
    // REPOSTS (LinkedIn specific)
    // =====================
    reposts: 'reposts',
    repost: 'reposts'
  }
};

/**
 * Display names for UI (English only).
 */
export const DEFAULT_DISPLAY_NAMES: DisplayNameMapping = {
  platforms: {
    instagram: 'INSTAGRAM',
    tiktok: 'TIKTOK',
    twitter: 'TWITTER/X',
    youtube: 'YOUTUBE',
    snapchat: 'SNAPCHAT',
    facebook: 'FACEBOOK',
    discord: 'DISCORD',
    linkedin: 'LINKEDIN'
  },
  serviceTypes: {
    followers: 'Followers',
    likes: 'Likes',
    comments: 'Comments',
    views: 'Views',
    subscribers: 'Subscribers',
    shares: 'Shares',
    retweets: 'Retweets',
    connections: 'Connections',
    reposts: 'Reposts',
    'company-followers': 'Company Followers'
  }
};
