import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  signal
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient, HttpEventType, HttpHeaders, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { matArrowBack, matUpload, matCheck, matError, matInfo } from '@ng-icons/material-icons/baseline';

/**
 * HTTP Demos Component
 *
 * Demonstrates various HTTP communication patterns:
 * - FormData for file uploads
 * - Custom headers
 * - Query parameters
 * - Progress tracking
 *
 * This component is for demonstration purposes to fulfill
 * evaluation criteria 5.4 (Different Formats).
 *
 * @route /cliente/http
 */
@Component({
  selector: 'app-http-demos',
  standalone: true,
  imports: [RouterLink, NgIcon],
  viewProviders: [provideIcons({ matArrowBack, matUpload, matCheck, matError, matInfo })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="http-demos">
      <header class="http-demos__header">
        <a routerLink="/cliente" class="http-demos__back-link">
          <ng-icon name="matArrowBack" size="20" />
          <span>Back to Cliente</span>
        </a>
        <h1 class="http-demos__title">HTTP Communication Demos</h1>
        <p class="http-demos__description">
          Demonstrating different HTTP formats and configurations (Criteria 5.4)
        </p>
      </header>

      <main class="http-demos__content">
        <!-- FormData Demo -->
        <section class="demo-section">
          <h2 class="demo-section__title">
            <ng-icon name="matUpload" size="24" />
            FormData - File Upload
          </h2>
          <p class="demo-section__description">
            Demonstrates using FormData for multipart/form-data requests,
            typically used for file uploads.
          </p>

          <div class="demo-section__body">
            <div class="file-upload">
              <input
                type="file"
                id="file-input"
                class="file-upload__input"
                (change)="onFileSelected($event)"
                accept="image/*,.pdf,.doc,.docx"
              />
              <label for="file-input" class="file-upload__label">
                <ng-icon name="matUpload" size="32" />
                <span>Click to select a file</span>
                <span class="file-upload__hint">Images, PDF, or documents</span>
              </label>
            </div>

            @if (selectedFile()) {
              <div class="file-info">
                <h4 class="file-info__title">Selected File:</h4>
                <dl class="file-info__details">
                  <dt>Name:</dt>
                  <dd>{{ selectedFile()?.name }}</dd>
                  <dt>Type:</dt>
                  <dd>{{ selectedFile()?.type || 'Unknown' }}</dd>
                  <dt>Size:</dt>
                  <dd>{{ formatFileSize(selectedFile()?.size || 0) }}</dd>
                </dl>

                <h4 class="file-info__title">FormData Structure:</h4>
                <pre class="code-block">{{ formDataStructure() }}</pre>

                <button
                  type="button"
                  class="demo-btn demo-btn--primary"
                  (click)="simulateUpload()"
                  [disabled]="isUploading()"
                >
                  @if (isUploading()) {
                    Uploading... {{ uploadProgress() }}%
                  } @else {
                    Simulate Upload
                  }
                </button>

                @if (uploadProgress() > 0) {
                  <div class="progress-bar">
                    <div
                      class="progress-bar__fill"
                      [style.width.%]="uploadProgress()"
                    ></div>
                  </div>
                }
              </div>
            }
          </div>
        </section>

        <!-- Query Parameters Demo -->
        <section class="demo-section">
          <h2 class="demo-section__title">
            <ng-icon name="matInfo" size="24" />
            Query Parameters (HttpParams)
          </h2>
          <p class="demo-section__description">
            Demonstrates building URLs with query parameters for filtering,
            pagination, and search.
          </p>

          <div class="demo-section__body">
            <div class="params-builder">
              <div class="params-builder__row">
                <label>
                  Page:
                  <input
                    type="number"
                    [value]="queryParams().page"
                    (input)="updateParam('page', $event)"
                    min="1"
                  />
                </label>
                <label>
                  Limit:
                  <input
                    type="number"
                    [value]="queryParams().limit"
                    (input)="updateParam('limit', $event)"
                    min="1"
                    max="100"
                  />
                </label>
              </div>
              <div class="params-builder__row">
                <label>
                  Search:
                  <input
                    type="text"
                    [value]="queryParams().search"
                    (input)="updateParam('search', $event)"
                    placeholder="Search term..."
                  />
                </label>
                <label>
                  Sort By:
                  <select (change)="updateParam('sortBy', $event)">
                    <option value="createdAt">Created At</option>
                    <option value="name">Name</option>
                    <option value="price">Price</option>
                  </select>
                </label>
              </div>
            </div>

            <h4 class="code-title">Generated HttpParams:</h4>
            <pre class="code-block">{{ httpParamsCode() }}</pre>

            <h4 class="code-title">Final URL:</h4>
            <pre class="code-block code-block--url">{{ generatedUrl() }}</pre>
          </div>
        </section>

        <!-- Custom Headers Demo -->
        <section class="demo-section">
          <h2 class="demo-section__title">
            <ng-icon name="matInfo" size="24" />
            Custom Headers (HttpHeaders)
          </h2>
          <p class="demo-section__description">
            Demonstrates adding custom HTTP headers for authentication,
            content type, API versioning, etc.
          </p>

          <div class="demo-section__body">
            <div class="headers-list">
              @for (header of customHeaders(); track header.key) {
                <div class="header-row">
                  <input
                    type="text"
                    [value]="header.key"
                    placeholder="Header name"
                    (input)="updateHeaderKey($index, $event)"
                  />
                  <input
                    type="text"
                    [value]="header.value"
                    placeholder="Header value"
                    (input)="updateHeaderValue($index, $event)"
                  />
                  <button
                    type="button"
                    class="demo-btn demo-btn--danger"
                    (click)="removeHeader($index)"
                  >
                    Remove
                  </button>
                </div>
              }
              <button
                type="button"
                class="demo-btn demo-btn--secondary"
                (click)="addHeader()"
              >
                + Add Header
              </button>
            </div>

            <h4 class="code-title">HttpHeaders Code:</h4>
            <pre class="code-block">{{ httpHeadersCode() }}</pre>
          </div>
        </section>
      </main>
    </div>
  `,
  styles: [`
    .http-demos {
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

    .file-upload {
      &__input {
        position: absolute;
        opacity: 0;
        width: 0;
        height: 0;
      }

      &__label {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 0.5rem;
        padding: 2rem;
        border: 2px dashed var(--color-border);
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          border-color: var(--color-primary);
          background: var(--color-surface-hover);
        }
      }

      &__hint {
        font-size: 0.8rem;
        color: var(--color-text-secondary);
      }
    }

    .file-info {
      padding: 1rem;
      background: var(--color-background);
      border-radius: 8px;

      &__title {
        font-size: 0.9rem;
        font-weight: 600;
        margin: 0 0 0.5rem;
      }

      &__details {
        display: grid;
        grid-template-columns: auto 1fr;
        gap: 0.25rem 1rem;
        margin: 0 0 1rem;
        font-size: 0.85rem;

        dt {
          color: var(--color-text-secondary);
        }

        dd {
          margin: 0;
        }
      }
    }

    .progress-bar {
      height: 8px;
      background: var(--color-border);
      border-radius: 4px;
      overflow: hidden;
      margin-top: 0.5rem;

      &__fill {
        height: 100%;
        background: var(--color-primary);
        transition: width 0.3s ease;
      }
    }

    .params-builder {
      display: flex;
      flex-direction: column;
      gap: 1rem;

      &__row {
        display: flex;
        gap: 1rem;
        flex-wrap: wrap;

        label {
          display: flex;
          flex-direction: column;
          gap: 0.25rem;
          flex: 1;
          min-width: 150px;
          font-size: 0.85rem;
          color: var(--color-text-secondary);
        }

        input, select {
          padding: 0.5rem;
          border: 1px solid var(--color-border);
          border-radius: 4px;
          font-size: 0.9rem;
        }
      }
    }

    .headers-list {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .header-row {
      display: flex;
      gap: 0.5rem;
      align-items: center;

      input {
        flex: 1;
        padding: 0.5rem;
        border: 1px solid var(--color-border);
        border-radius: 4px;
        font-size: 0.9rem;
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
      font-size: 0.8rem;
      overflow-x: auto;
      white-space: pre-wrap;
      word-break: break-all;

      &--url {
        color: var(--color-primary);
      }
    }

    .demo-btn {
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

        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }
      }

      &--secondary {
        background: var(--color-surface-hover);
        color: var(--color-text);

        &:hover {
          background: var(--color-border);
        }
      }

      &--danger {
        background: var(--color-error);
        color: white;
        padding: 0.25rem 0.5rem;
        font-size: 0.8rem;

        &:hover {
          opacity: 0.9;
        }
      }
    }
  `]
})
export class HttpDemos {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  // FormData Demo
  protected readonly selectedFile = signal<File | null>(null);
  protected readonly formDataStructure = signal('');
  protected readonly isUploading = signal(false);
  protected readonly uploadProgress = signal(0);

  // Query Params Demo
  protected readonly queryParams = signal({
    page: 1,
    limit: 20,
    search: '',
    sortBy: 'createdAt'
  });

  // Headers Demo
  protected readonly customHeaders = signal<Array<{ key: string; value: string }>>([
    { key: 'Authorization', value: 'Bearer <token>' },
    { key: 'X-API-Version', value: '2.0' },
    { key: 'Accept-Language', value: 'es-ES' }
  ]);

  /**
   * Handle file selection from input
   */
  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (file) {
      this.selectedFile.set(file);
      this.uploadProgress.set(0);

      // Build FormData structure preview
      const formData = new FormData();
      formData.append('file', file, file.name);
      formData.append('description', 'Demo upload');
      formData.append('timestamp', new Date().toISOString());

      this.formDataStructure.set(
`const formData = new FormData();
formData.append('file', selectedFile, '${file.name}');
formData.append('description', 'Demo upload');
formData.append('timestamp', '${new Date().toISOString()}');

// HTTP Request:
this.http.post('/api/upload', formData, {
  reportProgress: true,
  observe: 'events'
}).subscribe(event => {
  if (event.type === HttpEventType.UploadProgress) {
    const progress = Math.round(100 * event.loaded / event.total);
    console.log('Upload progress:', progress + '%');
  }
});`
      );
    }
  }

  /**
   * Simulate file upload with progress
   */
  protected simulateUpload(): void {
    this.isUploading.set(true);
    this.uploadProgress.set(0);

    // Simulate upload progress
    const interval = setInterval(() => {
      const current = this.uploadProgress();
      if (current >= 100) {
        clearInterval(interval);
        this.isUploading.set(false);
      } else {
        this.uploadProgress.set(current + 10);
      }
    }, 200);
  }

  /**
   * Format file size to human-readable string
   */
  protected formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  /**
   * Update query parameter
   */
  protected updateParam(key: string, event: Event): void {
    const target = event.target as HTMLInputElement | HTMLSelectElement;
    const value = target.type === 'number' ? parseInt(target.value) || 1 : target.value;
    this.queryParams.update(params => ({ ...params, [key]: value }));
  }

  /**
   * Generate HttpParams code
   */
  protected httpParamsCode(): string {
    const params = this.queryParams();
    return `const params = new HttpParams()
  .set('page', '${params.page}')
  .set('limit', '${params.limit}')
  .set('sortBy', '${params.sortBy}')${params.search ? `
  .set('search', '${params.search}')` : ''};

this.http.get('/api/items', { params }).subscribe();`;
  }

  /**
   * Generate full URL with query params
   */
  protected generatedUrl(): string {
    const params = this.queryParams();
    const queryString = new URLSearchParams({
      page: params.page.toString(),
      limit: params.limit.toString(),
      sortBy: params.sortBy,
      ...(params.search ? { search: params.search } : {})
    }).toString();
    return `/api/items?${queryString}`;
  }

  /**
   * Add new header
   */
  protected addHeader(): void {
    this.customHeaders.update(headers => [
      ...headers,
      { key: '', value: '' }
    ]);
  }

  /**
   * Remove header at index
   */
  protected removeHeader(index: number): void {
    this.customHeaders.update(headers =>
      headers.filter((_, i) => i !== index)
    );
  }

  /**
   * Update header key
   */
  protected updateHeaderKey(index: number, event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.customHeaders.update(headers =>
      headers.map((h, i) => i === index ? { ...h, key: value } : h)
    );
  }

  /**
   * Update header value
   */
  protected updateHeaderValue(index: number, event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.customHeaders.update(headers =>
      headers.map((h, i) => i === index ? { ...h, value: value } : h)
    );
  }

  /**
   * Generate HttpHeaders code
   */
  protected httpHeadersCode(): string {
    const headers = this.customHeaders().filter(h => h.key);
    if (headers.length === 0) {
      return 'const headers = new HttpHeaders();';
    }

    const headerLines = headers
      .map(h => `  .set('${h.key}', '${h.value}')`)
      .join('\n');

    return `const headers = new HttpHeaders()
${headerLines};

this.http.get('/api/data', { headers }).subscribe();`;
  }
}
