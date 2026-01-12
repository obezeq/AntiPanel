import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { AuthService, type AuthResponse, type UserResponse } from './auth.service';
import { TokenService } from './token.service';
import { firstValueFrom } from 'rxjs';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let tokenServiceMock: { setTokens: ReturnType<typeof vi.fn>; clearTokens: ReturnType<typeof vi.fn> };

  const mockAuthResponse: AuthResponse = {
    accessToken: 'test-access-token',
    refreshToken: 'test-refresh-token',
    tokenType: 'Bearer',
    expiresIn: 3600,
    user: {
      id: 1,
      email: 'test@test.com',
      role: 'USER',
      balance: 100.00
    }
  };

  const mockUserResponse: UserResponse = {
    id: 1,
    email: 'test@test.com',
    role: 'USER',
    department: null,
    balance: 0,
    isBanned: false,
    bannedReason: null,
    lastLoginAt: null,
    loginCount: 0,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  };

  beforeEach(() => {
    tokenServiceMock = {
      setTokens: vi.fn(),
      clearTokens: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService,
        { provide: TokenService, useValue: tokenServiceMock }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('login', () => {
    it('should authenticate user and store tokens', async () => {
      const credentials = { email: 'test@test.com', password: 'password123' };

      const loginPromise = firstValueFrom(service.login(credentials));

      const req = httpMock.expectOne('/api/v1/auth/login');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(credentials);
      req.flush(mockAuthResponse);

      const response = await loginPromise;

      expect(response.accessToken).toBe('test-access-token');
      expect(tokenServiceMock.setTokens).toHaveBeenCalledWith(
        'test-access-token',
        'test-refresh-token',
        3600,
        mockAuthResponse.user
      );
    });

    it('should throw error on invalid credentials', async () => {
      const credentials = { email: 'wrong@test.com', password: 'wrong' };

      const loginPromise = firstValueFrom(service.login(credentials));

      const req = httpMock.expectOne('/api/v1/auth/login');
      req.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

      await expect(loginPromise).rejects.toThrow();
    });
  });

  describe('register', () => {
    it('should register new user', async () => {
      const registerData = { email: 'new@test.com', password: 'password123', role: 'USER' as const };
      const newUserResponse = { ...mockUserResponse, email: 'new@test.com' };

      const registerPromise = firstValueFrom(service.register(registerData));

      const req = httpMock.expectOne('/api/v1/auth/register');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(registerData);
      req.flush(newUserResponse);

      const response = await registerPromise;

      expect(response.email).toBe('new@test.com');
    });

    it('should throw error on duplicate email', async () => {
      const registerData = { email: 'existing@test.com', password: 'password123', role: 'USER' as const };

      const registerPromise = firstValueFrom(service.register(registerData));

      const req = httpMock.expectOne('/api/v1/auth/register');
      req.flush({ message: 'Email already registered' }, { status: 409, statusText: 'Conflict' });

      await expect(registerPromise).rejects.toThrow();
    });
  });

  describe('logout', () => {
    it('should clear tokens on logout', async () => {
      const logoutPromise = firstValueFrom(service.logout());

      const req = httpMock.expectOne('/api/v1/auth/logout');
      expect(req.request.method).toBe('POST');
      req.flush(null);

      await logoutPromise;

      expect(tokenServiceMock.clearTokens).toHaveBeenCalled();
    });

    it('should clear tokens even if backend fails', async () => {
      const logoutPromise = firstValueFrom(service.logout()).catch(() => {});

      const req = httpMock.expectOne('/api/v1/auth/logout');
      req.flush(null, { status: 500, statusText: 'Server Error' });

      await logoutPromise;

      expect(tokenServiceMock.clearTokens).toHaveBeenCalled();
    });
  });

  describe('logoutLocal', () => {
    it('should clear tokens without calling backend', () => {
      service.logoutLocal();
      expect(tokenServiceMock.clearTokens).toHaveBeenCalled();
      httpMock.expectNone('/api/v1/auth/logout');
    });
  });

  describe('refreshToken', () => {
    it('should refresh access token', async () => {
      const refreshPromise = firstValueFrom(service.refreshToken('old-refresh-token'));

      const req = httpMock.expectOne('/api/v1/auth/refresh');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ refreshToken: 'old-refresh-token' });
      req.flush(mockAuthResponse);

      const response = await refreshPromise;

      expect(response.accessToken).toBe('test-access-token');
    });
  });

  describe('checkEmailExists', () => {
    it('should return true for taken email', async () => {
      const exists = await firstValueFrom(service.checkEmailExists('admin@antipanel.com'));
      expect(exists).toBe(true);
    });

    it('should return false for available email', async () => {
      const exists = await firstValueFrom(service.checkEmailExists('available@test.com'));
      expect(exists).toBe(false);
    });
  });

  describe('isEmailAlreadyRegisteredError', () => {
    it('should return true for 409 with email already registered message', () => {
      const error = new HttpErrorResponse({
        status: 409,
        error: { message: 'Email already registered' }
      });
      expect(service.isEmailAlreadyRegisteredError(error)).toBe(true);
    });

    it('should return false for other errors', () => {
      const error = new HttpErrorResponse({
        status: 400,
        error: { message: 'Bad request' }
      });
      expect(service.isEmailAlreadyRegisteredError(error)).toBe(false);
    });
  });

  describe('getErrorMessage', () => {
    it('should return error message from response', () => {
      const error = new HttpErrorResponse({
        status: 400,
        error: { message: 'Custom error message' }
      });
      expect(service.getErrorMessage(error)).toBe('Custom error message');
    });

    it('should return default message for 401', () => {
      const error = new HttpErrorResponse({ status: 401, error: {} });
      expect(service.getErrorMessage(error)).toBe('Invalid email or password.');
    });

    it('should return default message for 409', () => {
      const error = new HttpErrorResponse({ status: 409, error: {} });
      expect(service.getErrorMessage(error)).toBe('Email already registered.');
    });

    it('should return default message for 500', () => {
      const error = new HttpErrorResponse({ status: 500, error: {} });
      expect(service.getErrorMessage(error)).toBe('Server error. Please try again later.');
    });

    it('should return generic message for unknown errors', () => {
      const error = new HttpErrorResponse({ status: 418, error: {} });
      expect(service.getErrorMessage(error)).toBe('An unexpected error occurred.');
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
