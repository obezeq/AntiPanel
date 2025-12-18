import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NgIconComponent } from '@ng-icons/core';

export interface SidebarItem {
  label: string;
  path: string;
  icon?: string;
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, NgIconComponent]
})
export class Sidebar {
  /** Sidebar title */
  readonly title = input<string>('ADMIN');

  /** Navigation items */
  readonly items = input<SidebarItem[]>([
    { label: 'Dashboard', path: '/admin', icon: 'matDashboard' },
    { label: 'Orders', path: '/admin/orders', icon: 'matShoppingCart' }
  ]);

  /** Whether the sidebar is open (mobile) */
  readonly isOpen = input<boolean>(false);

  /** Emits when sidebar close is requested */
  readonly closeRequest = output<void>();

  protected onItemClick(): void {
    this.closeRequest.emit();
  }
}
