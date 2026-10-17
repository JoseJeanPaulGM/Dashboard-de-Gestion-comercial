import { AsyncPipe, DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DemoOrder, DemoStoreService, OrderKind, OrderLine } from './demo-store.service';

@Component({
  selector: 'app-orders-page', standalone: true, imports: [AsyncPipe, DatePipe, ReactiveFormsModule],
  template: `
    <section class="page-head"><div><div class="eyebrow">OPERACIÓN / {{ isPurchase ? 'COMPRAS' : 'VENTAS' }}</div><h1>{{ title }}</h1><p>{{ isPurchase ? 'Registra borradores de compra y simula su entrada al inventario.' : 'Prepara ventas y comprueba el saldo antes de confirmar.' }}</p></div>
      <button class="button primary" type="button" (click)="open()"><span aria-hidden="true">＋</span> Nueva {{ singular }}</button></section>
    <div class="notice"><span class="notice-icon">i</span><span>Flujo local de demostración. El backend y los permisos se integrarán en V12.</span></div>
    <section class="panel"><div class="panel-toolbar"><div><h2>Borradores y confirmadas</h2><p>Importes expresados en PEN</p></div></div>
      <div class="table-scroll"><table><thead><tr><th>ID</th><th>{{ isPurchase ? 'Proveedor' : 'Cliente' }}</th><th>Fecha</th><th>Líneas</th><th>Total</th><th>Estado</th><th class="actions-col">Acciones</th></tr></thead>
        <tbody>@for (order of orders$ | async; track order.id) { <tr><td class="muted">#{{ order.id }}</td><td class="strong-cell">{{ partyName(order.partyId) }}</td><td>{{ order.createdAt | date:'dd/MM/yyyy HH:mm' }}</td><td>{{ order.lines.length }}</td><td>S/ {{ order.total.toFixed(2) }}</td>
          <td><span class="status" [class.pending]="order.status === 'DRAFT'"><span></span>{{ order.status === 'DRAFT' ? 'Borrador' : 'Confirmada' }}</span></td>
          <td class="row-actions">@if (order.status === 'DRAFT') { <button type="button" (click)="confirm(order)">Confirmar</button> } @else { <span class="muted">—</span> }</td></tr> }
          @empty { <tr><td class="empty-cell" colspan="7"><span class="empty-icon">◇</span><strong>Aún no hay {{ title.toLowerCase() }}</strong><small>Crea un borrador para probar el flujo.</small></td></tr> }</tbody></table></div>
    </section>
    @if (message) { <div class="toast" role="status">{{ message }}</div> }
    @if (editing) { <div class="drawer-backdrop" (click)="close()"></div><aside class="drawer wide" role="dialog" aria-modal="true" [attr.aria-label]="'Nueva ' + singular">
      <div class="drawer-head"><div><div class="eyebrow">NUEVO BORRADOR</div><h2>Nueva {{ singular }}</h2></div><button type="button" class="icon-button" aria-label="Cerrar formulario" (click)="close()">×</button></div>
      <form [formGroup]="form" (ngSubmit)="save()"><div class="drawer-body">
        <label class="field"><span>{{ isPurchase ? 'Proveedor' : 'Cliente' }} *</span><select formControlName="partyId"><option value="">Seleccionar</option>@for (party of parties; track party.id) { <option [value]="party.id">{{ partyName(party.id) }}</option> }</select>@if (form.controls.partyId.invalid && form.controls.partyId.touched) { <small class="field-error">Selecciona una opción.</small> }</label>
        <div class="subheading"><strong>Productos</strong><button type="button" class="text-button" (click)="addLine()">＋ Agregar línea</button></div>
        <div formArrayName="lines">@for (line of lines.controls; track line; let i = $index) { <div class="order-line" [formGroupName]="i">
          <label class="field"><span>Producto *</span><select formControlName="productId"><option value="">Seleccionar</option>@for (product of products; track product.id) { <option [value]="product.id">{{ product['name'] }}</option> }</select></label>
          <label class="field narrow-field"><span>Cantidad *</span><input type="number" min="1" max="1000000" step="1" formControlName="quantity"></label>
          @if (isPurchase) { <label class="field narrow-field"><span>Precio (PEN) *</span><input type="number" min="0.01" step="0.01" formControlName="unitPrice"></label> }
          <button type="button" class="line-remove" [attr.aria-label]="'Quitar línea ' + (i + 1)" (click)="removeLine(i)" [disabled]="lines.length === 1">×</button>
        </div> }</div>
        @if (form.invalid && form.touched) { <p class="field-error">Completa los datos obligatorios y revisa cantidades y precios.</p> }
      </div><div class="drawer-footer"><button class="button secondary" type="button" (click)="close()">Cancelar</button><button class="button primary" type="submit">Crear borrador</button></div></form>
    </aside> }
  `
})
export class OrdersPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly store = inject(DemoStoreService);
  readonly kind = this.route.snapshot.data['kind'] as OrderKind;
  readonly isPurchase = this.kind === 'purchases';
  readonly title = this.isPurchase ? 'Compras' : 'Ventas';
  readonly singular = this.isPurchase ? 'compra' : 'venta';
  readonly orders$ = this.store.orders$(this.kind);
  form = this.createForm();
  editing = false;
  message = '';

  get parties() { return this.store.snapshot.records[this.isPurchase ? 'suppliers' : 'customers'].filter(item => item.active); }
  get products() { return this.store.snapshot.records.products.filter(item => item.active); }
  get lines(): FormArray { return this.form.controls.lines; }

  partyName(id: number): string {
    const party = this.store.snapshot.records[this.isPurchase ? 'suppliers' : 'customers'].find(item => item.id === id);
    return String(party?.[this.isPurchase ? 'businessName' : 'name'] ?? '—');
  }

  open(): void { this.form = this.createForm(); this.editing = true; this.message = ''; }
  close(): void { this.editing = false; }
  addLine(): void { this.lines.push(this.createLine()); }
  removeLine(index: number): void { if (this.lines.length > 1) this.lines.removeAt(index); }

  save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    const lines: OrderLine[] = raw.lines.map(line => ({ productId: Number(line.productId), quantity: Number(line.quantity), unitPrice: Number(line.unitPrice) }));
    try { this.store.createOrder(this.kind, Number(raw.partyId), lines); this.message = 'Borrador creado. Confírmalo desde el listado.'; this.close(); }
    catch (error) { this.message = (error as Error).message; }
  }

  confirm(order: DemoOrder): void {
    try { this.store.confirmOrder(this.kind, order.id); this.message = `${this.singular === 'compra' ? 'Compra' : 'Venta'} confirmada en la demostración.`; }
    catch (error) { this.message = (error as Error).message; }
  }

  private createForm() {
    return new FormGroup({ partyId: new FormControl('', { nonNullable: true, validators: Validators.required }), lines: new FormArray([this.createLine()]) });
  }

  private createLine() {
    return new FormGroup({
      productId: new FormControl('', { nonNullable: true, validators: Validators.required }),
      quantity: new FormControl(1, { nonNullable: true, validators: [Validators.required, Validators.min(1), Validators.max(1_000_000), Validators.pattern(/^\d+$/)] }),
      unitPrice: new FormControl(this.isPurchase ? 0 : 1, { nonNullable: true, validators: this.isPurchase ? [Validators.required, Validators.min(0.01)] : [] })
    });
  }
}
