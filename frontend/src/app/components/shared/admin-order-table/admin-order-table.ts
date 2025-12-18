import { ChangeDetectionStrategy, Component, input, output, signal, computed } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { NgIcon } from '@ng-icons/core';

import { AdminOrderStatusBadge, OrderStatus } from '../admin-order-status-badge/admin-order-status-badge';

/**
 * Admin order data interface
 * Contains all fields needed for the admin order table view
 */
export interface AdminOrder {
  id: string;
  status: OrderStatus;
  username: string;
  userHandle: string;
  serviceName: string;
  serviceId: string;
  link: string;
  quantity: number;
  remains: number;
  cost: number;
  sale: number;
  profit: number;
  createdAt: Date;
  description: string;
  providerId: string;
  providerServiceId: string;
  providerOrderId: string;
}

@Component({
  selector: 'app-admin-order-table',
  templateUrl: './admin-order-table.html',
  styleUrl: './admin-order-table.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DecimalPipe, DatePipe, NgIcon, AdminOrderStatusBadge]
})
export class AdminOrderTable {
  /** List of orders to display */
  readonly orders = input.required<AdminOrder[]>();

  /** Event emitted when an order row is clicked */
  readonly orderClick = output<AdminOrder>();

  /** Set of expanded order IDs */
  private readonly expandedOrderIds = signal<Set<string>>(new Set());

  /** Check if an order is expanded */
  protected isExpanded(orderId: string): boolean {
    return this.expandedOrderIds().has(orderId);
  }

  /** Toggle expanded state for an order */
  protected toggleExpand(orderId: string, event: Event): void {
    event.stopPropagation();
    this.expandedOrderIds.update(ids => {
      const newIds = new Set(ids);
      if (newIds.has(orderId)) {
        newIds.delete(orderId);
      } else {
        newIds.add(orderId);
      }
      return newIds;
    });
  }

  /** Handle row click */
  protected onRowClick(order: AdminOrder): void {
    this.orderClick.emit(order);
  }

  /** Format currency for display */
  protected formatCurrency(value: number): string {
    return `$${value.toFixed(2)}`;
  }
}
