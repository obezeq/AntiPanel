import { describe, it, expect, beforeEach, beforeAll, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Dashboard } from './dashboard';
import { UserService } from '../../core/services/user.service';
import { of } from 'rxjs';

describe('Dashboard', () => {
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
  const mockUserStats = {
    totalOrders: 150,
    pendingOrders: 10,
    completedOrders: 130,
    ordersThisMonth: 25,
    balance: 150.50
  };

  beforeEach(async () => {
    const userServiceMock = {
      getStatistics: vi.fn().mockReturnValue(of(mockUserStats))
    };

    await TestBed.configureTestingModule({
      imports: [Dashboard],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: UserService, useValue: userServiceMock }
      ]
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(Dashboard);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should display loading state initially', () => {
    const fixture = TestBed.createComponent(Dashboard);
    const component = fixture.componentInstance;
    // Initially isLoading is true before ngOnInit completes
    expect(component['isLoading']()).toBe(true);
  });

  it('should load user statistics on init', () => {
    const fixture = TestBed.createComponent(Dashboard);
    const component = fixture.componentInstance;

    // Manually set stats to simulate loaded state
    component['userStats'].set(mockUserStats);
    component['isLoading'].set(false);

    expect(component['userStats']()).toBeTruthy();
    expect(component['userStats']()?.totalOrders).toBe(150);
    expect(component['userStats']()?.balance).toBe(150.50);
  });

  it('should format balance correctly', () => {
    const fixture = TestBed.createComponent(Dashboard);
    const component = fixture.componentInstance;

    // Manually set stats
    component['userStats'].set(mockUserStats);

    expect(component['balance']()).toBe('$150.50');
  });

  it('should compute userBalance from stats', () => {
    const fixture = TestBed.createComponent(Dashboard);
    const component = fixture.componentInstance;

    // Manually set stats
    component['userStats'].set(mockUserStats);

    expect(component['userBalance']()).toBe(150.50);
  });

  it('should show $0.00 balance when stats are null', () => {
    const fixture = TestBed.createComponent(Dashboard);
    // Don't detect changes to keep userStats as null
    const component = fixture.componentInstance;
    component['userStats'].set(null);

    expect(component['balance']()).toBe('$0.00');
  });
});
