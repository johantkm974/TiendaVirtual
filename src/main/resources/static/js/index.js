/**
 * index.js - unificado (listado, carrito, pagos, UI, categorias, contacto)
 * CORREGIDO - Mantiene compatibilidad con tu backend y todas las funcionalidades de pago
 */

const API_URL = "https://tiendavirtual-production-88d4.up.railway.app/api/productos";
const API_CATEGORIAS = "https://tiendavirtual-production-88d4.up.railway.app/api/categorias";
const contenedorPrincipal = document.getElementById("mainProductos");

// Estado
let productosGlobales = [];
let carrito = JSON.parse(localStorage.getItem('carrito') || '[]');

(async function init() {
  // Eventos UI básicos
  setupMenuToggle();
  setupHeaderButtons();
  setupDropdown(); // 🔥 NUEVA FUNCIÓN
  bindContactoForm();

  // Cargar datos
  await Promise.all([cargarCategorias(), listarProductos()]);

  // Post-login actions (si regresaste para agregar)
  procesarPostLoginAdd();

  // Mostrar usuario y carrito inicial
  mostrarUsuario();
  actualizarCarritoUI();
})();

/* ==================== DROPDOWN CON CLICK ==================== */
function setupDropdown() {
    const dropdownContainer = document.getElementById('dropdownContainer');
    const productosDrop = document.getElementById('productosDrop');
    const dropdownCategorias = document.getElementById('dropdownCategorias');
    
    if (!dropdownContainer || !productosDrop || !dropdownCategorias) return;
    
    // Abrir/cerrar dropdown al hacer click en "Productos"
    productosDrop.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        dropdownCategorias.classList.toggle('active');
    });
    
    // Cerrar dropdown al hacer click en una categoría
    dropdownCategorias.addEventListener('click', function(e) {
        if (e.target.tagName === 'A') {
            dropdownCategorias.classList.remove('active');
        }
    });
    
    // Cerrar dropdown al hacer click fuera
    document.addEventListener('click', function(e) {
        if (!dropdownContainer.contains(e.target)) {
            dropdownCategorias.classList.remove('active');
        }
    });
    
    // Prevenir que se cierre al hacer click dentro del dropdown
    dropdownCategorias.addEventListener('click', function(e) {
        e.stopPropagation();
    });
}

/* ==================== MENU MÓVIL ==================== */
function setupMenuToggle() {
  const menuToggle = document.getElementById('menuToggle');
  const navMenu = document.getElementById('navMenu');
  if (!menuToggle || !navMenu) return;
  
  menuToggle.addEventListener('click', (e) => {
    e.stopPropagation();
    navMenu.classList.toggle('show');
  });

  // Cerrar menú al hacer click en un enlace
  navMenu.addEventListener('click', (e) => {
    if (e.target.tagName === 'A') {
      navMenu.classList.remove('show');
    }
  });

  // Cerrar menú al hacer click fuera
  document.addEventListener('click', (e) => {
    if (!navMenu.contains(e.target) && !menuToggle.contains(e.target)) {
      navMenu.classList.remove('show');
    }
  });
}

/* ==================== USUARIO & HEADER ==================== */
function setupHeaderButtons() {
  const btnLogin = document.getElementById('btn-login');
  if (btnLogin) btnLogin.addEventListener('click', () => window.location.href = './html/login.html');

  // Admin button is created later on DOMContentLoaded in addAdminButtonIfNeeded
  addAdminButtonIfNeeded();
}

function mostrarUsuario() {
  const userInfo = document.getElementById("user-info");
  const userNombreSpan = document.getElementById("user-nombre");
  const btnLogin = document.getElementById("btn-login");
  const carritoIcon = document.getElementById("carritoIcon");

  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');

  if (usuario && usuario.nombre) {
    userNombreSpan.textContent = usuario.nombre;
    if (userInfo) userInfo.style.display = 'flex';
    if (btnLogin) btnLogin.style.display = 'none';
    if (carritoIcon) carritoIcon.style.display = (usuario?.rol?.nombre === 'Administrador') ? 'none' : 'flex';
    addAdminButtonIfNeeded(); // en caso se inicie sesión en la misma página
  } else {
    if (userInfo) userInfo.style.display = 'none';
    if (btnLogin) btnLogin.style.display = 'inline-block';
    if (carritoIcon) carritoIcon.style.display = 'none';
  }
}

