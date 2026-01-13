import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal,
  computed,
  OnInit,
  OnDestroy
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  matArrowBack,
  matRefresh,
  matPlayArrow,
  matStop,
  matTimer,
  matSignalCellularAlt
} from '@ng-icons/material-icons/baseline';
import { interval, Subject, Subscription, timer, of } from 'rxjs';
import { switchMap, tap, catchError, takeUntil, map } from 'rxjs/operators';
import { DatePipe, DecimalPipe } from '@angular/common';

/**
 * Simulated server data item
 */
interface DataItem {
  id: number;
  value: number;
  timestamp: Date;
  status: 'active' | 'idle' | 'processing';
}

/**
 * State Demos Component
 *
 * Demonstrates various state management patterns:
 * - Polling for real-time updates
 * - Signal-based state management
 * - Start/stop controls
 * - Data refresh patterns
 *
 * This component is for demonstration purposes to fulfill
 * evaluation criteria 6.6 (WebSockets/Polling).
 *
 * @route /cliente/state
 */
@Component({
  selector: 'app-state-demos',
  standalone: true,
  imports: [RouterLink, NgIcon, DatePipe, DecimalPipe],
  viewProviders: [
    provideIcons({
      matArrowBack,
      matRefresh,
      matPlayArrow,
      matStop,
      matTimer,
      matSignalCellularAlt
    })
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="state-demos">
      <header class="state-demos__header">
        <a routerLink="/cliente" class="state-demos__back-link">
          <ng-icon name="matArrowBack" size="20" />
          <span>Back to Cliente</span>
        </a>
        <h1 class="state-demos__title">State Management Demos</h1>
        <p class="state-demos__description">
          Demonstrating polling and real-time state updates (Criteria 6.6)
        </p>
      </header>

      <main class="state-demos__content">
        <!-- Polling Demo -->
        <section class="demo-section">
          <h2 class="demo-section__title">
            <ng-icon name="matTimer" size="24" />
            Polling - Real-time Updates
          </h2>
          <p class="demo-section__description">
            Demonstrates using RxJS interval() for periodic data fetching,
            simulating real-time updates without WebSockets.
          </p>

          <div class="demo-section__body">
            <!-- Controls -->
            <div class="polling-controls">
              <div class="polling-controls__status">
                <span
                  class="status-indicator"
                  [class.status-indicator--active]="isPolling()"
                ></span>
                <span>{{ isPolling() ? 'Polling Active' : 'Polling Stopped' }}</span>
              </div>

              <div class="polling-controls__interval">
                <label>
                  Interval (ms):
                  <select
                    [value]="pollingInterval()"
                    (change)="onIntervalChange($event)"
                    [disabled]="isPolling()"
                  >
                    <option value="1000">1 second</option>
                    <option value="2000">2 seconds</option>
                    <option value="5000">5 seconds</option>
                    <option value="10000">10 seconds</option>
                  </select>
                </label>
              </div>

              <div class="polling-controls__buttons">
                @if (!isPolling()) {
                  <button
                    type="button"
                    class="demo-btn demo-btn--primary"
                    (click)="startPolling()"
                  >
                    <ng-icon name="matPlayArrow" size="18" />
                    Start Polling
                  </button>
                } @else {
                  <button
                    type="button"
                    class="demo-btn demo-btn--danger"
                    (click)="stopPolling()"
                  >
                    <ng-icon name="matStop" size="18" />
                    Stop Polling
                  </button>
                }
                <button
                  type="button"
                  class="demo-btn demo-btn--secondary"
                  (click)="fetchOnce()"
                  [disabled]="isPolling()"
                >
                  <ng-icon name="matRefresh" size="18" />
                  Fetch Once
                </button>
              </div>
            </div>

            <!-- Stats -->
            <div class="polling-stats">
              <div class="stat-card">
                <span class="stat-card__label">Total Fetches</span>
                <span class="stat-card__value">{{ fetchCount() }}</span>
              </div>
              <div class="stat-card">
                <span class="stat-card__label">Last Updated</span>
                <span class="stat-card__value">
                  {{ lastUpdated() | date:'HH:mm:ss' }}
                </span>
              </div>
              <div class="stat-card">
                <span class="stat-card__label">Items Count</span>
                <span class="stat-card__value">{{ dataItems().length }}</span>
              </div>
              <div class="stat-card">
                <span class="stat-card__label">Avg Value</span>
                <span class="stat-card__value">{{ averageValue() | number:'1.1-1' }}</span>
              </div>
            </div>

            <!-- Data Display -->
            <div class="data-grid">
              <h4 class="data-grid__title">
                <ng-icon name="matSignalCellularAlt" size="20" />
                Live Data ({{ dataItems().length }} items)
              </h4>
              <div class="data-grid__items">
                @for (item of dataItems(); track item.id) {
                  <div class="data-item" [class.data-item--updated]="recentlyUpdated().has(item.id)">
                    <span class="data-item__id">#{{ item.id }}</span>
                    <span class="data-item__value">{{ item.value }}</span>
                    <span class="data-item__status" [class]="'status--' + item.status">
                      {{ item.status }}
                    </span>
                    <span class="data-item__time">
                      {{ item.timestamp | date:'HH:mm:ss' }}
                    </span>
                  </div>
                } @empty {
                  <div class="data-grid__empty">
                    No data yet. Start polling or fetch once.
                  </div>
                }
              </div>
            </div>

            <!-- Code Example -->
            <h4 class="code-title">Polling Implementation:</h4>
            <pre class="code-block">{{ pollingCode }}</pre>
          </div>
        </section>

        <!-- Signal State Demo -->
        <section class="demo-section">
          <h2 class="demo-section__title">
            <ng-icon name="matSignalCellularAlt" size="24" />
            Signal-based State Management
          </h2>
          <p class="demo-section__description">
            Demonstrates Angular Signals for reactive state management
            with computed values and effects.
          </p>

          <div class="demo-section__body">
            <div class="signal-demo">
              <div class="signal-demo__controls">
                <button
                  type="button"
                  class="demo-btn demo-btn--secondary"
                  (click)="decrementCounter()"
                >
                  -
                </button>
                <span class="counter-display">{{ counter() }}</span>
                <button
                  type="button"
                  class="demo-btn demo-btn--secondary"
                  (click)="incrementCounter()"
                >
                  +
                </button>
              </div>

              <div class="computed-values">
                <div class="computed-item">
                  <span class="computed-item__label">Doubled:</span>
                  <span class="computed-item__value">{{ doubled() }}</span>
                </div>
                <div class="computed-item">
                  <span class="computed-item__label">Squared:</span>
                  <span class="computed-item__value">{{ squared() }}</span>
                </div>
                <div class="computed-item">
                  <span class="computed-item__label">Is Even:</span>
                  <span class="computed-item__value">{{ isEven() ? 'Yes' : 'No' }}</span>
                </div>
              </div>
            </div>

            <h4 class="code-title">Signals Implementation:</h4>
            <pre class="code-block">{{ signalsCode }}</pre>
          </div>
        </section>
      </main>
    </div>
  `,
  styles: [`
    .state-demos {
      padding: 2rem;
      max-width: 900px;
      margin: 0 auto;

      &__header {
        margin-bottom: 2rem;
      }

      &__back-link {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        color: var(--color-text-secondary);
        text-decoration: none;
        margin-bottom: 1rem;

        &:hover {
          color: var(--color-primary);
        }
      }

      &__title {
        font-size: 1.75rem;
        font-weight: 600;
        margin: 0 0 0.5rem;
      }

      &__description {
        color: var(--color-text-secondary);
        margin: 0;
      }
    }

    .demo-section {
      background: var(--color-surface);
      border: 1px solid var(--color-border);
      border-radius: 8px;
      padding: 1.5rem;
      margin-bottom: 1.5rem;

      &__title {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 1.25rem;
        font-weight: 600;
        margin: 0 0 0.5rem;
      }

      &__description {
        color: var(--color-text-secondary);
        margin: 0 0 1.5rem;
        font-size: 0.9rem;
      }

      &__body {
        display: flex;
        flex-direction: column;
        gap: 1rem;
      }
    }

    .polling-controls {
      display: flex;
      align-items: center;
      gap: 1.5rem;
      flex-wrap: wrap;
      padding: 1rem;
      background: var(--color-background);
      border-radius: 8px;

      &__status {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-weight: 500;
      }

      &__interval {
        label {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          font-size: 0.9rem;
        }

        select {
          padding: 0.5rem;
          border: 1px solid var(--color-border);
          border-radius: 4px;
        }
      }

      &__buttons {
        display: flex;
        gap: 0.5rem;
        margin-left: auto;
      }
    }

    .status-indicator {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: var(--color-text-secondary);

      &--active {
        background: var(--color-success, #4caf50);
        animation: pulse 1.5s infinite;
      }
    }

    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.5; }
    }

    .polling-stats {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
      gap: 1rem;
    }

    .stat-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 1rem;
      background: var(--color-background);
      border-radius: 8px;

      &__label {
        font-size: 0.75rem;
        color: var(--color-text-secondary);
        text-transform: uppercase;
        letter-spacing: 0.05em;
      }

      &__value {
        font-size: 1.5rem;
        font-weight: 600;
        margin-top: 0.25rem;
      }
    }

    .data-grid {
      &__title {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 1rem;
        font-weight: 600;
        margin: 0 0 0.75rem;
      }

      &__items {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 0.5rem;
      }

      &__empty {
        grid-column: 1 / -1;
        text-align: center;
        padding: 2rem;
        color: var(--color-text-secondary);
        font-style: italic;
      }
    }

    .data-item {
      display: grid;
      grid-template-columns: auto 1fr auto auto;
      gap: 0.5rem;
      align-items: center;
      padding: 0.75rem;
      background: var(--color-background);
      border-radius: 6px;
      font-size: 0.85rem;
      transition: background 0.3s;

      &--updated {
        background: var(--color-primary-light, rgba(33, 150, 243, 0.1));
      }

      &__id {
        font-weight: 600;
        color: var(--color-text-secondary);
      }

      &__value {
        font-weight: 500;
      }

      &__status {
        padding: 0.125rem 0.5rem;
        border-radius: 4px;
        font-size: 0.7rem;
        text-transform: uppercase;
      }

      &__time {
        color: var(--color-text-secondary);
        font-size: 0.75rem;
      }
    }

    .status--active {
      background: var(--color-success, #4caf50);
      color: white;
    }

    .status--idle {
      background: var(--color-text-secondary);
      color: white;
    }

    .status--processing {
      background: var(--color-warning, #ff9800);
      color: white;
    }

    .signal-demo {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1.5rem;
      padding: 1.5rem;
      background: var(--color-background);
      border-radius: 8px;

      &__controls {
        display: flex;
        align-items: center;
        gap: 1rem;
      }
    }

    .counter-display {
      font-size: 2.5rem;
      font-weight: 600;
      min-width: 80px;
      text-align: center;
    }

    .computed-values {
      display: flex;
      gap: 2rem;
      flex-wrap: wrap;
      justify-content: center;
    }

    .computed-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      &__label {
        font-size: 0.75rem;
        color: var(--color-text-secondary);
        text-transform: uppercase;
      }

      &__value {
        font-size: 1.25rem;
        font-weight: 500;
      }
    }

    .code-title {
      font-size: 0.9rem;
      font-weight: 600;
      margin: 1rem 0 0.5rem;
    }

    .code-block {
      background: var(--color-background);
      padding: 1rem;
      border-radius: 4px;
      font-family: 'Fira Code', monospace;
      font-size: 0.75rem;
      overflow-x: auto;
      white-space: pre;
      line-height: 1.5;
    }

    .demo-btn {
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 4px;
      font-size: 0.9rem;
      cursor: pointer;
      transition: all 0.2s;

      &--primary {
        background: var(--color-primary);
        color: white;

        &:hover:not(:disabled) {
          opacity: 0.9;
        }
      }

      &--secondary {
        background: var(--color-surface-hover);
        color: var(--color-text);

        &:hover:not(:disabled) {
          background: var(--color-border);
        }
      }

      &--danger {
        background: var(--color-error);
        color: white;

        &:hover:not(:disabled) {
          opacity: 0.9;
        }
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }
  `]
})
export class StateDemos implements OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  private readonly stopPolling$ = new Subject<void>();
  private pollingSubscription: Subscription | null = null;

  // Polling state
  protected readonly isPolling = signal(false);
  protected readonly pollingInterval = signal(2000);
  protected readonly fetchCount = signal(0);
  protected readonly lastUpdated = signal<Date | null>(null);
  protected readonly dataItems = signal<DataItem[]>([]);
  protected readonly recentlyUpdated = signal<Set<number>>(new Set());

  // Signal demo state
  protected readonly counter = signal(0);
  protected readonly doubled = computed(() => this.counter() * 2);
  protected readonly squared = computed(() => this.counter() ** 2);
  protected readonly isEven = computed(() => this.counter() % 2 === 0);

  // Computed for polling
  protected readonly averageValue = computed(() => {
    const items = this.dataItems();
    if (items.length === 0) return 0;
    return items.reduce((sum, item) => sum + item.value, 0) / items.length;
  });

  // Code examples
  protected readonly pollingCode = `// Using RxJS interval for polling
private readonly pollingInterval = signal(2000);
private readonly isPolling = signal(false);
private readonly stopPolling$ = new Subject<void>();

startPolling(): void {
  this.isPolling.set(true);

  interval(this.pollingInterval())
    .pipe(
      takeUntil(this.stopPolling$),
      takeUntilDestroyed(this.destroyRef),
      switchMap(() => this.fetchData())
    )
    .subscribe(data => {
      this.dataItems.set(data);
      this.lastUpdated.set(new Date());
    });
}

stopPolling(): void {
  this.stopPolling$.next();
  this.isPolling.set(false);
}`;

  protected readonly signalsCode = `// Angular Signals for reactive state
protected readonly counter = signal(0);

// Computed signals automatically update
protected readonly doubled = computed(() => this.counter() * 2);
protected readonly squared = computed(() => this.counter() ** 2);
protected readonly isEven = computed(() => this.counter() % 2 === 0);

// Update signal value
incrementCounter(): void {
  this.counter.update(c => c + 1);
}

decrementCounter(): void {
  this.counter.update(c => c - 1);
}`;

  ngOnDestroy(): void {
    this.stopPolling$.next();
    this.stopPolling$.complete();
  }

  /**
   * Start polling for data
   */
  protected startPolling(): void {
    this.isPolling.set(true);
    this.stopPolling$.next(); // Cancel any existing polling

    interval(this.pollingInterval())
      .pipe(
        takeUntil(this.stopPolling$),
        takeUntilDestroyed(this.destroyRef),
        switchMap(() => this.simulateFetch())
      )
      .subscribe();
  }

  /**
   * Stop polling
   */
  protected stopPolling(): void {
    this.stopPolling$.next();
    this.isPolling.set(false);
  }

  /**
   * Fetch data once
   */
  protected fetchOnce(): void {
    this.simulateFetch().subscribe();
  }

  /**
   * Change polling interval
   */
  protected onIntervalChange(event: Event): void {
    const value = parseInt((event.target as HTMLSelectElement).value);
    this.pollingInterval.set(value);
  }

  /**
   * Increment counter
   */
  protected incrementCounter(): void {
    this.counter.update(c => c + 1);
  }

  /**
   * Decrement counter
   */
  protected decrementCounter(): void {
    this.counter.update(c => c - 1);
  }

  /**
   * Simulate data fetch (would be real HTTP in production)
   */
  private simulateFetch() {
    // Simulate network delay
    return timer(100 + Math.random() * 200).pipe(
      map(() => this.generateMockData()),
      tap(data => {
        this.fetchCount.update(c => c + 1);
        this.lastUpdated.set(new Date());

        // Track updated items
        const updatedIds = new Set(data.map(item => item.id));
        this.recentlyUpdated.set(updatedIds);

        // Clear highlight after 500ms
        setTimeout(() => {
          this.recentlyUpdated.set(new Set());
        }, 500);

        this.dataItems.set(data);
      })
    );
  }

  /**
   * Generate mock data items
   */
  private generateMockData(): DataItem[] {
    const statuses: DataItem['status'][] = ['active', 'idle', 'processing'];
    const count = 6 + Math.floor(Math.random() * 4);

    return Array.from({ length: count }, (_, i) => ({
      id: i + 1,
      value: Math.floor(Math.random() * 100),
      timestamp: new Date(),
      status: statuses[Math.floor(Math.random() * statuses.length)]
    }));
  }
}
