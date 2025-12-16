import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type MainContentVariant = 'default' | 'narrow' | 'wide' | 'fluid';

@Component({
  selector: 'app-main-content',
  templateUrl: './main-content.html',
  styleUrl: './main-content.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainContent {
  /** Container width variant */
  readonly variant = input<MainContentVariant>('default');

  /** Whether to add vertical padding */
  readonly padded = input<boolean>(true);
}
