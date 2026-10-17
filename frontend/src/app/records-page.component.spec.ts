import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { DemoStoreService } from './demo-store.service';
import { RecordsPageComponent } from './records-page.component';

describe('RecordsPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecordsPageComponent],
      providers: [{ provide: ActivatedRoute, useValue: { snapshot: { data: { kind: 'customers' } } } }]
    }).compileComponents();
  });

  it('shows customer records and filters them by search text', () => {
    const fixture = TestBed.createComponent(RecordsPageComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.search$.next('andes');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Andes Retail SAC');
    expect(fixture.nativeElement.textContent).not.toContain('Pacífico Norte EIRL');
  });

  it('requires document and name before saving a new customer', () => {
    const fixture = TestBed.createComponent(RecordsPageComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const store = TestBed.inject(DemoStoreService);
    component.openCreate();
    component.save();
    expect(store.snapshot.records.customers.length).toBe(3);
    component.form.patchValue({ documentType: 'DNI', documentNumber: '12345678', name: 'Cliente nuevo' });
    component.save();
    expect(store.snapshot.records.customers[0]['name']).toBe('Cliente nuevo');
  });
});
