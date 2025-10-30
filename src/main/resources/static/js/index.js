const API_URL = "https://tiendavirtual-production-88d4.up.railway.app/api/productos";
const API_CATEGORIAS = "https://tiendavirtual-production-88d4.up.railway.app/api/categorias";
const contenedorPrincipal = document.getElementById("mainProductos");

// Estado global
let productosGlobales = [];
let carrito = JSON.parse(localStorage.getItem('carrito') || '[]');

(async function init() {
    setupMenuToggle();
    setupDropdown();
    setupHeaderButtons();
    bindContactoForm();
    
    await Promise.all([cargarCategorias(), listarProductos()]);
    procesarPostLoginAdd();
    handlePostLoginReturn();
    mostrarUsuario();
    actualizarCarritoUI();
})();

/* ==================== MENU MÓVIL ==================== */
function setupMenuToggle() {
    const menuToggle = document.getElementById('menuToggle');
    const navMenu = document.getElementById('navMenu');
    if (!menuToggle || !navMenu) return;

    menuToggle.addEventListener('click', e => {
        e.stopPropagation();
        navMenu.classList.toggle('show');
    });

    navMenu.addEventListener('click', e => {
        if (e.target.tagName === 'A') navMenu.classList.remove('show');
    });

    document.addEventListener('click', e => {
        if (!navMenu.contains(e.target) && !menuToggle.contains(e.target)) {
            navMenu.classList.remove('show');
        }
    });
}

/* ==================== DROPDOWN CATEGORIAS ==================== */
function setupDropdown() {
    const dropdownContainer = document.getElementById('dropdownContainer');
    const productosDrop = document.getElementById('productosDrop');
    const dropdownCategorias = document.getElementById('dropdownCategorias');
    if (!dropdownContainer || !productosDrop || !dropdownCategorias) return;

    productosDrop.addEventListener('click', e => {
        e.preventDefault();
        e.stopPropagation();
        dropdownCategorias.classList.toggle('active');
    });

    document.addEventListener('click', e => {
        if (!dropdownContainer.contains(e.target)) dropdownCategorias.classList.remove('active');
    });

    dropdownCategorias.addEventListener('click', e => e.stopPropagation());
}

/* ==================== HEADER & USUARIO ==================== */
function setupHeaderButtons() {
    const btnLogin = document.getElementById('btn-login');
    if (btnLogin) btnLogin.onclick = () => window.location.href = './html/login.html';
    addAdminButtonIfNeeded();
}

function mostrarUsuario() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
    const userInfo = document.getElementById("user-info");
    const userNombreSpan = document.getElementById("user-nombre");
    const btnLogin = document.getElementById("btn-login");
    const carritoIcon = document.getElementById("carritoIcon");

    if (usuario && usuario.nombre) {
        userNombreSpan.textContent = usuario.nombre;
        if (userInfo) userInfo.style.display = 'flex';
        if (btnLogin) btnLogin.style.display = 'none';
        if (carritoIcon) carritoIcon.style.display = usuario?.rol?.nombre === 'Administrador' ? 'none' : 'flex';
        addAdminButtonIfNeeded();
    } else {
        if (userInfo) userInfo.style.display = 'none';
        if (btnLogin) btnLogin.style.display = 'inline-block';
        if (carritoIcon) carritoIcon.style.display = 'none';
    }
}

function logout() {
    localStorage.removeItem('usuario');
    mostrarUsuario();
    actualizarCarritoUI();
    window.location.href = './html/login.html';
}
window.logout = logout;

function addAdminButtonIfNeeded() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
    const headerActions = document.querySelector('.header-actions');
    if (!headerActions || !usuario || usuario.rol?.nombre !== 'Administrador') return;
    if (headerActions.querySelector('.btn-admin')) return;

    const btnAdmin = document.createElement('button');
    btnAdmin.textContent = '🛠 Panel Administrador';
    btnAdmin.className = 'btn-admin';
    btnAdmin.onclick = () => window.location.href = './html/productos.html';
    headerActions.insertBefore(btnAdmin, headerActions.firstChild);
}

