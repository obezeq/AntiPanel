import {
  ChangeDetectionStrategy,
  Component,
  PLATFORM_ID,
  inject,
  signal
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgIcon } from '@ng-icons/core';
import { ThemeService } from '../../services/theme.service';

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
import { HeaderVariant } from '../../components/layout/header/header';
// New order components
import { DashboardSectionHeader } from '../../components/shared/dashboard-section-header/dashboard-section-header';
import { RecentOrderCard, RecentOrderData } from '../../components/shared/recent-order-card/recent-order-card';
import { OrderCard, OrderCardData } from '../../components/shared/order-card/order-card';
import { OrderReady, OrderReadyData } from '../../components/shared/order-ready/order-ready';
import { OrderPlaced } from '../../components/shared/order-placed/order-placed';
import { ServiceItemCard, ServiceItemData } from '../../components/shared/service-item-card/service-item-card';
import { AdminOrderTable, AdminOrder } from '../../components/shared/admin-order-table/admin-order-table';
import { OrderPagination } from '../../components/shared/order-pagination/order-pagination';
import { OrderFilters, FilterCategory, SortOrder } from '../../components/shared/order-filters/order-filters';

@Component({
  selector: 'app-style-guide',
  templateUrl: './style-guide.html',
  styleUrl: './style-guide.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
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
    UserOrderRow,
    // New order components
    DashboardSectionHeader,
    RecentOrderCard,
    OrderCard,
    OrderReady,
    OrderPlaced,
    ServiceItemCard,
    AdminOrderTable,
    OrderPagination,
    OrderFilters
  ]
})
export class StyleGuide {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  // Theme service (global)
  protected readonly themeService = inject(ThemeService);

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

  // Service cards demo - 8 plataformas según Figma
  protected readonly serviceDemos: ServiceCardData[] = [
    { id: '1', name: 'Instagram', serviceCount: 3, slug: 'instagram', icon: 'iconoirInstagram' },
    { id: '2', name: 'TikTok', serviceCount: 5, slug: 'tiktok', icon: 'iconoirTiktok' },
    { id: '3', name: 'YouTube', serviceCount: 4, slug: 'youtube', icon: 'iconoirYoutube' },
    { id: '4', name: 'Twitter', serviceCount: 2, slug: 'twitter', icon: 'iconoirTwitter' },
    { id: '5', name: 'Facebook', serviceCount: 6, slug: 'facebook', icon: 'iconoirFacebook' },
    { id: '6', name: 'Discord', serviceCount: 3, slug: 'discord', icon: 'iconoirDiscord' },
    { id: '7', name: 'Snapchat', serviceCount: 2, slug: 'snapchat', icon: 'simpleSnapchat' },
    { id: '8', name: 'LinkedIn', serviceCount: 4, slug: 'linkedin', icon: 'iconoirLinkedin' }
  ];

  // Stats cards demo - 4 variantes según Figma
  protected readonly statsDemos: StatsCardData[] = [
    { icon: 'matShowChart', title: 'TOTAL', value: '369', label: 'All time orders' },
    { icon: 'matSchedule', title: 'PENDING', value: '3', label: 'In progress' },
    { icon: 'matCheckCircle', title: 'STATUS', value: '333', label: 'Completed' },
    { icon: 'matQueryStats', title: 'STATUS', value: '33', label: 'This month' }
  ];

  // Color palette with descriptions
  protected readonly colors = [
    { name: 'Background', variable: '--color-background', hex: '#0A0A0A', description: 'The foundation. Creates depth and provides contrast for all interface elements.' },
    { name: 'Text', variable: '--color-text', hex: '#FAFAFA', description: 'Primary content. Ensures readability across all contexts.' },
    { name: 'High Contrast', variable: '--color-high-contrast', hex: '#FFFFFF', description: 'Maximum emphasis. Reserved for critical actions and focal points.' },
    { name: 'Foreground', variable: '--color-foreground', hex: '#A1A1A1', description: 'Secondary content. Descriptions, captions, and supporting information.' },
    { name: 'Secondary', variable: '--color-secondary', hex: '#666666', description: 'Subtle elements. Borders, dividers, and inactive states.' },
    { name: 'Information', variable: '--color-information', hex: '#393939', description: 'Neutral surfaces. Input fields, cards, and containers.' },
    { name: 'Tiny Info', variable: '--color-tiny-info', hex: '#1C1C1C', description: 'Subtle backgrounds. Hover states and grouped sections.' },
    { name: 'Success', variable: '--color-success', hex: '#00DC33', description: 'Positive feedback. Confirmations, completed actions, and valid states.' },
    { name: 'Error', variable: '--color-error', hex: '#FF4444', description: 'Critical alerts. Errors, warnings, and destructive actions.' },
    { name: 'Status Yellow', variable: '--color-status-yellow', hex: '#F0B100', description: 'Pending states. In-progress items and cautionary notices.' },
    { name: 'Stats Blue', variable: '--color-stats-blue', hex: '#00A5FF', description: 'Informational highlights. Links, stats, and interactive elements.' }
  ];

