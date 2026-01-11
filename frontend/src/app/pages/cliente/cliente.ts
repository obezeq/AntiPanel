import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Breadcrumb } from '../../components/shared/breadcrumb/breadcrumb';
import { DomEventsSection } from './sections/dom-events-section/dom-events-section';
import { FormsSection } from './sections/forms-section/forms-section';
import { ModalSection } from './sections/modal-section/modal-section';
import { ServicesSection } from './sections/services-section/services-section';

@Component({
  selector: 'app-cliente',
  standalone: true,
  imports: [
    Breadcrumb,
    DomEventsSection,
    FormsSection,
    ModalSection,
    ServicesSection
  ],
  templateUrl: './cliente.html',
  styleUrl: './cliente.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Cliente {}
