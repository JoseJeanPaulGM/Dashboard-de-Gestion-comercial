import { Routes } from '@angular/router';
import { InventoryPageComponent } from './inventory-page.component';
import { OrdersPageComponent } from './orders-page.component';
import { RecordsPageComponent } from './records-page.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'customers' },
  { path: 'customers', component: RecordsPageComponent, data: { kind: 'customers' } },
  { path: 'categories', component: RecordsPageComponent, data: { kind: 'categories' } },
  { path: 'products', component: RecordsPageComponent, data: { kind: 'products' } },
  { path: 'suppliers', component: RecordsPageComponent, data: { kind: 'suppliers' } },
  { path: 'users', component: RecordsPageComponent, data: { kind: 'users' } },
  { path: 'purchases', component: OrdersPageComponent, data: { kind: 'purchases' } },
  { path: 'sales', component: OrdersPageComponent, data: { kind: 'sales' } },
  { path: 'inventory', component: InventoryPageComponent },
  { path: '**', redirectTo: 'customers' }
];
