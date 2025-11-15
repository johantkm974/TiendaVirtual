// carrito.js - gestión de carrito, modal lateral, stock y pagos (PayPal + pago simulado)

// ========== Estado ==========
let carrito = JSON.parse(localStorage.getItem('carrito') || '[]');

// ========== Backend base (sin dominio, solo prefijo /api) ==========
const API_BASE = "/api";

// ========== Utilidades ==========
function guardarCarrito() {
  localStorage.setItem('carrito', JSON.stringify(carrito));
}
function totalItemsCarrito() {
  return carrito.reduce((s, it) => s + (Number(it.cantidad) || 0), 0);
}

// ========== Apertura / cierre carrito ==========
function abrirCarrito() {
  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  if (!usuario) {
    localStorage.setItem('postLoginReturn', JSON.stringify({ from: 'carrito' }));
    window.location.href = './html/login.html';
    return;
  }
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

function cerrarCarrito() {
  document.getElementById('overlay')?.classList.remove('active');
  const modal = document.getElementById('carritoModal');
  if (modal) {
    modal.classList.remove('active');
    modal.setAttribute('aria-hidden', 'true');
  }
}

// ========== Agregar / eliminar / cambiar cantidad ==========
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
      existente.cantidad += 1;
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

function eliminarDelCarrito(id) {
  carrito = carrito.filter(i => String(i.id) !== String(id));
  guardarCarrito();
  actualizarCarritoUI();
}

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

function vaciarCarrito() {
  if (!confirm('¿Vaciar todo el carrito?')) return;
  carrito = [];
  guardarCarrito();
  actualizarCarritoUI();
}

// ========== UI Carrito ==========
function actualizarCarritoUI() {
  const cont = document.getElementById('carritoItems');
  const totalDiv = document.getElementById('carritoTotal');
  const countSpan = document.getElementById('carritoCount');
  if (!cont || !totalDiv || !countSpan) return;

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
        <img src="${escapeHtml(item.imagen_url)}" alt="${escapeHtml(item.nombre)}">
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

// ========== Helpers ==========
function escapeHtml(s) {
  if (!s && s !== 0) return '';
  return String(s).replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;"}[m]));
}

function mostrarMiniNotificacion(msg, ms = 1800) {
  const n = document.createElement('div');
  n.style.cssText = 'position:fixed;right:18px;bottom:18px;background:#0b6623;color:white;padding:10px 14px;border-radius:10px;z-index:9999;box-shadow:0 6px 20px rgba(0,0,0,0.12)';
  n.textContent = msg;
  document.body.appendChild(n);
  setTimeout(()=> n.remove(), ms);
}

// ========== PAGOS ==========
async function procesarPagoPayPal() {
  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  if (!usuario) { alert('Debes iniciar sesión'); window.location.href = './html/login.html'; return; }
  if (usuario?.rol?.nombre === 'Administrador') { alert('Los administradores no pueden comprar'); return; }
  if (carrito.length === 0) { alert('Tu carrito está vacío'); return; }

  try {
    const total = carrito.reduce((s,i) => s + (Number(i.precio) * Number(i.cantidad)), 0);

    const ventaRes = await fetch(`${API_BASE}/ventas`, {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({
        usuarioId: usuario.id,
        total: Number(total.toFixed(2)),
        metodoPagoId: 1, // PayPal
        detalles: carrito.map(i => ({ productoId: i.id, cantidad: i.cantidad, precioUnitario: i.precio }))
      })
    });
    if (!ventaRes.ok) throw new Error('Error al crear la venta en el servidor');
    const venta = await ventaRes.json();

    const pagoRes = await fetch(`${API_BASE}/paypal/create-payment`, {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({
        amount: Number(total.toFixed(2)),
        description: `Compra - Orden #${venta.id}`,
        ventaId: venta.id
      })
    });
    if (!pagoRes.ok) throw new Error('Error al crear pago PayPal');
    const pagoData = await pagoRes.json();
    const redirectUrl = pagoData.approvalUrl || pagoData.url;
    if (!redirectUrl) throw new Error('No se recibió URL de pago PayPal');

    carrito = [];
    guardarCarrito();
    actualizarCarritoUI();
    window.location.href = redirectUrl;

  } catch (err) {
    console.error('❌ Error en el pago PayPal:', err);
    alert('❌ Error en el pago PayPal: ' + (err.message || err));
  }
}

async function procesarPagoSimulado() {
  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  if (!usuario) { alert('Debes iniciar sesión para comprar'); window.location.href = './html/login.html'; return; }
  if (usuario?.rol?.nombre === 'Administrador') { alert('Los administradores no pueden comprar'); return; }
  if (carrito.length === 0) { alert('Tu carrito está vacío'); return; }

  const modal = document.getElementById('modalCarga');
  modal.style.display = 'flex'; // Mostrar modal

  try {
    const total = carrito.reduce((s,i) => s + (Number(i.precio) * Number(i.cantidad)), 0);

    const ventaRes = await fetch(`${API_BASE}/ventas`, {
      method:'POST',
      headers:{'Content-Type':'application/json'},
      body: JSON.stringify({
        usuarioId: usuario.id,
        total: Number(total.toFixed(2)),
        metodoPagoId: 2, // Pago simulado
        detalles: carrito.map(i => ({ productoId: i.id, cantidad: i.cantidad, precioUnitario: i.precio }))
      })
    });
    if (!ventaRes.ok) throw new Error('Error al crear venta');
    const venta = await ventaRes.json();

    await new Promise(r => setTimeout(r, 2000)); // efecto de carga

    const pagoRes = await fetch(`${API_BASE}/pago-simulado/pagar?ventaId=${venta.id}`, {
      method: 'POST'
    });
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
    modal.style.display = 'none'; // Ocultar modal
  }
}

// ========== Post-login ==========
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

// ========== INIT ==========
document.addEventListener('DOMContentLoaded', () => {
  actualizarCarritoUI();

  try {
    const r = JSON.parse(localStorage.getItem('postLoginReturn') || 'null');
    if (r?.from === 'carrito') {
      localStorage.removeItem('postLoginReturn');
      const u = JSON.parse(localStorage.getItem('usuario') || 'null');
      if (u) abrirCarrito();
    }
  } catch(e) {}

  procesarPostLoginAdd();

  const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
  if (usuario?.rol?.nombre === 'Administrador') {
    const btnAdmin = document.createElement('button');
    btnAdmin.textContent = '🛠 Panel Administrador';
    btnAdmin.className = 'btn-admin';
    btnAdmin.onclick = () => { window.location.href = './html/productos.html'; };
    const headerActions = document.querySelector('.header-actions');
    if (headerActions) headerActions.insertBefore(btnAdmin, headerActions.firstChild);

    const carritoIcon = document.getElementById('carritoIcon');
    if (carritoIcon) carritoIcon.style.display = 'none';
  }
});

// ===== Funciones globales =====
window.agregarAlCarrito = agregarAlCarrito;
window.abrirCarrito = abrirCarrito;
window.cerrarCarrito = cerrarCarrito;
window.eliminarDelCarrito = eliminarDelCarrito;
window.cambiarCantidad = cambiarCantidad;
window.vaciarCarrito = vaciarCarrito;
window.procesarPagoPayPal = procesarPagoPayPal;
window.procesarPagoSimulado = procesarPagoSimulado;


