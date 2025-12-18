import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './services/theme.service';
import { LoadingService } from './services/loading.service';
import { ToastContainer } from './components/shared/toast-container/toast-container';
import { Spinner } from './components/shared/spinner/spinner';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastContainer, Spinner],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class App {
  // Initialize ThemeService to apply theme on app start
  private readonly themeService = inject(ThemeService);

  // LoadingService for global spinner
  protected readonly loadingService = inject(LoadingService);
}
