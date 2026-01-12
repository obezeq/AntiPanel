import { describe, it, expect, beforeEach, beforeAll, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Orders } from './orders';
import { OrderService, type PageResponse, type OrderResponse } from '../../core/services/order.service';
import { UserService } from '../../core/services/user.service';
import { of } from 'rxjs';

describe('Orders', () => {
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
  const mockOrders: OrderResponse[] = [
    {
      id: 1,
      user: { id: 1, email: 'test@test.com', role: 'USER' },
      serviceId: 1,
      serviceName: 'Instagram Followers',
      target: 'https://instagram.com/user',
      quantity: 1000,
      startCount: null,
      remains: 0,
      status: 'COMPLETED',
      progress: 100,
      totalCharge: 5.00,
      isRefillable: false,
      refillDays: 0,
      refillDeadline: null,
      canRequestRefill: false,
      createdAt: '2024-01-01T10:00:00Z',
      completedAt: '2024-01-01T12:00:00Z',
      updatedAt: '2024-01-01T12:00:00Z'
    },
    {
      id: 2,
      user: { id: 1, email: 'test@test.com', role: 'USER' },
      serviceId: 2,
      serviceName: 'TikTok Views',
      target: 'https://tiktok.com/@user/video/123',
      quantity: 5000,
      startCount: 100,
      remains: 2500,
      status: 'PROCESSING',
      progress: 50,
      totalCharge: 2.50,
      isRefillable: true,
      refillDays: 30,
      refillDeadline: '2024-02-01T00:00:00Z',
      canRequestRefill: false,
      createdAt: '2024-01-01T14:00:00Z',
      completedAt: null,
      updatedAt: '2024-01-01T14:30:00Z'
    }
  ];

  const mockPageResponse: PageResponse<OrderResponse> = {
    content: mockOrders,
    pageNumber: 0,
    pageSize: 10,
    totalElements: 2,
    totalPages: 1,
    first: true,
    last: true,
    hasNext: false,
    hasPrevious: false
  };

  const mockUserStats = {
    totalOrders: 2,
    pendingOrders: 0,
    completedOrders: 1,
    ordersThisMonth: 2,
    balance: 100.00
  };

  beforeEach(async () => {
    const orderServiceMock = {
      getOrders: vi.fn().mockReturnValue(of(mockPageResponse))
    };

    const userServiceMock = {
      getStatistics: vi.fn().mockReturnValue(of(mockUserStats))
    };

    await TestBed.configureTestingModule({
      imports: [Orders],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: OrderService, useValue: orderServiceMock },
        { provide: UserService, useValue: userServiceMock }
      ]
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(Orders);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should display loading state initially', () => {
    const fixture = TestBed.createComponent(Orders);
    const component = fixture.componentInstance;
    expect(component['isLoading']()).toBe(true);
  });

  it('should load orders on init', async () => {
    const fixture = TestBed.createComponent(Orders);
    fixture.detectChanges();
    await fixture.whenStable();

    const component = fixture.componentInstance;
    const orders = component['orders']();

    expect(orders.length).toBe(2);
    expect(component['isLoading']()).toBe(false);
  });

  it('should set total pages from response', async () => {
    const fixture = TestBed.createComponent(Orders);
    fixture.detectChanges();
    await fixture.whenStable();

    const component = fixture.componentInstance;
    expect(component['totalPages']()).toBe(1);
    expect(component['totalElements']()).toBe(2);
  });

  it('should filter orders by category', async () => {
    const fixture = TestBed.createComponent(Orders);
    fixture.detectChanges();
    await fixture.whenStable();

    const component = fixture.componentInstance;

    // Filter by COMPLETED
    component['selectedCategory'].set('COMPLETED');
    const completedOrders = component['filteredOrders']();
    expect(completedOrders.length).toBe(1);
    expect(completedOrders[0].status).toBe('completed');

    // Filter by PROCESSING
    component['selectedCategory'].set('PROCESSING');
    const processingOrders = component['filteredOrders']();
    expect(processingOrders.length).toBe(1);
    expect(processingOrders[0].status).toBe('processing');
  });

  it('should filter orders by search query', async () => {
    const fixture = TestBed.createComponent(Orders);
    fixture.detectChanges();
    await fixture.whenStable();

    const component = fixture.componentInstance;

    component['searchQuery'].set('instagram');
    const filteredOrders = component['filteredOrders']();
    expect(filteredOrders.length).toBe(1);
    expect(filteredOrders[0].serviceName).toBe('Instagram Followers');
  });

  it('should show orders when not loading and has orders', async () => {
    const fixture = TestBed.createComponent(Orders);
    fixture.detectChanges();
    await fixture.whenStable();

    const component = fixture.componentInstance;
    expect(component['showOrders']()).toBe(true);
    expect(component['isEmpty']()).toBe(false);
  });

  it('should show empty state when no orders match filter', async () => {
    const fixture = TestBed.createComponent(Orders);
    fixture.detectChanges();
    await fixture.whenStable();

    const component = fixture.componentInstance;
    component['searchQuery'].set('nonexistent');

    expect(component['isEmpty']()).toBe(true);
    expect(component['showOrders']()).toBe(false);
  });

  it('should format balance for header', async () => {
    const fixture = TestBed.createComponent(Orders);
    fixture.detectChanges();
    await fixture.whenStable();

    const component = fixture.componentInstance;
    expect(component['balance']()).toBe('$100.00');
  });
});
