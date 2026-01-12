import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { TokenService } from './token.service';
import type { UserSummary } from './auth.service';

describe('TokenService', () => {
  let service: TokenService;

  const mockUser: UserSummary = {
    id: 1,
    email: 'test@test.com',
    role: 'USER',
    balance: 100.00
  };

  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [TokenService]
    });

    service = TestBed.inject(TokenService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('setTokens', () => {
    it('should store tokens in localStorage', () => {
      service.setTokens('access-token', 'refresh-token', 3600, mockUser);

      expect(localStorage.getItem('antipanel_access_token')).toBe('access-token');
      expect(localStorage.getItem('antipanel_refresh_token')).toBe('refresh-token');
      expect(localStorage.getItem('antipanel_user')).toBe(JSON.stringify(mockUser));
    });

    it('should update access token signal', () => {
      service.setTokens('access-token', 'refresh-token', 3600, mockUser);

      expect(service.getAccessToken()).toBe('access-token');
      expect(service.accessToken()).toBe('access-token');
    });

    it('should update current user signal', () => {
      service.setTokens('access-token', 'refresh-token', 3600, mockUser);

      expect(service.currentUser()).toEqual(mockUser);
    });

    it('should calculate expiry timestamp correctly', () => {
      const now = Date.now();
      vi.setSystemTime(now);

      service.setTokens('access-token', 'refresh-token', 3600, mockUser);

      const expectedExpiry = now + (3600 * 1000);
      expect(service.getTokenExpiry()).toBe(expectedExpiry);

      vi.useRealTimers();
    });
  });

  describe('getAccessToken', () => {
    it('should return null when no token is stored', () => {
      expect(service.getAccessToken()).toBeNull();
    });

    it('should return stored token', () => {
      service.setTokens('test-token', 'refresh', 3600);
      expect(service.getAccessToken()).toBe('test-token');
    });
  });

  describe('getRefreshToken', () => {
    it('should return null when no token is stored', () => {
      expect(service.getRefreshToken()).toBeNull();
    });

    it('should return stored refresh token', () => {
      service.setTokens('access', 'refresh-token', 3600);
      expect(service.getRefreshToken()).toBe('refresh-token');
    });
  });

  describe('clearTokens', () => {
    it('should remove all stored tokens', () => {
      service.setTokens('access', 'refresh', 3600, mockUser);

      service.clearTokens();

      expect(localStorage.getItem('antipanel_access_token')).toBeNull();
      expect(localStorage.getItem('antipanel_refresh_token')).toBeNull();
      expect(localStorage.getItem('antipanel_token_expiry')).toBeNull();
      expect(localStorage.getItem('antipanel_user')).toBeNull();
    });

    it('should update signals to null', () => {
      service.setTokens('access', 'refresh', 3600, mockUser);

      service.clearTokens();

      expect(service.getAccessToken()).toBeNull();
      expect(service.currentUser()).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('should return false when no token exists', () => {
      expect(service.isAuthenticated()).toBe(false);
    });

    it('should return true when valid token exists', () => {
      service.setTokens('access', 'refresh', 3600);
      expect(service.isAuthenticated()).toBe(true);
    });

    it('should return false when token is expired', () => {
      const now = Date.now();
      vi.setSystemTime(now);

      // Set token that expires in 1 second
      service.setTokens('access', 'refresh', 1);

      // Move time forward by 2 seconds
      vi.setSystemTime(now + 2000);

      expect(service.isAuthenticated()).toBe(false);

      vi.useRealTimers();
    });
  });

  describe('isTokenExpired', () => {
    it('should return true when no expiry is set', () => {
      expect(service.isTokenExpired()).toBe(true);
    });

    it('should return false for valid token', () => {
      service.setTokens('access', 'refresh', 3600);
      expect(service.isTokenExpired()).toBe(false);
    });

    it('should return true when within buffer period', () => {
      const now = Date.now();
      vi.setSystemTime(now);

      // Set token that expires in 30 seconds
      service.setTokens('access', 'refresh', 30);

      // With 60 second buffer, token should be considered expired
      expect(service.isTokenExpired(60)).toBe(true);

      vi.useRealTimers();
    });

    it('should return false when outside buffer period', () => {
      service.setTokens('access', 'refresh', 3600);
      expect(service.isTokenExpired(60)).toBe(false);
    });
  });

  describe('updateAccessToken', () => {
    it('should update only access token', () => {
      service.setTokens('old-access', 'refresh', 3600);

      service.updateAccessToken('new-access', 7200);

      expect(service.getAccessToken()).toBe('new-access');
      expect(service.getRefreshToken()).toBe('refresh');
    });

    it('should update expiry timestamp', () => {
      const now = Date.now();
      vi.setSystemTime(now);

      service.setTokens('old-access', 'refresh', 3600);
      service.updateAccessToken('new-access', 7200);

      const expectedExpiry = now + (7200 * 1000);
      expect(service.getTokenExpiry()).toBe(expectedExpiry);

      vi.useRealTimers();
    });
  });

  describe('updateUser', () => {
    it('should update stored user', () => {
      service.setTokens('access', 'refresh', 3600, mockUser);

      const updatedUser: UserSummary = { ...mockUser, balance: 200.00 };
      service.updateUser(updatedUser);

      expect(service.currentUser()?.balance).toBe(200.00);
      expect(JSON.parse(localStorage.getItem('antipanel_user')!).balance).toBe(200.00);
    });
  });

  describe('canRefresh', () => {
    it('should return false when no refresh token exists', () => {
      expect(service.canRefresh()).toBe(false);
    });

    it('should return true when refresh token exists', () => {
      service.setTokens('access', 'refresh', 3600);
      expect(service.canRefresh()).toBe(true);
    });
  });

  describe('initialization', () => {
    it('should load tokens from localStorage on fresh instance', () => {
      // Pre-populate localStorage before creating new TestBed
      localStorage.setItem('antipanel_access_token', 'stored-token');
      localStorage.setItem('antipanel_refresh_token', 'stored-refresh');
      localStorage.setItem('antipanel_user', JSON.stringify(mockUser));
      localStorage.setItem('antipanel_token_expiry', (Date.now() + 3600000).toString());

      // Reset and recreate TestBed to get fresh instance
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [TokenService]
      });

      const newService = TestBed.inject(TokenService);

      expect(newService.getAccessToken()).toBe('stored-token');
      expect(newService.currentUser()?.email).toBe('test@test.com');
    });
  });
});
