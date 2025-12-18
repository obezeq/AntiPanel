import { ChangeDetectionStrategy, Component } from '@angular/core';
import { DomEventsSection } from './sections/dom-events-section/dom-events-section';
import { FormsSection } from './sections/forms-section/forms-section';
import { ServicesSection } from './sections/services-section/services-section';

@Component({
  selector: 'app-cliente',
  standalone: true,
  imports: [DomEventsSection, FormsSection, ServicesSection],
  templateUrl: './cliente.html',
  styleUrl: './cliente.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Cliente {}
