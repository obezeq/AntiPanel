import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnalysisContentSection } from './analysis-content-section';

describe('AnalysisContentSection', () => {
  let component: AnalysisContentSection;
  let fixture: ComponentFixture<AnalysisContentSection>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnalysisContentSection]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AnalysisContentSection);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