function logout() {
  localStorage.removeItem('usuario');
  // no eliminamos carrito por defecto
  mostrarUsuario();
  actualizarCarritoUI();
  window.location.href = './html/login.html';
}
window.logout = logout;

/* crea botón admin si el usuario es admin */
function addAdminButtonIfNeeded() {
  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  const headerActions = document.querySelector('.header-actions');
  if (!headerActions) return;

  // Evitar duplicados
  if (headerActions.querySelector('.btn-admin')) return;

  if (usuario && usuario.rol && usuario.rol.nombre === 'Administrador') {
    const btnAdmin = document.createElement('button');
    btnAdmin.textContent = '🛠 Panel Administrador';
    btnAdmin.className = 'btn-admin';
    btnAdmin.onclick = () => window.location.href = './html/productos.html';
    headerActions.insertBefore(btnAdmin, headerActions.firstChild);
  }
}

/* ==================== CATEGORIAS ==================== */
async function cargarCategorias() {
  let categorias = [];
  try {
    const res = await fetch(API_CATEGORIAS);
    if (res.ok) {
      categorias = await res.json();
      categorias = Array.isArray(categorias) ? categorias : categorias.map?.(c=>c.nombre) || [];
      categorias = categorias.map(c => typeof c === 'string' ? c : c.nombre);
    }
  } catch (err) {
    console.warn('No se pudo obtener categorias desde API, usaremos productos como fuente.');
  }

  populateCategoriasDropdown(categorias);
}

/* llena dropdown (y escucha clicks) */
function populateCategoriasDropdown(cats) {
  const dd = document.getElementById('dropdownCategorias');
  if (!dd) return;
  dd.innerHTML = `<li><a href="#" data-cat="Todas">Todas</a></li>`;

  // si nos pasaron un array de strings
  cats = cats || [];
  // si está vacío, lo llenaremos con base en productos cuando se carguen
  cats.forEach(c => {
    const li = document.createElement('li');
    const a = document.createElement('a');
    a.href = '#';
    a.dataset.cat = c;
    a.textContent = c;
    li.appendChild(a);
    dd.appendChild(li);
  });

  // Delegación para los links
  dd.addEventListener('click', (e) => {
    const a = e.target.closest('a');
    if (!a) return;
    e.preventDefault();
    const cat = a.dataset.cat || 'Todas';
    if (cat === 'Todas') {
      listarProductos(); // carga todo
    } else {
      filtrarPorCategoria(cat);
    }
  });
}

/* ==================== LISTADO PRODUCTOS - CORREGIDO ==================== */
async function listarProductos() {
  try {
    const res = await fetch(API_URL);
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const data = await res.json();
    
    // 🔥 CORRECCIÓN: Procesar productos para estructura consistente
    productosGlobales = Array.isArray(data) ? data.map(procesarProducto) : [];

    // Si dropdown no tenía categorías, crear desde productos
    const dd = document.getElementById('dropdownCategorias');
    if (dd && (!dd.querySelector('a[data-cat]') || dd.querySelectorAll('li').length <= 1)) {
      const categorias = [...new Set(productosGlobales.map(p => p.categoria || 'Otros'))].filter(Boolean);
      categorias.forEach(c => {
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = '#';
        a.dataset.cat = c;
        a.textContent = c;
        li.appendChild(a);
        dd.appendChild(li);
      });
    }

    // guardar cache para post-login
    localStorage.setItem('productosCache', JSON.stringify(productosGlobales));

    // Mostrar grid por categorías
    listarProductosPorCategoria();

  } catch (err) {
    console.error('Error listar productos', err);
    contenedorPrincipal.innerHTML = `<p style="text-align:center;padding:30px;color:#666">No se pudieron cargar los productos. Intenta más tarde.</p>`;
  }
}

