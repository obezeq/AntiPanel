import { Component, ChangeDetectionStrategy, signal } from '@angular/core';
import { AnalysisCard } from '../../../../components/shared/analysis-card/analysis-card';
// import type { UsersAnalyisisResponse } from '../../../../core/services/analysis.service';

@Component({
  selector: 'app-analysis-content-section',
  imports: [AnalysisCard],
  templateUrl: './analysis-content-section.html',
  styleUrl: './analysis-content-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnalysisContentSection {

  /** Stats data */
  // readonly stats = input.required<StatsCardData>();

  /** Datos mock para los analysis cards */
  readonly money = signal("369.33");
  readonly orders = signal("33");
  readonly users = signal("69");

  // readonly analysis = input.required<UsersAnalyisisResponse>();

  /** Money Spent by all the users */
  // readonly totalUsersSpent = input.required<string>();

  /** Total Users made by all the users */
  // readonly totalUsersOrders = input.required<string>();

}