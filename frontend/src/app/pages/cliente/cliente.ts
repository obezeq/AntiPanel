import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormsSection } from './sections/forms-section/forms-section';

@Component({
  selector: 'app-cliente',
  standalone: true,
  imports: [FormsSection],
  templateUrl: './cliente.html',
  styleUrl: './cliente.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Cliente {}
