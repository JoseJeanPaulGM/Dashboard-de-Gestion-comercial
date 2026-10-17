import { Injectable } from '@angular/core';
import { BehaviorSubject, map } from 'rxjs';

export type RecordKind = 'customers' | 'categories' | 'products' | 'suppliers' | 'users';
export type OrderKind = 'purchases' | 'sales';
export type RecordValue = string | number | boolean | string[] | undefined;
export interface DemoRecord { id: number; active: boolean; [key: string]: RecordValue }
export interface OrderLine { productId: number; quantity: number; unitPrice: number }
export interface DemoOrder {
  id: number;
  partyId: number;
  status: 'DRAFT' | 'CONFIRMED';
  lines: OrderLine[];
  total: number;
  createdAt: string;
}
export interface Balance { productId: number; quantity: number; minimumQuantity: number }
export interface DemoState {
  records: Record<RecordKind, DemoRecord[]>;
  purchases: DemoOrder[];
  sales: DemoOrder[];
  balances: Balance[];
}

const seed: DemoState = {
  records: {
    customers: [
      { id: 1, active: true, documentType: 'RUC', documentNumber: '20123456789', name: 'Andes Retail SAC', email: 'contacto@andes.example' },
      { id: 2, active: true, documentType: 'DNI', documentNumber: '45678912', name: 'María Quispe', email: 'maria@example.test' },
      { id: 3, active: false, documentType: 'RUC', documentNumber: '20654321987', name: 'Pacífico Norte EIRL', email: '' }
    ],
    categories: [
      { id: 1, active: true, code: 'OFI', name: 'Oficina', description: 'Artículos de escritorio' },
      { id: 2, active: true, code: 'TEC', name: 'Tecnología', description: 'Accesorios y equipos' },
      { id: 3, active: true, code: 'EMP', name: 'Empaque', description: 'Material de embalaje' }
    ],
    products: [
      { id: 1, active: true, sku: 'TEC-001', name: 'Teclado inalámbrico', salePrice: 129.9, categoryId: 2 },
      { id: 2, active: true, sku: 'OFI-014', name: 'Cuaderno A4', salePrice: 18.5, categoryId: 1 },
      { id: 3, active: true, sku: 'EMP-008', name: 'Caja de cartón', salePrice: 7.8, categoryId: 3 },
      { id: 4, active: false, sku: 'TEC-005', name: 'Mouse óptico', salePrice: 39.9, categoryId: 2 }
    ],
    suppliers: [
      { id: 1, active: true, ruc: '20456789123', businessName: 'Distribuidora Lima SAC', tradeName: 'DistriLima', email: 'ventas@distribuidora.example' },
      { id: 2, active: true, ruc: '20567891234', businessName: 'Suministros del Sur SAC', tradeName: 'SumiSur', email: 'pedidos@suministros.example' }
    ],
    users: [
      { id: 1, active: true, username: 'admin.demo', roles: ['ADMIN'] },
      { id: 2, active: true, username: 'ventas.demo', roles: ['VENDEDOR'] },
      { id: 3, active: true, username: 'almacen.demo', roles: ['ALMACEN'] }
    ]
  },
  purchases: [],
  sales: [],
  balances: [
    { productId: 1, quantity: 24, minimumQuantity: 8 },
    { productId: 2, quantity: 6, minimumQuantity: 10 },
    { productId: 3, quantity: 42, minimumQuantity: 12 },
    { productId: 4, quantity: 0, minimumQuantity: 0 }
  ]
};

@Injectable({ providedIn: 'root' })
export class DemoStoreService {
  private readonly stateSubject = new BehaviorSubject<DemoState>(structuredClone(seed));
  readonly state$ = this.stateSubject.asObservable();

  get snapshot(): DemoState { return this.stateSubject.value; }

  records$(kind: RecordKind) {
    return this.state$.pipe(map(state => state.records[kind]));
  }

  orders$(kind: OrderKind) {
    return this.state$.pipe(map(state => state[kind]));
  }