  // Copy toast state
  protected readonly showCopyToast = signal(false);
  protected readonly copiedColor = signal('');
  protected readonly toastPosition = signal({ x: 0, y: 0 });

  // Copy color hex to clipboard with toast notification
  protected copyColorToClipboard(hex: string, event: Event): void {
    if (!this.isBrowser) return;
    navigator.clipboard.writeText(hex);

    // Set toast position - use mouse position or element center for keyboard
    if (event instanceof MouseEvent) {
      this.toastPosition.set({ x: event.clientX, y: event.clientY });
    } else {
      const target = event.target as HTMLElement;
      const rect = target.getBoundingClientRect();
      this.toastPosition.set({ x: rect.left + rect.width / 2, y: rect.top });
    }

    this.copiedColor.set(hex);
    this.showCopyToast.set(true);

    // Hide toast after animation
    setTimeout(() => {
      this.showCopyToast.set(false);
    }, 1500);
  }

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

  // Font weights with professional descriptions
  protected readonly fontWeights = [
    {
      name: 'Light',
      weight: 300,
      variable: '--font-weight-light',
      description: 'Elegant display. Large headlines and decorative text where subtlety creates sophistication.'
    },
    {
      name: 'Regular',
      weight: 400,
      variable: '--font-weight-regular',
      description: 'Body text. The baseline for comfortable extended reading and general content.'
    },
    {
      name: 'Medium',
      weight: 500,
      variable: '--font-weight-medium',
      description: 'Subtle emphasis. Labels and secondary information that needs distinction without dominance.'
    },
    {
      name: 'Semibold',
      weight: 600,
      variable: '--font-weight-semibold',
      description: 'Interface elements. Navigation, buttons, and interactive components that guide action.'
    },
    {
      name: 'Bold',
      weight: 700,
      variable: '--font-weight-bold',
      description: 'Primary hierarchy. Headlines and key information that anchors the visual structure.'
    },
    {
      name: 'Extrabold',
      weight: 800,
      variable: '--font-weight-extrabold',
      description: 'Maximum impact. Hero sections and statements that demand immediate attention.'
    }
  ];

  // Sidebar demo items
  protected readonly sidebarItems: SidebarItem[] = [
    { label: 'Dashboard', path: '/admin' },
    { label: 'Orders', path: '/admin/orders' }
  ];

  // Modal state
  protected readonly isModalOpen = signal(false);
  protected readonly modalSize = signal<'sm' | 'md' | 'lg'>('md');

  // User orders demo (user variant)
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

  // Admin orders demo (admin variant with extended fields)
  protected readonly adminOrders: UserOrder[] = [
    {
      id: '12345',
      serviceName: 'Instagram Followers [Real]',
      quantity: 5000,
      price: '$24.99',
      status: 'completed',
      createdAt: new Date('2024-12-15'),
      username: 'john_doe',
      link: 'https://instagram.com/example_user',
      startCount: 1250,
      remains: 0
    },
    {
      id: '12346',
      serviceName: 'TikTok Views [Premium]',
      quantity: 10000,
      price: '$15.00',
      status: 'processing',
      createdAt: new Date('2024-12-14'),
      username: 'jane_smith',
      link: 'https://tiktok.com/@example_video',
      startCount: 5000,
      remains: 3500
    },
    {
      id: '12347',
      serviceName: 'YouTube Subscribers',
      quantity: 1000,
      price: '$89.00',
      status: 'pending',
      createdAt: new Date('2024-12-13'),
      username: 'mike_wilson',
      link: 'https://youtube.com/c/example_channel',
      startCount: 0,
      remains: 1000
    },
    {
      id: '12348',
      serviceName: 'Twitter Retweets',
      quantity: 500,
      price: '$8.50',
      status: 'cancelled',
      createdAt: new Date('2024-12-12'),
      username: 'sarah_jones',
      link: 'https://twitter.com/example/status/123',
      startCount: 0,
      remains: 500
    },
    {
      id: '12349',
      serviceName: 'Facebook Page Likes',
      quantity: 2000,
      price: '$35.00',
      status: 'partial',
      createdAt: new Date('2024-12-11'),
      username: 'alex_brown',
      link: 'https://facebook.com/example_page',
      startCount: 800,
      remains: 450
    }
  ];

  // Recent orders demo (dashboard)
  protected readonly recentOrders: RecentOrderData[] = [
    {
      id: '1',
      serviceName: 'Instagram Followers',
      quantity: 3000,
      price: '$33.00',
      status: 'completed'
    },
    {
      id: '2',
      serviceName: 'TikTok Views',
      quantity: 5000,
      price: '$15.00',
      status: 'processing'
    },
    {
      id: '3',
      serviceName: 'YouTube Subscribers',
      quantity: 1000,
      price: '$89.00',
      status: 'pending'
    }
  ];