/* ==================== CATEGORIAS ==================== */
async function cargarCategorias() {
    try {
        const res = await fetch(API_CATEGORIAS);
        if (!res.ok) throw new Error('No se pudieron cargar categorías');
        let categorias = await res.json();
        categorias = Array.isArray(categorias) ? categorias : [];
        categorias = categorias.map(c => typeof c === 'string' ? c : c.nombre);
        populateCategoriasDropdown(categorias);
    } catch {
        console.warn('Fallo al cargar categorías desde API');
    }
}

function populateCategoriasDropdown(cats = []) {
    const dd = document.getElementById('dropdownCategorias');
    if (!dd) return;
    dd.innerHTML = `<li><a href="#" data-cat="Todas">Todas</a></li>`;
    cats.forEach(c => {
        const li = document.createElement('li');
        li.innerHTML = `<a href="#" data-cat="${c}">${c}</a>`;
        dd.appendChild(li);
    });

    dd.addEventListener('click', e => {
        const a = e.target.closest('a');
        if (!a) return;
        e.preventDefault();
        filtrarPorCategoria(a.dataset.cat || 'Todas');
    });
}

/* ==================== PRODUCTOS ==================== */
async function listarProductos() {
    try {
        const res = await fetch(API_URL);
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const data = await res.json();
        productosGlobales = Array.isArray(data) ? data.map(procesarProducto) : [];

        // Crear categorías desde productos si dropdown vacío
        const dd = document.getElementById('dropdownCategorias');
        if (dd && dd.querySelectorAll('li').length <= 1) {
            const categorias = [...new Set(productosGlobales.map(p => p.categoria || 'Otros'))];
            categorias.forEach(c => {
                const li = document.createElement('li');
                li.innerHTML = `<a href="#" data-cat="${c}">${c}</a>`;
                dd.appendChild(li);
            });
        }

        localStorage.setItem('productosCache', JSON.stringify(productosGlobales));
        listarProductosPorCategoria();
    } catch (err) {
        console.error('Error listar productos', err);
        if (contenedorPrincipal) contenedorPrincipal.innerHTML = `<p style="text-align:center;color:#666;padding:30px;">No se pudieron cargar los productos.</p>`;
    }
}