// 🔥 NUEVA FUNCIÓN: Procesar estructura del producto
// 🔥 NUEVA VERSIÓN: Procesar producto con imagen desde la base de datos
function procesarProducto(producto) {
  // Determinar categoría
  let categoriaNombre = 'Otros';
  if (producto.categoriaNombre) {
    categoriaNombre = producto.categoriaNombre;
  } else if (producto.categoria && producto.categoria.nombre) {
    categoriaNombre = producto.categoria.nombre;
  }

  // 🔥 Nueva forma de obtener imagen desde el backend
  // si el producto tiene imagen guardada (no null), generamos la URL del endpoint
  let imagenUrl;
  if (producto.imagen) {
    // Endpoint que devuelve directamente la imagen (Base64 o binaria)
    imagenUrl = `https://tiendavirtual-production-88d4.up.railway.app/api/productos/${producto.id}/imagen`;
  } else {
    imagenUrl = '/img/no-image.png';
  }

  return {
    id: producto.id,
    nombre: producto.nombre || 'Sin nombre',
    descripcion: producto.descripcion || '',
    precio: parseFloat(producto.precio) || 0,
    stock: parseInt(producto.stock) || 0,
    imagen_url: imagenUrl,
    categoria: categoriaNombre,
    _original: producto
  };
}


/* Agrupa y muestra productos por categoría */
function listarProductosPorCategoria() {
  const contenedor = contenedorPrincipal;
  if (!contenedor) return;
  contenedor.innerHTML = '<h2>Productos Destacados</h2>';

  const destacadas = productosGlobales.slice(0, 6); // primeros 6 como destacados
  contenedor.innerHTML += crearGridHTML(destacadas, 'destacadas');

  // Agrupar por categoría (mostrar cada grupo)
  const categorias = [...new Set(productosGlobales.map(p => p.categoria || 'Otros'))];
  categorias.forEach(cat => {
    const productosCat = productosGlobales.filter(p => (p.categoria || 'Otros') === cat);
    if (productosCat.length > 0) {
      contenedor.innerHTML += `<div class="categoria-productos"><h3>${cat}</h3>${crearGridHTML(productosCat, cat)}</div>`;
    }
  });

  // Re-agregar eventos botones comprar
  attachComprarListeners();
}

/* helper para crear grid html - VERSIÓN MEJORADA */
function crearGridHTML(productos, id) {
    return `<div class="productos-grid" id="productos-${escapeHtml(id)}">
        ${productos.map(p => {
            const img = p.imagen_url
                ? (p.imagen_url.startsWith('http') ? p.imagen_url : (p.imagen_url.startsWith('/') ? p.imagen_url : `/uploads/img/productos/${p.imagen_url}`))
                : '/img/no-image.png';
            
            const stockEtiqueta = (typeof p.stock !== 'undefined') ? `<div class="stock">Stock: ${p.stock}</div>` : '';
            
            return `
            <div class="producto-card" data-product-id="${p.id}">
                <!-- 🔥 CONTENEDOR DE IMAGEN MEJORADO -->
                <div class="producto-imagen-container">
                    <img src="${escapeHtml(img)}" alt="${escapeHtml(p.nombre)}" 
                         class="producto-imagen"
                         onerror="this.src='/img/no-image.png'">
                </div>
                <div class="producto-info">
                    <h3>${escapeHtml(p.nombre)}</h3>
                    <p>${escapeHtml(p.descripcion || '')}</p>
                    <div class="precio">S/ ${Number(p.precio || 0).toFixed(2)}</div>
                    ${stockEtiqueta}
                    <button class="btn-comprar" data-product-id="${p.id}">🛒 Agregar al carrito</button>
                </div>
            </div>`;
        }).join('')}
    </div>`;
}

/* attach listeners a botones comprar actuales */
function attachComprarListeners() {
  document.querySelectorAll('.btn-comprar').forEach(b => {
    // evitar duplicar listeners
    b.replaceWith(b.cloneNode(true));
  });
  document.querySelectorAll('.btn-comprar').forEach(b => {
    b.addEventListener('click', (ev) => {
      const pid = ev.currentTarget.getAttribute('data-product-id');
      const producto = productosGlobales.find(x => String(x.id) === String(pid));
      if (!producto) return alert('Producto no encontrado');
      if (!producto.stock || Number(producto.stock) <= 0) {
        alert('❌ Este producto no tiene stock disponible.');
        return;
      }
      agregarAlCarrito(producto);

      // actualizar contador visible
      const carritoCount = document.getElementById('carritoCount');
      const carritoLocal = JSON.parse(localStorage.getItem('carrito') || '[]');
      if (carritoCount) carritoCount.textContent = carritoLocal.reduce((s, i) => s + (Number(i.cantidad) || 0), 0);
    });
  });
}

