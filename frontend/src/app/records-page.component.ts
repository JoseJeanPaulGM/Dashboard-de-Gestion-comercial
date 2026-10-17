import { AsyncPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, combineLatest, map } from 'rxjs';
import { DemoRecord, DemoStoreService, RecordKind, RecordValue } from './demo-store.service';

interface Field { key: string; label: string; type?: 'text' | 'email' | 'number' | 'select' | 'password'; required?: boolean; options?: { value: string | number; label: string }[] }
interface Column { key: string; label: string }
interface PageDefinition { title: string; singular: string; description: string; fields: Field[]; columns: Column[] }

const pages: Record<RecordKind, PageDefinition> = {
  customers: {
    title: 'Clientes', singular: 'cliente', description: 'Consulta y organiza los clientes de la operación comercial.',
    fields: [
      { key: 'documentType', label: 'Tipo de documento', type: 'select', required: true, options: ['DNI', 'RUC', 'CE', 'PASSPORT', 'OTHER'].map(value => ({ value, label: value })) },
      { key: 'documentNumber', label: 'Número de documento', required: true },
      { key: 'name', label: 'Nombre o razón social', required: true },
      { key: 'email', label: 'Correo electrónico', type: 'email' }
    ],
    columns: [{ key: 'name', label: 'Cliente' }, { key: 'documentNumber', label: 'Documento' }, { key: 'email', label: 'Correo' }]
  },
  categories: {
    title: 'Categorías', singular: 'categoría', description: 'Agrupa el catálogo para encontrar productos con facilidad.',
    fields: [{ key: 'code', label: 'Código', required: true }, { key: 'name', label: 'Nombre', required: true }, { key: 'description', label: 'Descripción' }],
    columns: [{ key: 'code', label: 'Código' }, { key: 'name', label: 'Categoría' }, { key: 'description', label: 'Descripción' }]
  },
  products: {
    title: 'Productos', singular: 'producto', description: 'Precios de venta en soles y referencias del catálogo.',
    fields: [{ key: 'sku', label: 'SKU', required: true }, { key: 'name', label: 'Nombre', required: true }, { key: 'salePrice', label: 'Precio de venta (PEN)', type: 'number', required: true }, { key: 'categoryId', label: 'Categoría', type: 'select', required: true }],
    columns: [{ key: 'sku', label: 'SKU' }, { key: 'name', label: 'Producto' }, { key: 'categoryId', label: 'Categoría' }, { key: 'salePrice', label: 'Precio' }]
  },
  suppliers: {
    title: 'Proveedores', singular: 'proveedor', description: 'Directorio de empresas que abastecen el catálogo.',
    fields: [{ key: 'ruc', label: 'RUC', required: true }, { key: 'businessName', label: 'Razón social', required: true }, { key: 'tradeName', label: 'Nombre comercial' }, { key: 'email', label: 'Correo electrónico', type: 'email' }],
    columns: [{ key: 'businessName', label: 'Proveedor' }, { key: 'ruc', label: 'RUC' }, { key: 'tradeName', label: 'Nombre comercial' }]
  },
  users: {
    title: 'Usuarios', singular: 'usuario', description: 'Vista local de cuentas y roles predefinidos. No inicia sesiones.',
    fields: [{ key: 'username', label: 'Nombre de usuario', required: true }, { key: 'password', label: 'Contraseña de ejemplo (no se guarda)', type: 'password', required: true }, { key: 'roles', label: 'Rol', type: 'select', required: true, options: ['ADMIN', 'VENDEDOR', 'ALMACEN', 'SUPERVISOR', 'AUDITOR'].map(value => ({ value, label: value })) }],
    columns: [{ key: 'username', label: 'Usuario' }, { key: 'roles', label: 'Rol' }]
  }
};

