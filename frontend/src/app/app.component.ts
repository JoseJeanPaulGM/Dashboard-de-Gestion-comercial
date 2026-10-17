import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <a class="brand" routerLink="/customers" aria-label="Gestión Comercial, ir a clientes">
          <span class="brand-mark">GC</span>
          <span><strong>Gestión Comercial</strong><small>Espacio de trabajo</small></span>
        </a>
        <div class="sidebar-label">OPERACIÓN</div>
        <nav aria-label="Navegación principal">
          @for (item of navigation; track item.path) {
            <a class="nav-link" [routerLink]="item.path" routerLinkActive="active">
              <span class="nav-icon" aria-hidden="true">{{ item.icon }}</span>{{ item.label }}
            </a>
          }
        </nav>
        <div class="sidebar-spacer"></div>
        <div class="sidebar-foot"><span class="demo-dot"></span> Modo demostración <small>Datos locales · V11</small></div>
      </aside>
      <div class="main-area">
        <header class="topbar">
          <div class="topbar-context">GESTIÓN COMERCIAL <span>/</span> INTERFAZ V11</div>
          <div class="topbar-user"><span class="avatar">DE</span><span>Demo local<small>Sin sesión real</small></span></div>
        </header>
        <main id="main-content"><router-outlet /></main>
      </div>
    </div>
  `
})
export class AppComponent {
  readonly navigation = [
    { path: '/customers', label: 'Clientes', icon: 'C' },
    { path: '/categories', label: 'Categorías', icon: 'Ca' },
    { path: '/products', label: 'Productos', icon: 'P' },
    { path: '/suppliers', label: 'Proveedores', icon: 'Pr' },
    { path: '/purchases', label: 'Compras', icon: '↗' },
    { path: '/sales', label: 'Ventas', icon: '↙' },
    { path: '/inventory', label: 'Inventario', icon: 'I' },
    { path: '/users', label: 'Usuarios', icon: 'U' }
  ];
}