/* Filtrar por categoria (desde dropdown) */
function filtrarPorCategoria(categoria) {
  if (!categoria || categoria === 'Todas') {
    listarProductosPorCategoria();
    return;
  }
  const contenedor = contenedorPrincipal;
  contenedor.innerHTML = `<h2>${escapeHtml(categoria)}</h2>` + crearGridHTML(productosGlobales.filter(p => (p.categoria || 'Otros') === categoria), categoria);
  attachComprarListeners();
}

/* ==================== CARRITO (STATE + UI) ==================== */
function guardarCarrito() {
  localStorage.setItem('carrito', JSON.stringify(carrito));
}
function totalItemsCarrito() {
  return carrito.reduce((s, it) => s + (Number(it.cantidad) || 0), 0);
}

function abrirCarrito() {
  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  if (!usuario) {
    localStorage.setItem('postLoginReturn', JSON.stringify({ from: 'carrito' }));
    window.location.href = './html/login.html';
    return;
  }
  // impedir que admin compre
  if (usuario?.rol?.nombre === 'Administrador') {
    alert('Los administradores no pueden comprar productos.');
    return;
  }

  document.getElementById('overlay')?.classList.add('active');
  const modal = document.getElementById('carritoModal');
  if (modal) {
    modal.classList.add('active');
    modal.setAttribute('aria-hidden', 'false');
  }
  actualizarCarritoUI();
}
window.abrirCarrito = abrirCarrito;

function cerrarCarrito() {
  document.getElementById('overlay')?.classList.remove('active');
  const modal = document.getElementById('carritoModal');
  if (modal) {
    modal.classList.remove('active');
    modal.setAttribute('aria-hidden', 'true');
  }
}
window.cerrarCarrito = cerrarCarrito;

function agregarAlCarrito(producto) {
  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  if (!usuario) {
    localStorage.setItem('postLoginAdd', JSON.stringify({ productId: producto.id }));
    window.location.href = './html/login.html';
    return;
  }
  if (usuario?.rol?.nombre === 'Administrador') {
    alert('Los administradores no pueden comprar productos.');
    return;
  }
  if (!producto.stock || Number(producto.stock) <= 0) {
    alert('❌ Este producto no tiene stock disponible.');
    return;
  }

  const existente = carrito.find(i => String(i.id) === String(producto.id));
  if (existente) {
    if (Number(existente.cantidad) < Number(producto.stock)) {
      existente.cantidad = Number(existente.cantidad) + 1;
    } else {
      alert('⚠️ Has alcanzado el stock máximo de este producto.');
    }
  } else {
    carrito.push({
      id: producto.id,
      nombre: producto.nombre,
      precio: Number(producto.precio) || 0,
      imagen_url: producto.imagen_url || '/img/no-image.png',
      stock: Number(producto.stock) || 0,
      cantidad: 1
    });
  }
  guardarCarrito();
  actualizarCarritoUI();
  mostrarMiniNotificacion('Producto agregado al carrito');
}
window.agregarAlCarrito = agregarAlCarrito;

function eliminarDelCarrito(id) {
  carrito = carrito.filter(i => String(i.id) !== String(id));
  guardarCarrito();
  actualizarCarritoUI();
}
window.eliminarDelCarrito = eliminarDelCarrito;

function cambiarCantidad(id, delta) {
  const item = carrito.find(i => String(i.id) === String(id));
  if (!item) return;
  const nueva = Number(item.cantidad) + Number(delta);
  if (nueva <= 0) {
    eliminarDelCarrito(id);
    return;
  }
  if (nueva > Number(item.stock)) {
    item.cantidad = Number(item.stock);
    alert('⚠️ No hay más stock disponible.');
  } else {
    item.cantidad = nueva;
  }
  guardarCarrito();
  actualizarCarritoUI();
}
window.cambiarCantidad = cambiarCantidad;

function vaciarCarrito() {
  if (!confirm('¿Vaciar todo el carrito?')) return;
  carrito = [];
  guardarCarrito();
  actualizarCarritoUI();
}
window.vaciarCarrito = vaciarCarrito;

