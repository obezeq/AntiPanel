import { Component, computed, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AnalysisHeaderSection } from './sections/analysis-header-section/analysis-header-section' ;
import { AnalysisContentSection } from './sections/analysis-content-section/analysis-content-section';

@Component({
  selector: 'app-analysis',
  imports: [Header, Footer, AnalysisHeaderSection, AnalysisContentSection],
  templateUrl: './analysis.html',
  styleUrl: './analysis.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Analysis {
  /*
  protected readonly headerVariant = computed<HeaderVariant>(() =>
    this.authService.isAuthenticated() ? 'loggedIn' : 'home'
  );
  */
}
