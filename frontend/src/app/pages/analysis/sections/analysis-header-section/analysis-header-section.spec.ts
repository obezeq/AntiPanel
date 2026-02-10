import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnalysisHeaderSection } from './analysis-header-section';

describe('AnalysisHeaderSection', () => {
  let component: AnalysisHeaderSection;
  let fixture: ComponentFixture<AnalysisHeaderSection>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnalysisHeaderSection]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AnalysisHeaderSection);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
