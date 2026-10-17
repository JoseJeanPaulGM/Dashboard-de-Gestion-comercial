import { TestBed } from '@angular/core/testing';
import { DemoStoreService } from './demo-store.service';

describe('DemoStoreService', () => {
  let store: DemoStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    store = TestBed.inject(DemoStoreService);
  });

  it('publishes local changes to the record list without retaining example passwords', () => {
    let names: string[] = [];
    const subscription = store.records$('customers').subscribe(rows => names = rows.map(row => String(row['name'])));
    store.saveRecord('customers', { documentType: 'DNI', documentNumber: '12345678', name: 'Cliente de prueba' });
    store.saveRecord('users', { username: 'test.user', password: 'not-persisted', roles: ['VENDEDOR'] });

    expect(names[0]).toBe('Cliente de prueba');
    expect(store.snapshot.records.users[0]['password']).toBeUndefined();
    subscription.unsubscribe();
  });

  it('confirms a purchase once and adds all its quantities to inventory', () => {
    store.createOrder('purchases', 1, [
      { productId: 1, quantity: 3, unitPrice: 50 },
      { productId: 2, quantity: 2, unitPrice: 7.5 }
    ]);
    const order = store.snapshot.purchases[0];
    expect(order.total).toBe(165);

    store.confirmOrder('purchases', order.id);
    expect(store.snapshot.balances.find(item => item.productId === 1)?.quantity).toBe(27);
    expect(store.snapshot.balances.find(item => item.productId === 2)?.quantity).toBe(8);
    expect(() => store.confirmOrder('purchases', order.id)).toThrow();
    expect(store.snapshot.balances.find(item => item.productId === 1)?.quantity).toBe(27);
  });

  it('keeps a sale in draft and every balance unchanged when one line lacks stock', () => {
    store.createOrder('sales', 1, [
      { productId: 1, quantity: 2, unitPrice: 0 },
      { productId: 2, quantity: 7, unitPrice: 0 }
    ]);
    const order = store.snapshot.sales[0];
    expect(order.lines[0].unitPrice).toBe(129.9);

    expect(() => store.confirmOrder('sales', order.id)).toThrowError(/Stock insuficiente/);
    expect(store.snapshot.sales[0].status).toBe('DRAFT');
    expect(store.snapshot.balances.find(item => item.productId === 1)?.quantity).toBe(24);
    expect(store.snapshot.balances.find(item => item.productId === 2)?.quantity).toBe(6);
  });

  it('rejects repeated products and invalid adjustments without changing state', () => {
    expect(() => store.createOrder('purchases', 1, [
      { productId: 1, quantity: 1, unitPrice: 10 },
      { productId: 1, quantity: 2, unitPrice: 10 }
    ])).toThrow();
    expect(store.snapshot.purchases.length).toBe(0);
    expect(() => store.adjust(2, -7, 'Ajuste por daño')).toThrow();
    expect(store.snapshot.balances.find(item => item.productId === 2)?.quantity).toBe(6);
  });

  it('updates a minimum independently of the current quantity', () => {
    store.setMinimum(1, 30);
    expect(store.snapshot.balances.find(item => item.productId === 1)).toEqual({ productId: 1, quantity: 24, minimumQuantity: 30 });
  });
});
