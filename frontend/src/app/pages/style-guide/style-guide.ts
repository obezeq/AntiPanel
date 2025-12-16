import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { NgIcon } from '@ng-icons/core';

import { Button } from '../../components/shared/button/button';
import { Alert } from '../../components/shared/alert/alert';
import { FormInput } from '../../components/shared/form-input/form-input';
import { FormTextarea } from '../../components/shared/form-textarea/form-textarea';
import { FormSelect, SelectOption } from '../../components/shared/form-select/form-select';
import { ServiceCard, ServiceCardData } from '../../components/shared/service-card/service-card';
import { StatsCard, StatsCardData } from '../../components/shared/stats-card/stats-card';
import { OrderInput } from '../../components/shared/order-input/order-input';
import { DashboardHeader } from '../../components/shared/dashboard-header/dashboard-header';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { Sidebar, SidebarItem } from '../../components/layout/sidebar/sidebar';
import { MainContent } from '../../components/layout/main-content/main-content';
import { Modal } from '../../components/shared/modal/modal';
import { AuthForm } from '../../components/shared/auth-form/auth-form';
import { UserOrderRow, UserOrder } from '../../components/shared/user-order-row/user-order-row';

@Component({
  selector: 'app-style-guide',
  templateUrl: './style-guide.html',
  styleUrl: './style-guide.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgIcon,
    Button,
    Alert,
    FormInput,
    FormTextarea,
    FormSelect,
    ServiceCard,
    StatsCard,
    OrderInput,
    DashboardHeader,
    Header,
    Footer,
    Sidebar,
    MainContent,
    Modal,
    AuthForm,
    UserOrderRow
  ]
})
export class StyleGuide {
  // Form demo values
  protected readonly inputValue = signal<string>('');
  protected readonly textareaValue = signal<string>('');
  protected readonly selectValue = signal<string>('');

  // Select options
  protected readonly selectOptions: SelectOption[] = [
    { value: 'instagram', label: 'Instagram' },
    { value: 'tiktok', label: 'TikTok' },
    { value: 'youtube', label: 'YouTube' },
    { value: 'twitter', label: 'Twitter' }
  ];

  // Service card demo
  protected readonly serviceDemo: ServiceCardData = {
    id: '1',
    name: 'Instagram',
    serviceCount: 156,
    slug: 'instagram'
  };

  // Stats card demo
  protected readonly statsDemo: StatsCardData = {
    value: '12,456',
    label: 'Orders Today',
    change: 12.5,
    changeLabel: 'vs yesterday'
  };

  // Color palette
  protected readonly colors = [
    { name: 'Background', variable: '--color-background', hex: '#0A0A0A' },
    { name: 'Text', variable: '--color-text', hex: '#FAFAFA' },
    { name: 'High Contrast', variable: '--color-high-contrast', hex: '#FFFFFF' },
    { name: 'Foreground', variable: '--color-foreground', hex: '#A1A1A1' },
    { name: 'Secondary', variable: '--color-secondary', hex: '#666666' },
    { name: 'Information', variable: '--color-information', hex: '#393939' },
    { name: 'Tiny Info', variable: '--color-tiny-info', hex: '#1C1C1C' },
    { name: 'Success', variable: '--color-success', hex: '#00DC33' },
    { name: 'Error', variable: '--color-error', hex: '#FF4444' },
    { name: 'Status Yellow', variable: '--color-status-yellow', hex: '#F0B100' },
    { name: 'Stats Blue', variable: '--color-stats-blue', hex: '#00A5FF' }
  ];

  // Typography scale
  protected readonly typographySizes = [
    { name: 'Title', size: '128px', variable: '--font-size-title' },
    { name: 'H1', size: '96px', variable: '--font-size-h1' },
    { name: 'H2', size: '64px', variable: '--font-size-h2' },
    { name: 'H3', size: '48px', variable: '--font-size-h3' },
    { name: 'H4', size: '32px', variable: '--font-size-h4' },
    { name: 'H5', size: '24px', variable: '--font-size-h5' },
    { name: 'H6', size: '20px', variable: '--font-size-h6' },
    { name: 'Body', size: '16px', variable: '--font-size-body' },
    { name: 'Caption', size: '14px', variable: '--font-size-caption' },
    { name: 'Small', size: '12px', variable: '--font-size-small' },
    { name: 'Tiny', size: '10px', variable: '--font-size-tiny' }
  ];

  // Sidebar demo items
  protected readonly sidebarItems: SidebarItem[] = [
    { label: 'Dashboard', path: '/admin' },
    { label: 'Orders', path: '/admin/orders' },
    { label: 'Users', path: '/admin/users' },
    { label: 'Services', path: '/admin/services' }
  ];

  // Modal state
  protected readonly isModalOpen = signal(false);
  protected readonly modalSize = signal<'sm' | 'md' | 'lg'>('md');

  // User orders demo
  protected readonly userOrders: UserOrder[] = [
    {
      id: 'ORD-001',
      serviceName: 'Instagram Followers',
      quantity: 1000,
      price: '$4.99',
      status: 'completed',
      createdAt: new Date()
    },
    {
      id: 'ORD-002',
      serviceName: 'TikTok Likes',
      quantity: 5000,
      price: '$12.50',
      status: 'processing',
      createdAt: new Date()
    },
    {
      id: 'ORD-003',
      serviceName: 'YouTube Views',
      quantity: 10000,
      price: '$25.00',
      status: 'pending',
      createdAt: new Date()
    },
    {
      id: 'ORD-004',
      serviceName: 'Twitter Followers',
      quantity: 500,
      price: '$3.99',
      status: 'cancelled',
      createdAt: new Date()
    },
    {
      id: 'ORD-005',
      serviceName: 'Instagram Likes',
      quantity: 2000,
      price: '$6.00',
      status: 'partial',
      createdAt: new Date()
    }
  ];

  protected onOrderSubmit(value: string): void {
    console.log('Order submitted:', value);
  }

  protected openModal(size: 'sm' | 'md' | 'lg'): void {
    this.modalSize.set(size);
    this.isModalOpen.set(true);
  }

  protected closeModal(): void {
    this.isModalOpen.set(false);
  }

  protected onAuthSubmit(data: unknown): void {
    console.log('Auth form submitted:', data);
  }

  protected onOrderRowClick(order: UserOrder): void {
    console.log('Order clicked:', order);
  }
}