  saveRecord(kind: RecordKind, values: Record<string, RecordValue>, id?: number): void {
    const next = structuredClone(this.snapshot);
    const list = next.records[kind];
    if (kind === 'users') delete values['password']; // La demostración no conserva credenciales.
    if (id !== undefined) {
      const index = list.findIndex(item => item.id === id);
      if (index < 0) throw new Error('El registro ya no está disponible.');
      list[index] = { ...list[index], ...values };
    } else {
      list.unshift({ id: Math.max(0, ...list.map(item => item.id)) + 1, active: true, ...values });
    }
    this.stateSubject.next(next);
  }

  toggleStatus(kind: RecordKind, id: number): void {
    const next = structuredClone(this.snapshot);
    const item = next.records[kind].find(record => record.id === id);
    if (!item) throw new Error('El registro ya no está disponible.');
    item.active = !item.active;
    this.stateSubject.next(next);
  }

  createOrder(kind: OrderKind, partyId: number, lines: OrderLine[]): void {
    const next = structuredClone(this.snapshot);
    const partyKind = kind === 'purchases' ? 'suppliers' : 'customers';
    if (!next.records[partyKind].some(party => party.id === partyId && party.active)) {
      throw new Error('Selecciona un cliente o proveedor activo.');
    }
    if (!lines.length || new Set(lines.map(line => line.productId)).size !== lines.length) {
      throw new Error('Agrega líneas con productos diferentes.');
    }
    const calculated = lines.map(line => {
      const product = next.records.products.find(item => item.id === line.productId && item.active);
      if (!product || !Number.isInteger(line.quantity) || line.quantity < 1 || line.quantity > 1_000_000) {
        throw new Error('Revisa los productos y cantidades de las líneas.');
      }
      const unitPrice = kind === 'sales' ? Number(product['salePrice']) : line.unitPrice;
      if (!Number.isFinite(unitPrice) || unitPrice <= 0) throw new Error('El precio debe ser positivo.');
      return { productId: line.productId, quantity: line.quantity, unitPrice };
    });
    const list = next[kind];
    list.unshift({
      id: Math.max(0, ...list.map(item => item.id)) + 1,
      partyId,
      status: 'DRAFT',
      lines: calculated,
      total: Math.round(calculated.reduce((sum, line) => sum + line.quantity * line.unitPrice, 0) * 100) / 100,
      createdAt: new Date().toISOString()
    });
    this.stateSubject.next(next);
  }

  confirmOrder(kind: OrderKind, id: number): void {
    const next = structuredClone(this.snapshot);
    const order = next[kind].find(item => item.id === id);
    if (!order || order.status !== 'DRAFT') throw new Error('El borrador ya no está disponible.');
    for (const line of order.lines) {
      const balance = next.balances.find(item => item.productId === line.productId);
      if (!balance) throw new Error('No hay saldo para uno de los productos.');
      if (kind === 'sales' && balance.quantity < line.quantity) {
        throw new Error('Stock insuficiente. La venta sigue en borrador.');
      }
    }
    for (const line of order.lines) {
      const balance = next.balances.find(item => item.productId === line.productId)!;
      balance.quantity += kind === 'purchases' ? line.quantity : -line.quantity;
    }
    order.status = 'CONFIRMED';
    this.stateSubject.next(next);
  }

  setMinimum(productId: number, minimumQuantity: number): void {
    if (!Number.isInteger(minimumQuantity) || minimumQuantity < 0) throw new Error('El mínimo debe ser un entero no negativo.');
    const next = structuredClone(this.snapshot);
    const balance = next.balances.find(item => item.productId === productId);
    if (!balance) throw new Error('Producto no encontrado.');
    balance.minimumQuantity = minimumQuantity;
    this.stateSubject.next(next);
  }

  adjust(productId: number, quantityChange: number, reason: string): void {
    if (!Number.isInteger(quantityChange) || quantityChange === 0 || Math.abs(quantityChange) > 1_000_000) {
      throw new Error('La variación debe ser un entero distinto de cero.');
    }
    if (reason.trim().length < 10) throw new Error('Describe el motivo con al menos 10 caracteres.');
    const next = structuredClone(this.snapshot);
    const balance = next.balances.find(item => item.productId === productId);
    if (!balance || balance.quantity + quantityChange < 0) throw new Error('El ajuste dejaría un saldo negativo.');
    balance.quantity += quantityChange;
    this.stateSubject.next(next);
  }
}