/* UI Carrito */
function actualizarCarritoUI() {
  const cont = document.getElementById('carritoItems');
  const totalDiv = document.getElementById('carritoTotal');
  const countSpan = document.getElementById('carritoCount');

  if (!cont || !totalDiv || !countSpan) {
    // si no están en DOM, actualizar contador mínimo si existe
    const count = document.getElementById('carritoCount');
    if (count) count.textContent = totalItemsCarrito();
    return;
  }

  cont.innerHTML = '';
  let total = 0;

  if (carrito.length === 0) {
    cont.innerHTML = `<p style="text-align:center;color:#666;padding:18px 8px;">Tu carrito está vacío.</p>`;
  } else {
    carrito.forEach(item => {
      const subtotal = Number(item.precio) * Number(item.cantidad);
      total += subtotal;

      const el = document.createElement('div');
      el.className = 'carrito-item';
      el.innerHTML = `
        <img src="${escapeHtml(item.imagen_url)}" alt="${escapeHtml(item.nombre)}" 
             onerror="this.src='/img/no-image.png'">
        <div class="carrito-item-info">
          <h4>${escapeHtml(item.nombre)}</h4>
          <p class="precio">S/ ${Number(item.precio).toFixed(2)}</p>
          <div class="qty-controls" aria-label="Controles de cantidad">
            <button onclick="cambiarCantidad('${item.id}', -1)">−</button>
            <div class="qty-badge" aria-live="polite">${item.cantidad}</div>
            <button onclick="cambiarCantidad('${item.id}', 1)">+</button>
            <small style="margin-left:8px;color:#666;">Stock: ${Number(item.stock)}</small>
          </div>
          <p style="margin-top:6px;color:#666;">Subtotal: S/ ${subtotal.toFixed(2)}</p>
        </div>
        <button class="btn-eliminar" onclick="eliminarDelCarrito('${item.id}')">🗑️</button>
      `;
      cont.appendChild(el);
    });
  }

  totalDiv.textContent = `Total: S/ ${Number(total).toFixed(2)}`;
  countSpan.textContent = totalItemsCarrito();
}

/* ==================== PAGOS - MANTENIENDO TODA LA FUNCIONALIDAD ==================== */
async function procesarPagoPayPal() {
  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  if (!usuario) { alert('Debes iniciar sesión'); window.location.href = './html/login.html'; return; }
  if (usuario?.rol?.nombre === 'Administrador') { alert('Los administradores no pueden comprar'); return; }
  if (carrito.length === 0) { alert('Tu carrito está vacío'); return; }

  try {
    const total = carrito.reduce((s,i) => s + (Number(i.precio) * Number(i.cantidad)), 0);

    // Crear venta en tu backend
    const ventaRes = await fetch('https://tiendavirtual-production-88d4.up.railway.app/api/ventas', {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({
        usuarioId: usuario.id,
        total: Number(total.toFixed(2)),
        metodoPagoId: 1,
        detalles: carrito.map(i => ({ productoId: i.id, cantidad: i.cantidad, precioUnitario: i.precio }))
      })
    });
    if (!ventaRes.ok) throw new Error('Error al crear la venta en el servidor');
    const venta = await ventaRes.json();

    // Solicita creación de pago PayPal
    const pagoRes = await fetch('https://tiendavirtual-production-88d4.up.railway.app/api/paypal/create-payment', {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({ amount: Number(total.toFixed(2)), description: `Compra - Orden #${venta.id}`, ventaId: venta.id })
    });
    if (!pagoRes.ok) throw new Error('Error al crear pago PayPal');
    const pagoData = await pagoRes.json();
    const redirectUrl = pagoData.approvalUrl || pagoData.url;
    if (!redirectUrl) throw new Error('No se recibió URL de pago PayPal');

    // limpiar carrito local (se podría esperar confirmación)
    carrito = [];
    guardarCarrito();
    actualizarCarritoUI();

    // redirigir al checkout
    window.location.href = redirectUrl;

  } catch (err) {
    console.error('❌ Error en el pago PayPal:', err);
    alert('❌ Error en el pago PayPal: ' + (err.message || err));
  }
}
window.procesarPagoPayPal = procesarPagoPayPal;