  // Full order cards demo (orders page)
  protected readonly fullOrders: OrderCardData[] = [
    {
      id: '00001',
      serviceName: 'EXAMPLE FOLLOWERS. FAST. HIGH QUALITY.',
      targetUrl: 'https://example.com/username',
      quantity: 3000,
      remains: 0,
      price: 3.69,
      status: 'completed',
      createdAt: new Date('2025-03-03T13:00:00')
    },
    {
      id: '00002',
      serviceName: 'Instagram Likes [Premium]',
      targetUrl: 'https://instagram.com/p/example123',
      quantity: 5000,
      remains: 2500,
      price: 12.50,
      status: 'processing',
      createdAt: new Date('2025-03-02T10:30:00')
    },
    {
      id: '00003',
      serviceName: 'YouTube Views [High Retention]',
      targetUrl: 'https://youtube.com/watch?v=example',
      quantity: 10000,
      remains: 10000,
      price: 25.00,
      status: 'pending',
      createdAt: new Date('2025-03-01T08:15:00')
    },
    {
      id: '00004',
      serviceName: 'Twitter Followers',
      targetUrl: 'https://twitter.com/example_user',
      quantity: 500,
      remains: 500,
      price: 3.99,
      status: 'cancelled',
      createdAt: new Date('2025-02-28T16:45:00')
    },
    {
      id: '00005',
      serviceName: 'Facebook Page Likes',
      targetUrl: 'https://facebook.com/example_page',
      quantity: 2000,
      remains: 450,
      price: 35.00,
      status: 'partial',
      createdAt: new Date('2025-02-27T14:20:00')
    }
  ];

  protected onOrderSubmit(_value: string): void {
    // Demo handler - no-op
  }

  protected openModal(size: 'sm' | 'md' | 'lg'): void {
    this.modalSize.set(size);
    this.isModalOpen.set(true);
  }

  protected closeModal(): void {
    this.isModalOpen.set(false);
  }

  protected onAuthSubmit(_data: unknown): void {
    // Demo handler - no-op
  }

  protected onOrderRowClick(_order: UserOrder): void {
    // Demo handler - no-op
  }

  // New order component handlers
  protected onRecentOrderClick(_order: RecentOrderData): void {
    // Demo handler - no-op
  }

  protected onRecentOrderAgain(_order: RecentOrderData): void {
    // Demo handler - no-op
  }

  protected onOrderCardClick(_order: OrderCardData): void {
    // Demo handler - no-op
  }

  protected onOrderAgain(_order: OrderCardData): void {
    // Demo handler - no-op
  }

  protected onRefill(_order: OrderCardData): void {
    // Demo handler - no-op
  }

  // Order Ready demo data
  protected readonly orderReadyDemo: OrderReadyData = {
    matchPercentage: 93,
    service: {
      icon: 'iconoirInstagram',
      platform: 'INSTAGRAM',
      type: 'Followers',
      quality: 'HIGH Quality',
      speed: 'FAST Speed'
    },
    quantity: 3000,
    price: '$3.3'
  };

  protected readonly orderReadyWithTargetDemo: OrderReadyData = {
    matchPercentage: 93,
    service: {
      icon: 'iconoirInstagram',
      platform: 'INSTAGRAM',
      type: 'Followers',
      quality: 'HIGH Quality',
      speed: 'FAST Speed'
    },
    quantity: 3000,
    price: '$3.3',
    target: '@username'
  };

  // Order Placed state
  protected readonly isOrderPlacedOpen = signal(false);

  // Order Ready handlers
  protected onExploreMore(): void {
    // Demo handler - no-op
  }

  protected onMorePlatform(_platform: string): void {
    // Demo handler - no-op
  }

  protected onPlaceOrder(_data: OrderReadyData): void {
    // Demo handler - no-op
  }

  // Order Placed handlers
  protected openOrderPlacedModal(): void {
    this.isOrderPlacedOpen.set(true);
  }

  protected closeOrderPlacedModal(): void {
    this.isOrderPlacedOpen.set(false);
  }

  // Service Item Card demo data
  protected readonly serviceItemDemos: ServiceItemData[] = [
    {
      id: '1',
      name: 'Instagram Followers',
      price: 0.3,
      priceUnit: 'PER 1K',
      quality: 'QUALITY',
      speed: 'SPEED'
    },
    {
      id: '2',
      name: 'TikTok Likes',
      price: 0.5,
      priceUnit: 'PER 1K',
      quality: 'PREMIUM',
      speed: 'INSTANT'
    },
    {
      id: '3',
      name: 'YouTube Views',
      price: 1.2,
      priceUnit: 'PER 1K',
      quality: 'HIGH',
      speed: 'FAST'
    }
  ];

