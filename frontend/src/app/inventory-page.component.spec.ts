import { TestBed } from '@angular/core/testing';
import { DemoStoreService } from './demo-store.service';
import { InventoryPageComponent } from './inventory-page.component';

describe('InventoryPageComponent', () => {
  it('shows low stock alerts and saves a new minimum through its form', async () => {
    await TestBed.configureTestingModule({ imports: [InventoryPageComponent] }).compileComponents();
    const fixture = TestBed.createComponent(InventoryPageComponent);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Bajo mínimo');
    fixture.componentInstance.openMinimum({ productId: 1, quantity: 24, minimumQuantity: 8 });
    fixture.componentInstance.minimumForm.controls.minimumQuantity.setValue(30);
    fixture.componentInstance.saveMinimum();
    expect(TestBed.inject(DemoStoreService).snapshot.balances.find(item => item.productId === 1)?.minimumQuantity).toBe(30);
  });
});
