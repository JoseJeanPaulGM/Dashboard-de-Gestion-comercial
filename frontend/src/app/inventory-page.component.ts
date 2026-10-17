import { AsyncPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BehaviorSubject, combineLatest, map } from 'rxjs';
import { Balance, DemoStoreService } from './demo-store.service';

@Component({
  selector: 'app-inventory-page', standalone: true, imports: [AsyncPipe, ReactiveFormsModule],
  template: `
    <section class="page-head"><div><div class="eyebrow">OPERACIÓN / INVENTARIO</div><h1>Inventario</h1><p>Consulta saldos, detecta mínimos y ensaya ajustes locales.</p></div><button type="button" class="button primary" (click)="openAdjust()"><span aria-hidden="true">＋</span> Nuevo ajuste</button></section>
    <div class="notice"><span class="notice-icon">i</span><span>Los saldos de esta vista son ficticios. Las confirmaciones de compras y ventas también los actualizan.</span></div>
    <section class="metric-row"><div class="metric-card"><span>PRODUCTOS CON SALDO</span><strong>{{ store.snapshot.balances.length }}</strong><small>En esta demostración</small></div><div class="metric-card alert-card"><span>ALERTAS DE MÍNIMO</span><strong>{{ alertCount }}</strong><small>Saldo igual o menor al mínimo</small></div></section>
    <section class="panel"><div class="panel-toolbar"><div><h2>Saldos por producto</h2><p>Unidades disponibles y umbral de alerta</p></div><label class="check-filter"><input type="checkbox" (change)="setAlertsOnly($event)"> Solo alertas</label></div>
      <div class="table-scroll"><table><thead><tr><th>Producto</th><th>SKU</th><th>Disponible</th><th>Mínimo</th><th>Estado</th><th class="actions-col">Acciones</th></tr></thead><tbody>
        @for (balance of balances$ | async; track balance.productId) { <tr><td class="strong-cell">{{ productName(balance.productId) }}</td><td>{{ productSku(balance.productId) }}</td><td>{{ balance.quantity }}</td><td>{{ balance.minimumQuantity }}</td><td><span class="status" [class.low]="isLow(balance)"><span></span>{{ isLow(balance) ? 'Bajo mínimo' : 'Normal' }}</span></td><td class="row-actions"><button type="button" (click)="openMinimum(balance)">Configurar mínimo</button></td></tr> }
        @empty { <tr><td class="empty-cell" colspan="6">No hay productos con alerta en este momento.</td></tr> }
      </tbody></table></div></section>
    @if (message) { <div class="toast" role="status">{{ message }}</div> }
    @if (modal) { <div class="drawer-backdrop" (click)="close()"></div><aside class="drawer" role="dialog" aria-modal="true" [attr.aria-label]="modal === 'adjust' ? 'Nuevo ajuste' : 'Configurar mínimo'">
      <div class="drawer-head"><div><div class="eyebrow">INVENTARIO LOCAL</div><h2>{{ modal === 'adjust' ? 'Nuevo ajuste' : 'Configurar mínimo' }}</h2></div><button type="button" class="icon-button" aria-label="Cerrar formulario" (click)="close()">×</button></div>
      @if (modal === 'adjust') { <form [formGroup]="adjustForm" (ngSubmit)="saveAdjustment()"><div class="drawer-body">
        <label class="field"><span>Producto *</span><select formControlName="productId"><option value="">Seleccionar</option>@for (product of products; track product.id) { <option [value]="product.id">{{ product['name'] }}</option> }</select></label>
        <label class="field"><span>Variación de unidades *</span><input type="number" step="1" formControlName="quantityChange"><small>Usa un número negativo para descontar.</small></label>
        <label class="field"><span>Motivo *</span><textarea rows="4" formControlName="reason" placeholder="Describe el motivo del ajuste"></textarea></label>
        @if (adjustForm.invalid && adjustForm.touched) { <p class="field-error">Completa producto, variación y motivo (mínimo 10 caracteres).</p> }
      </div><div class="drawer-footer"><button class="button secondary" type="button" (click)="close()">Cancelar</button><button class="button primary" type="submit">Registrar ajuste</button></div></form> }
      @else { <form [formGroup]="minimumForm" (ngSubmit)="saveMinimum()"><div class="drawer-body"><p class="form-context">{{ productName(selectedProductId) }}</p><label class="field"><span>Mínimo de unidades *</span><input type="number" min="0" step="1" formControlName="minimumQuantity"></label>@if (minimumForm.invalid && minimumForm.touched) { <p class="field-error">Ingresa un entero no negativo.</p> }</div><div class="drawer-footer"><button class="button secondary" type="button" (click)="close()">Cancelar</button><button class="button primary" type="submit">Guardar mínimo</button></div></form> }
    </aside> }
  `
})
export class InventoryPageComponent {
  readonly store = inject(DemoStoreService);
  readonly alertsOnly$ = new BehaviorSubject(false);
  readonly balances$ = combineLatest([this.store.state$, this.alertsOnly$]).pipe(map(([state, only]) => state.balances.filter(balance => !only || this.isLow(balance))));
  adjustForm = this.createAdjustForm();
  minimumForm = this.createMinimumForm();
  modal: 'adjust' | 'minimum' | null = null;
  selectedProductId = 0;
  message = '';

  get products() { return this.store.snapshot.records.products.filter(item => item.active); }
  get alertCount() { return this.store.snapshot.balances.filter(balance => this.isLow(balance)).length; }
  isLow(balance: Balance): boolean { return balance.minimumQuantity > 0 && balance.quantity <= balance.minimumQuantity; }
  productName(id: number): string { return String(this.store.snapshot.records.products.find(item => item.id === id)?.['name'] ?? '—'); }
  productSku(id: number): string { return String(this.store.snapshot.records.products.find(item => item.id === id)?.['sku'] ?? '—'); }
  setAlertsOnly(event: Event): void { this.alertsOnly$.next((event.target as HTMLInputElement).checked); }

  openAdjust(): void { this.adjustForm = this.createAdjustForm(); this.modal = 'adjust'; this.message = ''; }
  openMinimum(balance: Balance): void { this.selectedProductId = balance.productId; this.minimumForm = this.createMinimumForm(balance.minimumQuantity); this.modal = 'minimum'; this.message = ''; }
  close(): void { this.modal = null; }

  saveAdjustment(): void {
    this.adjustForm.markAllAsTouched();
    if (this.adjustForm.invalid) return;
    const value = this.adjustForm.getRawValue();
    try { this.store.adjust(Number(value.productId), Number(value.quantityChange), value.reason); this.message = 'Saldo ajustado en la demostración.'; this.close(); }
    catch (error) { this.message = (error as Error).message; }
  }

  saveMinimum(): void {
    this.minimumForm.markAllAsTouched();
    if (this.minimumForm.invalid) return;
    try { this.store.setMinimum(this.selectedProductId, Number(this.minimumForm.getRawValue().minimumQuantity)); this.message = 'Mínimo actualizado en la demostración.'; this.close(); }
    catch (error) { this.message = (error as Error).message; }
  }

  private createAdjustForm() {
    return new FormGroup({
      productId: new FormControl('', { nonNullable: true, validators: Validators.required }),
      quantityChange: new FormControl(0, { nonNullable: true, validators: [Validators.required, Validators.pattern(/^-?\d+$/)] }),
      reason: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(10)] })
    });
  }

  private createMinimumForm(value = 0) {
    return new FormGroup({ minimumQuantity: new FormControl(value, { nonNullable: true, validators: [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)] }) });
  }
}
