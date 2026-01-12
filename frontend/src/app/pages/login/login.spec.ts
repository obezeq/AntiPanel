import { describe, it, expect, beforeEach, beforeAll, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Login } from './login';
import { AuthService } from '../../core/services/auth.service';
import { of, throwError } from 'rxjs';

describe('Login', () => {
  // Mock window.matchMedia for ThemeService
  beforeAll(() => {
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: vi.fn().mockImplementation((query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(),
        removeListener: vi.fn(),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
      })),
    });
  });
  const createMockActivatedRoute = (queryParams: Record<string, string> = {}) => ({
    snapshot: {
      queryParamMap: {
        get: (key: string) => queryParams[key] ?? null
      }
    }
  });

  beforeEach(async () => {
    const authServiceMock = {
      login: vi.fn().mockReturnValue(of({ accessToken: 'token', refreshToken: 'refresh' })),
      getErrorMessage: vi.fn().mockReturnValue('An error occurred')
    };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
        { provide: ActivatedRoute, useValue: createMockActivatedRoute() }
      ]
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(Login);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should not be loading initially', () => {
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;
    expect(component['isLoading']()).toBe(false);
  });

  it('should have empty error message initially', () => {
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;
    expect(component['serverError']()).toBe('');
  });

  it('should show success message after registration', async () => {
    await TestBed.resetTestingModule();

    const authServiceMock = {
      login: vi.fn().mockReturnValue(of({ accessToken: 'token', refreshToken: 'refresh' }))
    };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
        { provide: ActivatedRoute, useValue: createMockActivatedRoute({ registered: 'true' }) }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    expect(component['successMessage']()).toBe('Account created successfully! Please login.');
  });

  it('should show info message when session expired', async () => {
    await TestBed.resetTestingModule();

    const authServiceMock = {
      login: vi.fn().mockReturnValue(of({ accessToken: 'token', refreshToken: 'refresh' }))
    };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
        { provide: ActivatedRoute, useValue: createMockActivatedRoute({ sessionExpired: 'true' }) }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    expect(component['infoMessage']()).toBe('Your session has expired. Please login again.');
  });

  it('should set return URL from query params', async () => {
    await TestBed.resetTestingModule();

    const authServiceMock = {
      login: vi.fn().mockReturnValue(of({ accessToken: 'token', refreshToken: 'refresh' }))
    };

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
        { provide: ActivatedRoute, useValue: createMockActivatedRoute({ returnUrl: '/wallet' }) }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(Login);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    expect(component['returnUrl']).toBe('/wallet');
  });

  it('should have isLoading signal', () => {
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;

    expect(component['isLoading']).toBeDefined();
    expect(component['isLoading']()).toBe(false);

    // Test that it can be set
    component['isLoading'].set(true);
    expect(component['isLoading']()).toBe(true);
  });

  it('should have serverError signal that can be cleared', () => {
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;

    // Set an error
    component['serverError'].set('Some error');
    expect(component['serverError']()).toBe('Some error');

    // Clear the error
    component['serverError'].set('');
    expect(component['serverError']()).toBe('');
  });
});