async function procesarPagoSimulado() {
  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  if (!usuario) { alert('Debes iniciar sesión para comprar'); window.location.href = './html/login.html'; return; }
  if (usuario?.rol?.nombre === 'Administrador') { alert('Los administradores no pueden comprar'); return; }
  if (carrito.length === 0) { alert('Tu carrito está vacío'); return; }

  const modal = document.getElementById('modalCarga');
  if (modal) modal.style.display = 'flex';
  try {
    const total = carrito.reduce((s,i) => s + (Number(i.precio) * Number(i.cantidad)), 0);

    const ventaRes = await fetch('https://tiendavirtual-production-88d4.up.railway.app/api/ventas', {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({
        usuarioId: usuario.id,
        total: Number(total.toFixed(2)),
        metodoPagoId: 2,
        detalles: carrito.map(i => ({ productoId: i.id, cantidad: i.cantidad, precioUnitario: i.precio }))
      })
    });
    if (!ventaRes.ok) throw new Error('Error al crear venta');
    const venta = await ventaRes.json();

    // Simular delay
    await new Promise(r => setTimeout(r, 1500));

    const pagoRes = await fetch(`https://tiendavirtual-production-88d4.up.railway.app/api/pago-simulado/pagar?ventaId=${venta.id}`, { method: 'POST' });
    if (!pagoRes.ok) throw new Error('Error al crear venta simulada');

    const html = await pagoRes.text();
    document.open();
    document.write(html);
    document.close();

    carrito = [];
    guardarCarrito();
    actualizarCarritoUI();

  } catch (err) {
    console.error('❌ Error en el pago simulado:', err);
    alert('❌ Error en el pago simulado: ' + (err.message || err));
  } finally {
    if (modal) modal.style.display = 'none';
  }
}
window.procesarPagoSimulado = procesarPagoSimulado;

/* ==================== POST-LOGIN helpers ==================== */
function procesarPostLoginAdd() {
  try {
    const post = JSON.parse(localStorage.getItem('postLoginAdd') || 'null');
    if (!post || !post.productId) return;
    const productosCache = JSON.parse(localStorage.getItem('productosCache') || '[]');
    const producto = productosCache.find(p => String(p.id) === String(post.productId));
    if (producto) {
      agregarAlCarrito(producto);
      abrirCarrito();
    }
    localStorage.removeItem('postLoginAdd');
  } catch (e) {
    console.warn('postLoginAdd error', e);
    localStorage.removeItem('postLoginAdd');
  }
}

/* Si el usuario volvió después de login y pidió abrir carrito */
(function handlePostLoginReturn() {
  try {
    const r = JSON.parse(localStorage.getItem('postLoginReturn') || 'null');
    if (r && r.from === 'carrito') {
      localStorage.removeItem('postLoginReturn');
      const u = JSON.parse(localStorage.getItem('usuario') || 'null');
      if (u) abrirCarrito();
    }
  } catch (e) {}
})();

/* ==================== CONTACTO ==================== */
function bindContactoForm() {
  const form = document.getElementById('formContacto');
  if (!form) return;
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    // Aquí podrías enviar a tu API; por ahora solo feedback
    alert('✅ Mensaje enviado. Gracias por contactarnos.');
    form.reset();
  });
}

/* ==================== UTIL HELPERS ==================== */
function escapeHtml(s) {
  if (s === null || s === undefined) return '';
  return String(s).replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;"}[m]));
}

function mostrarMiniNotificacion(msg, ms = 1500) {
  const n = document.createElement('div');
  n.style.cssText = 'position:fixed;right:18px;bottom:18px;background:#27ae60;color:white;padding:10px 14px;border-radius:10px;z-index:9999;box-shadow:0 6px 20px rgba(0,0,0,0.12)';
  n.textContent = msg;
  document.body.appendChild(n);
  setTimeout(()=> n.remove(), ms);
}

/* ==================== Exports (global) ==================== */
window.mostrarUsuario = mostrarUsuario;
window.agregarAlCarrito = agregarAlCarrito;
window.abrirCarrito = abrirCarrito;
window.cerrarCarrito = cerrarCarrito;
window.eliminarDelCarrito = eliminarDelCarrito;
window.cambiarCantidad = cambiarCantidad;
window.vaciarCarrito = vaciarCarrito;
window.procesarPagoPayPal = procesarPagoPayPal;

window.procesarPagoSimulado = procesarPagoSimulado;