@Component({
  selector: 'app-records-page', standalone: true,
  imports: [AsyncPipe, ReactiveFormsModule],
  template: `
    <section class="page-head">
      <div><div class="eyebrow">MAESTROS / {{ definition.title.toUpperCase() }}</div><h1>{{ definition.title }}</h1><p>{{ definition.description }}</p></div>
      <button class="button primary" type="button" (click)="openCreate()"><span aria-hidden="true">＋</span> Nuevo {{ definition.singular }}</button>
    </section>
    <div class="notice"><span class="notice-icon">i</span><span>Datos de demostración en memoria. Los cambios se reinician al recargar la página.</span></div>
    <section class="panel">
      <div class="panel-toolbar">
        <div><h2>Listado</h2><p>{{ (filtered$ | async)?.length ?? 0 }} registros visibles</p></div>
        <div class="filters"><label class="search-field"><span aria-hidden="true">⌕</span><input type="search" aria-label="Buscar registros" placeholder="Buscar en {{ definition.title.toLowerCase() }}" (input)="setSearch($event)"></label>
          <select aria-label="Filtrar por estado" (change)="setStatus($event)"><option value="all">Todos los estados</option><option value="active">Activos</option><option value="inactive">Inactivos</option></select></div>
      </div>
      <div class="table-scroll"><table><thead><tr><th>ID</th>@for (column of definition.columns; track column.key) { <th>{{ column.label }}</th> }<th>Estado</th><th class="actions-col">Acciones</th></tr></thead>
        <tbody>@for (row of filtered$ | async; track row.id) {
          <tr><td class="muted">#{{ row.id }}</td>@for (column of definition.columns; track column.key) { <td [class.strong-cell]="column.key === 'name' || column.key === 'businessName' || column.key === 'username'">{{ display(row, column.key) }}</td> }
            <td><span class="status" [class.inactive]="!row.active"><span></span>{{ row.active ? 'Activo' : 'Inactivo' }}</span></td>
            <td class="row-actions">@if (kind !== 'users') { <button type="button" (click)="openEdit(row)">Editar</button> }<button type="button" (click)="toggle(row)">{{ row.active ? 'Desactivar' : 'Activar' }}</button></td></tr>
        } @empty { <tr><td class="empty-cell" [attr.colspan]="definition.columns.length + 3">No hay registros para estos filtros.</td></tr> }</tbody></table></div>
    </section>
    @if (message) { <div class="toast" role="status">{{ message }}</div> }
    @if (editing) {
      <div class="drawer-backdrop" (click)="close()"></div>
      <aside class="drawer" role="dialog" aria-modal="true" [attr.aria-label]="editingId ? 'Editar ' + definition.singular : 'Nuevo ' + definition.singular">
        <div class="drawer-head"><div><div class="eyebrow">{{ editingId ? 'EDITAR REGISTRO' : 'NUEVO REGISTRO' }}</div><h2>{{ editingId ? 'Editar' : 'Nuevo' }} {{ definition.singular }}</h2></div><button type="button" class="icon-button" aria-label="Cerrar formulario" (click)="close()">×</button></div>
        <form [formGroup]="form" (ngSubmit)="save()">
          <div class="drawer-body">@for (field of fields; track field.key) {
            <label class="field"><span>{{ field.label }} @if (field.required) { <b>*</b> }</span>
              @if (field.type === 'select') { <select [formControlName]="field.key"><option value="">Seleccionar</option>@for (option of field.options; track option.value) { <option [value]="option.value">{{ option.label }}</option> }</select> }
              @else { <input [type]="field.type || 'text'" [formControlName]="field.key" [attr.min]="field.type === 'number' ? '0.01' : null" [attr.step]="field.type === 'number' ? '0.01' : null"> }
              @if (form.get(field.key)?.invalid && form.get(field.key)?.touched) { <small class="field-error">Revisa este campo.</small> }
            </label>
          }</div>
          <div class="drawer-footer"><button class="button secondary" type="button" (click)="close()">Cancelar</button><button class="button primary" type="submit">Guardar {{ definition.singular }}</button></div>
        </form>
      </aside>
    }
  `
})
export class RecordsPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly store = inject(DemoStoreService);
  readonly kind = this.route.snapshot.data['kind'] as RecordKind;
  readonly definition = pages[this.kind];
  readonly search$ = new BehaviorSubject<string>('');
  readonly status$ = new BehaviorSubject<string>('all');
  readonly filtered$ = combineLatest([this.store.records$(this.kind), this.search$, this.status$]).pipe(
    map(([records, search, status]) => records.filter(row => {
      const matchesText = Object.values(row).some(value => String(value ?? '').toLocaleLowerCase().includes(search));
      return matchesText && (status === 'all' || row.active === (status === 'active'));
    }))
  );
  fields: Field[] = [];
  form!: FormGroup;
  editing = false;
  editingId?: number;
  message = '';

  ngOnInit(): void { this.buildFields(); this.resetForm(); }

  setSearch(event: Event): void { this.search$.next((event.target as HTMLInputElement).value.trim().toLocaleLowerCase()); }
  setStatus(event: Event): void { this.status$.next((event.target as HTMLSelectElement).value); }

  display(row: DemoRecord, key: string): string {
    const value = row[key];
    if (key === 'categoryId') return String(this.store.snapshot.records.categories.find(item => item.id === Number(value))?.['name'] ?? '—');
    if (key === 'salePrice') return `S/ ${Number(value).toFixed(2)}`;
    if (Array.isArray(value)) return value.join(', ');
    return value === '' || value === undefined ? '—' : String(value);
  }

  openCreate(): void { this.editingId = undefined; this.buildFields(); this.resetForm(); this.editing = true; this.message = ''; }
  openEdit(row: DemoRecord): void { this.editingId = row.id; this.buildFields(); this.resetForm(row); this.editing = true; this.message = ''; }
  close(): void { this.editing = false; }

  save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const values: Record<string, RecordValue> = { ...this.form.getRawValue() };
    if (this.kind === 'products') {
      values['categoryId'] = Number(values['categoryId']);
      values['salePrice'] = Number(values['salePrice']);
    }
    if (this.kind === 'users') values['roles'] = [String(values['roles'])];
    try {
      this.store.saveRecord(this.kind, values, this.editingId);
      this.message = 'Registro guardado en la demostración.';
      this.close();
    } catch (error) { this.message = (error as Error).message; }
  }

  toggle(row: DemoRecord): void {
    try { this.store.toggleStatus(this.kind, row.id); this.message = 'Estado actualizado en la demostración.'; }
    catch (error) { this.message = (error as Error).message; }
  }

  private buildFields(): void {
    this.fields = this.definition.fields.map(field => field.key === 'categoryId'
      ? { ...field, options: this.store.snapshot.records.categories.filter(item => item.active).map(item => ({ value: item.id, label: String(item['name']) })) }
      : field);
  }

  private resetForm(row?: DemoRecord): void {
    const controls: Record<string, FormControl> = {};
    for (const field of this.fields) {
      const validators = [ ...(field.required ? [Validators.required] : []), ...(field.type === 'email' ? [Validators.email] : []), ...(field.key === 'salePrice' ? [Validators.min(0.01)] : []) ];
      controls[field.key] = new FormControl(row?.[field.key] ?? '', validators);
    }
    this.form = new FormGroup(controls);
  }
}
