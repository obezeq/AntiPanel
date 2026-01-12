import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, HttpErrorResponse } from '@angular/common/http';
import { OrderService, type OrderResponse, type PageResponse } from './order.service';
import { firstValueFrom } from 'rxjs';

describe('OrderService', () => {
  let service: OrderService;
  let httpMock: HttpTestingController;

  const mockOrder: OrderResponse = {
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
  };

  const mockPageResponse: PageResponse<OrderResponse> = {
    content: [mockOrder],
    pageNumber: 0,
    pageSize: 20,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
    hasNext: false,
    hasPrevious: false
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        OrderService
      ]
    });

    service = TestBed.inject(OrderService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('createOrder', () => {
    it('should create order with idempotency key', async () => {
      const request = {
        serviceId: 1,
        target: 'https://instagram.com/user',
        quantity: 1000
      };

      const createPromise = firstValueFrom(service.createOrder(request));

      const req = httpMock.expectOne('/api/v1/orders');
      expect(req.request.method).toBe('POST');
      expect(req.request.body.serviceId).toBe(1);
      expect(req.request.body.idempotencyKey).toBeDefined();
      req.flush(mockOrder);

      const response = await createPromise;

      expect(response.id).toBe(1);
      expect(response.serviceName).toBe('Instagram Followers');
    });

    it('should use provided idempotency key', async () => {
      const request = {
        serviceId: 1,
        target: 'https://instagram.com/user',
        quantity: 1000,
        idempotencyKey: 'custom-key'
      };

      const createPromise = firstValueFrom(service.createOrder(request));

      const req = httpMock.expectOne('/api/v1/orders');
      expect(req.request.body.idempotencyKey).toBe('custom-key');
      req.flush(mockOrder);

      await createPromise;
    });
  });

  describe('getOrders', () => {
    it('should fetch paginated orders', async () => {
      const getPromise = firstValueFrom(service.getOrders(0, 20));

      const req = httpMock.expectOne('/api/v1/orders?page=0&size=20');
      expect(req.request.method).toBe('GET');
      req.flush(mockPageResponse);

      const response = await getPromise;

      expect(response.content.length).toBe(1);
      expect(response.totalElements).toBe(1);
    });

    it('should use default pagination params', async () => {
      const getPromise = firstValueFrom(service.getOrders());

      const req = httpMock.expectOne('/api/v1/orders?page=0&size=20');
      req.flush(mockPageResponse);

      await getPromise;
    });
  });

  describe('getOrderById', () => {
    it('should fetch order by id', async () => {
      const getPromise = firstValueFrom(service.getOrderById(1));

      const req = httpMock.expectOne('/api/v1/orders/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockOrder);

      const response = await getPromise;

      expect(response.id).toBe(1);
    });
  });

  describe('getActiveOrders', () => {
    it('should fetch active orders', async () => {
      const getPromise = firstValueFrom(service.getActiveOrders());

      const req = httpMock.expectOne('/api/v1/orders/active');
      expect(req.request.method).toBe('GET');
      req.flush([mockOrder]);

      const response = await getPromise;

      expect(response.length).toBe(1);
    });
  });

  describe('getRefillableOrders', () => {
    it('should fetch refillable orders', async () => {
      const getPromise = firstValueFrom(service.getRefillableOrders());

      const req = httpMock.expectOne('/api/v1/orders/refillable');
      expect(req.request.method).toBe('GET');
      req.flush([mockOrder]);

      const response = await getPromise;

      expect(response.length).toBe(1);
    });
  });

  describe('error type checks', () => {
    it('should identify insufficient balance error', () => {
      const error = new HttpErrorResponse({ status: 402 });
      expect(service.isInsufficientBalanceError(error)).toBe(true);
    });

    it('should identify duplicate order error', () => {
      const error = new HttpErrorResponse({ status: 409 });
      expect(service.isDuplicateOrderError(error)).toBe(true);
    });

    it('should identify service unavailable error', () => {
      const error = new HttpErrorResponse({ status: 503 });
      expect(service.isServiceUnavailableError(error)).toBe(true);
    });

    it('should return false for non-matching errors', () => {
      const error = new HttpErrorResponse({ status: 400 });
      expect(service.isInsufficientBalanceError(error)).toBe(false);
      expect(service.isDuplicateOrderError(error)).toBe(false);
      expect(service.isServiceUnavailableError(error)).toBe(false);
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
