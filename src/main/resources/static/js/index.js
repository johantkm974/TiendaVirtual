const API_URL = "https://tiendavirtual-production-88d4.up.railway.app/api/productos";
const API_CATEGORIAS = "https://tiendavirtual-production-88d4.up.railway.app/api/categorias";
const contenedorPrincipal = document.getElementById("mainProductos");

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
    if(carrito.length===0){ cont.innerHTML='<p>Carrito vacío</p>'; totalDiv.textContent='S/ 0.00'; countSpan.textContent='0'; return; }
    carrito.forEach(i=>{
        const div=document.createElement('div'); div.className='carrito-item';
        div.innerHTML=`<img src="${escapeHtml(i.imagen_url)}" alt="${escapeHtml(i.nombre)}">
        <div class="carrito-info">
        <h4>${escapeHtml(i.nombre)}</h4>
        <div>
            <button onclick="cambiarCantidad('${i.id}',-1)">-</button>
            <span>${i.cantidad}</span>
            <button onclick="cambiarCantidad('${i.id}',1)">+</button>
        </div>
        <p>S/ ${(i.precio*i.cantidad).toFixed(2)}</p>
        <button onclick="eliminarDelCarrito('${i.id}')">🗑</button>
        </div>`; cont.appendChild(div);
        total += i.precio*i.cantidad;
    });
    totalDiv.textContent=`S/ ${total.toFixed(2)}`;
    countSpan.textContent=totalItemsCarrito();
}

/* ==================== FILTRO CATEGORIA ==================== */
function filtrarPorCategoria(categoria='Todas'){
    const grids = document.querySelectorAll('.productos-grid');
    grids.forEach(g=>{
        if(categoria==='Todas'){ g.style.display='grid'; return; }
        g.style.display=g.id.includes(escapeHtml(categoria))?'grid':'none';
    });
}

/* ==================== POST LOGIN ==================== */
function procesarPostLoginAdd(){
    const postLoginAdd = JSON.parse(localStorage.getItem('postLoginAdd') || 'null');
    if(!postLoginAdd) return;
    const prod = productosGlobales.find(p=>String(p.id)===String(postLoginAdd.productId));
    if(prod) agregarAlCarrito(prod);
    localStorage.removeItem('postLoginAdd');
}

function handlePostLoginReturn(){
    const postLoginReturn = JSON.parse(localStorage.getItem('postLoginReturn') || 'null');
    if(!postLoginReturn) return;
    if(postLoginReturn.from==='carrito') abrirCarrito();
    localStorage.removeItem('postLoginReturn');
}

/* ==================== FORM CONTACTO ==================== */
function bindContactoForm(){
    const form=document.getElementById('contactoForm');
    if(!form) return;
    form.addEventListener('submit', async e=>{
        e.preventDefault();
        alert('Formulario de contacto enviado (simulado)');
        form.reset();
    });
}

/* ==================== UTILS ==================== */
function escapeHtml(text){ return text?text.toString().replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m])):''; }

function mostrarMiniNotificacion(msg){
    const notif=document.createElement('div');
    notif.className='mini-notificacion';
    notif.textContent=msg;
    document.body.appendChild(notif);
    setTimeout(()=>notif.remove(),2000);
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




