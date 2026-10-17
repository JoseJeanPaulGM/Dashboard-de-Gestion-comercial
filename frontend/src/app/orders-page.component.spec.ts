import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { DemoStoreService } from './demo-store.service';
import { OrdersPageComponent } from './orders-page.component';

describe('OrdersPageComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrdersPageComponent],
      providers: [{ provide: ActivatedRoute, useValue: { snapshot: { data: { kind: 'sales' } } } }]
    }).compileComponents();
  });

  it('creates a sale draft from a valid reactive form', () => {
    const fixture = TestBed.createComponent(OrdersPageComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.open();
    component.form.controls.partyId.setValue('1');
    component.lines.at(0).patchValue({ productId: '1', quantity: 2 });
    component.save();
    const order = TestBed.inject(DemoStoreService).snapshot.sales[0];
    expect(order.status).toBe('DRAFT');
    expect(order.total).toBe(259.8);
  });

  it('does not create a draft when the party is missing', () => {
    const fixture = TestBed.createComponent(OrdersPageComponent);
    fixture.detectChanges();
    fixture.componentInstance.save();
    expect(TestBed.inject(DemoStoreService).snapshot.sales.length).toBe(0);
  });
});
