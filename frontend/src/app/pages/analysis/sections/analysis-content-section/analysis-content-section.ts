import { Component, ChangeDetectionStrategy, computed, input, output } from '@angular/core';
// import type { UsersAnalyisisResponse } from '../../../../core/services/analysis.service';

@Component({
  selector: 'app-analysis-content-section',
  imports: [],
  templateUrl: './analysis-content-section.html',
  styleUrl: './analysis-content-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnalysisContentSection {


  // readonly analysis = input.required<UsersAnalyisisResponse>();

  /** Money Spent by all the users */
  // readonly totalUsersSpent = input.required<string>();

  /** Total Users made by all the users */
  // readonly totalUsersOrders = input.required<string>();

}