  // Service Item Card handlers
  protected onServiceItemQuickOrder(_service: ServiceItemData): void {
    // Demo handler - no-op
  }

  protected onServiceItemClick(_service: ServiceItemData): void {
    // Demo handler - no-op
  }

  // Admin Order Table demo data
  protected readonly adminOrderTableData: AdminOrder[] = [
    {
      id: '12345',
      status: 'pending',
      username: 'John Doe',
      userHandle: '@johndoe',
      serviceName: 'Instagram Followers',
      serviceId: '101',
      link: 'https://instagram.com/example_user',
      quantity: 5000,
      remains: 5000,
      cost: 12.50,
      sale: 24.99,
      profit: 12.49,
      createdAt: new Date('2024-12-15T10:30:00'),
      description: 'Real followers with high retention rate',
      providerId: 'PROV-001',
      providerServiceId: 'SVC-1001',
      providerOrderId: 'EXT-98765'
    },
    {
      id: '12346',
      status: 'processing',
      username: 'Jane Smith',
      userHandle: '@janesmith',
      serviceName: 'TikTok Views',
      serviceId: '205',
      link: 'https://tiktok.com/@example_video',
      quantity: 10000,
      remains: 3500,
      cost: 8.00,
      sale: 15.00,
      profit: 7.00,
      createdAt: new Date('2024-12-14T14:45:00'),
      description: 'Premium TikTok views with fast delivery',
      providerId: 'PROV-002',
      providerServiceId: 'SVC-2005',
      providerOrderId: 'EXT-98766'
    },
    {
      id: '12347',
      status: 'completed',
      username: 'Mike Wilson',
      userHandle: '@mikewilson',
      serviceName: 'YouTube Subscribers',
      serviceId: '301',
      link: 'https://youtube.com/c/example_channel',
      quantity: 1000,
      remains: 0,
      cost: 45.00,
      sale: 89.00,
      profit: 44.00,
      createdAt: new Date('2024-12-13T09:15:00'),
      description: 'High quality subscribers with engagement',
      providerId: 'PROV-001',
      providerServiceId: 'SVC-3001',
      providerOrderId: 'EXT-98767'
    },
    {
      id: '12348',
      status: 'cancelled',
      username: 'Sarah Jones',
      userHandle: '@sarahjones',
      serviceName: 'Twitter Retweets',
      serviceId: '402',
      link: 'https://twitter.com/example/status/123',
      quantity: 500,
      remains: 500,
      cost: 4.00,
      sale: 8.50,
      profit: 4.50,
      createdAt: new Date('2024-12-12T16:20:00'),
      description: 'Fast retweets from real accounts',
      providerId: 'PROV-003',
      providerServiceId: 'SVC-4002',
      providerOrderId: 'EXT-98768'
    },
    {
      id: '12349',
      status: 'partial',
      username: 'Alex Brown',
      userHandle: '@alexbrown',
      serviceName: 'Facebook Page Likes',
      serviceId: '503',
      link: 'https://facebook.com/example_page',
      quantity: 2000,
      remains: 450,
      cost: 18.00,
      sale: 35.00,
      profit: 17.00,
      createdAt: new Date('2024-12-11T11:00:00'),
      description: 'Page likes from worldwide audience',
      providerId: 'PROV-002',
      providerServiceId: 'SVC-5003',
      providerOrderId: 'EXT-98769'
    }
  ];

  // Admin Order Table handlers
  protected onAdminOrderClick(_order: AdminOrder): void {
    // Demo handler - no-op
  }

  // =========================================================================
  // ORDER PAGINATION & FILTERS DEMO
  // =========================================================================

  /** Pagination demo state */
  protected readonly demoPaginationPage = signal(1);
  protected readonly demoPaginationTotal = signal(33);
  protected readonly demoPaginationSize = signal(10);

  /** Filters demo state */
  protected readonly demoFilterCategory = signal<FilterCategory>('ALL');
  protected readonly demoFilterSort = signal<SortOrder>('latest');
  protected readonly demoFilterSearch = signal('');

  /** Pagination handlers */
  protected onPaginationPageChange(page: number): void {
    this.demoPaginationPage.set(page);
  }

  protected onPaginationSizeChange(size: number): void {
    this.demoPaginationSize.set(size);
    this.demoPaginationPage.set(1); // Reset to page 1 when size changes
  }

  /** Filters handlers */
  protected onFilterCategoryChange(category: FilterCategory): void {
    this.demoFilterCategory.set(category);
  }

  protected onFilterSortChange(order: SortOrder): void {
    this.demoFilterSort.set(order);
  }

  protected onFilterSearchChange(query: string): void {
    this.demoFilterSearch.set(query);
  }
}