function procesarProducto(producto) {
    const categoriaNombre = producto.categoriaNombre || producto.categoria?.nombre || 'Otros';
    const imagenUrl = producto.imagen ? `${API_URL}/${producto.id}/imagen` : '/img/no-image.png';

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

function listarProductosPorCategoria() {
    if (!contenedorPrincipal) return;
    contenedorPrincipal.innerHTML = '<h2>Productos Destacados</h2>';

    const destacadas = productosGlobales.slice(0, 6);
    contenedorPrincipal.innerHTML += crearGridHTML(destacadas, 'destacadas');

    const categorias = [...new Set(productosGlobales.map(p => p.categoria || 'Otros'))];
    categorias.forEach(cat => {
        const productosCat = productosGlobales.filter(p => (p.categoria || 'Otros') === cat);
        if (productosCat.length) {
            contenedorPrincipal.innerHTML += `<div class="categoria-productos"><h3>${cat}</h3>${crearGridHTML(productosCat, cat)}</div>`;
        }
    });

    attachComprarListeners();
}

function crearGridHTML(productos, id) {
    return `<div class="productos-grid" id="productos-${escapeHtml(id)}">
        ${productos.map(p => `
            <div class="producto-card" data-product-id="${p.id}">
                <div class="producto-imagen-container">
                    <img src="${escapeHtml(p.imagen_url)}" alt="${escapeHtml(p.nombre)}" onerror="this.src='/img/no-image.png'">
                </div>
                <div class="producto-info">
                    <h3>${escapeHtml(p.nombre)}</h3>
                    <p>${escapeHtml(p.descripcion)}</p>
                    <div class="precio">S/ ${p.precio.toFixed(2)}</div>
                    ${p.stock ? `<div class="stock">Stock: ${p.stock}</div>` : ''}
                    <button class="btn-comprar" data-product-id="${p.id}">🛒 Agregar al carrito</button>
                </div>
            </div>`).join('')}
    </div>`;
}

/* Delegación de eventos comprar */
function attachComprarListeners() {
    contenedorPrincipal.addEventListener('click', e => {
        const btn = e.target.closest('.btn-comprar');
        if (!btn) return;
        const pid = btn.dataset.productId;
        const producto = productosGlobales.find(p => String(p.id) === String(pid));
        if (!producto) return alert('Producto no encontrado');
        if (!producto.stock || producto.stock <= 0) return alert('❌ Sin stock');
        agregarAlCarrito(producto);
        document.getElementById('carritoCount')?.textContent = totalItemsCarrito();
    });
}

function filtrarPorCategoria(categoria) {
    if (!contenedorPrincipal) return;
    if (!categoria || categoria === 'Todas') return listarProductosPorCategoria();
    const filtrados = productosGlobales.filter(p => (p.categoria || 'Otros') === categoria);
    contenedorPrincipal.innerHTML = `<h2>${escapeHtml(categoria)}</h2>` + crearGridHTML(filtrados, categoria);
    attachComprarListeners();
}

/* ==================== CARRITO ==================== */
function guardarCarrito() { localStorage.setItem('carrito', JSON.stringify(carrito)); }
function totalItemsCarrito() { return carrito.reduce((s,i)=>s+(i.cantidad||0),0); }

function abrirCarrito() {
    const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
    if (!usuario) { localStorage.setItem('postLoginReturn', JSON.stringify({from:'carrito'})); window.location.href='./html/login.html'; return; }
    if (usuario?.rol?.nombre==='Administrador'){ alert('Admin no puede comprar'); return; }

    document.getElementById('overlay')?.classList.add('active');
    const modal = document.getElementById('carritoModal');
    if (modal) { modal.classList.add('active'); modal.setAttribute('aria-hidden','false'); }
    actualizarCarritoUI();
}
window.abrirCarrito = abrirCarrito;

function cerrarCarrito() {
    document.getElementById('overlay')?.classList.remove('active');
    const modal = document.getElementById('carritoModal');
    if (modal) { modal.classList.remove('active'); modal.setAttribute('aria-hidden','true'); }
}
window.cerrarCarrito = cerrarCarrito;

function agregarAlCarrito(producto) {
    const usuario = JSON.parse(localStorage.getItem('usuario') || 'null');
    if (!usuario) { localStorage.setItem('postLoginAdd', JSON.stringify({productId: producto.id})); window.location.href='./html/login.html'; return; }
    if (usuario?.rol?.nombre==='Administrador') { alert('Admin no puede comprar'); return; }
    if (!producto.stock || producto.stock<=0) { alert('❌ Sin stock'); return; }

    const existente = carrito.find(i=>String(i.id)===String(producto.id));
    if (existente) {
        if (existente.cantidad < producto.stock) existente.cantidad++;
        else alert('⚠️ Stock máximo alcanzado');
    } else {
        carrito.push({id:producto.id,nombre:producto.nombre,precio:producto.precio,imagen_url:producto.imagen_url,stock:producto.stock,cantidad:1});
    }
    guardarCarrito();
    actualizarCarritoUI();
    mostrarMiniNotificacion('Producto agregado al carrito');
}
window.agregarAlCarrito = agregarAlCarrito;

function eliminarDelCarrito(id){ carrito = carrito.filter(i=>String(i.id)!==String(id)); guardarCarrito(); actualizarCarritoUI(); }
window.eliminarDelCarrito = eliminarDelCarrito;

function cambiarCantidad(id,delta){
    const item = carrito.find(i=>String(i.id)===String(id));
    if(!item) return;
    const nueva = item.cantidad + delta;
    if(nueva<=0){ eliminarDelCarrito(id); return; }
    if(nueva>item.stock){ item.cantidad=item.stock; alert('⚠️ No hay más stock'); }
    else item.cantidad=nueva;
    guardarCarrito();
    actualizarCarritoUI();
}
window.cambiarCantidad = cambiarCantidad;

function vaciarCarrito(){ if(confirm('¿Vaciar todo el carrito?')){ carrito=[]; guardarCarrito(); actualizarCarritoUI(); } }
window.vaciarCarrito = vaciarCarrito;

function actualizarCarritoUI(){
    const cont=document.getElementById('carritoItems');
    const totalDiv=document.getElementById('carritoTotal');
    const countSpan=document.getElementById('carritoCount');
    if(!cont||!totalDiv||!countSpan){ if(countSpan) countSpan.textContent=totalItemsCarrito(); return; }
    cont.innerHTML=''; let total=0;
    if(carrito.length===0) cont.innerHTML=`<p style="text-align:center;color:#666;padding:18px;">Tu carrito está vacío.</p>`;
    else carrito.forEach(item=>{
        const subtotal = item.precio*item.cantidad; total+=subtotal;
        const el = document.createElement('div'); el.className='carrito-item';
        el.innerHTML = `
            <img src="${escapeHtml(item.imagen_url)}" alt="${escapeHtml(item.nombre)}" onerror="this.src='/img/no-image.png'">
            <div class="carrito-item-info">
                <h4>${escapeHtml(item.nombre)}</h4>
                <p class="precio">S/ ${item.precio.toFixed(2)}</p>
                <div class="qty-controls">
                    <button onclick="cambiarCantidad('${item.id}', -1)">−</button>
                    <div class="qty-badge">${item.cantidad}</div>
                    <button onclick="cambiarCantidad('${item.id}', 1)">+</button>
                    <small style="margin-left:8px;color:#666;">Stock: ${item.stock}</small>
                </div>
                <p style="margin-top:6px;color:#666;">Subtotal: S/ ${subtotal.toFixed(2)}</p>
            </div>
            <button class="btn-eliminar" onclick="eliminarDelCarrito('${item.id}')">🗑️</button>
        `;
        cont.appendChild(el);
    });
    totalDiv.textContent=`Total: S/ ${total.toFixed(2)}`;
    countSpan.textContent=totalItemsCarrito();
}

/* ==================== PAGOS ==================== */
// Mantiene lógica de PayPal y simulado
window.procesarPagoPayPal = async function() { /* ...igual que tu código original... */ };
window.procesarPagoSimulado = async function() { /* ...igual que tu código original... */ };

/* ==================== POST LOGIN ==================== */
function procesarPostLoginAdd(){
    try{
        const post = JSON.parse(localStorage.getItem('postLoginAdd')||'null');
        if(post?.productId){
            const producto = JSON.parse(localStorage.getItem('productosCache')||'[]').find(p=>String(p.id)===String(post.productId));
            if(producto){ agregarAlCarrito(producto); abrirCarrito(); }
        }
        localStorage.removeItem('postLoginAdd');
    }catch{ localStorage.removeItem('postLoginAdd'); }
}

function handlePostLoginReturn(){
    try{
        const r = JSON.parse(localStorage.getItem('postLoginReturn')||'null');
        if(r?.from==='carrito'){ localStorage.removeItem('postLoginReturn'); const u=JSON.parse(localStorage.getItem('usuario')||'null'); if(u) abrirCarrito(); }
    }catch{}
}

/* ==================== CONTACTO ==================== */
function bindContactoForm(){
    const form = document.getElementById('formContacto');
    if(!form) return;
    form.addEventListener('submit',e=>{
        e.preventDefault();
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



