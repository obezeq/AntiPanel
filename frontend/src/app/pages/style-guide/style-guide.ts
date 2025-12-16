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
    DashboardHeader
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

  protected onOrderSubmit(value: string): void {
    console.log('Order submitted:', value);
  }
}
