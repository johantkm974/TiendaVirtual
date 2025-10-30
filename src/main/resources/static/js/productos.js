const API_URL = "https://tiendavirtual-production-88d4.up.railway.app/api/productos";
const CATEGORIAS_URL = "https://tiendavirtual-production-88d4.up.railway.app/api/categorias";

const tabla = document.querySelector("#tablaProductos tbody");
const modal = document.getElementById("modalProducto");
const btnAgregar = document.getElementById("btnAgregar");
const cerrarModal = document.getElementById("cerrarModal");
const form = document.getElementById("productoForm");
const imagenPreview = document.getElementById("imagenPreview");
const filtroCategoria = document.getElementById("filtroCategoria");

// ===== Estado usuario =====
let usuario = JSON.parse(localStorage.getItem('usuario'));
if (!usuario) window.location.href = './login.html';

// ===== Header usuario/admin =====
const headerActions = document.getElementById('headerActions');
const spanNombre = document.createElement('span');
spanNombre.textContent = `Hola, ${usuario.nombre}`;
spanNombre.style.marginRight = '10px';
headerActions.appendChild(spanNombre);

// Botón admin
if (usuario.rol?.nombre === 'Administrador') {
  const btnAdmin = document.createElement('button');
  btnAdmin.textContent = '🛠 Administrador';
  btnAdmin.className = 'btn-admin';
  btnAdmin.onclick = () => window.location.href = './productos.html';
  headerActions.appendChild(btnAdmin);

  const carritoDiv = document.querySelector('.carrito');
  if (carritoDiv) carritoDiv.style.display = 'none';
}

// Botón cerrar sesión
const btnLogout = document.createElement('button');
btnLogout.textContent = 'Cerrar sesión';
btnLogout.className = 'btn-logout';
btnLogout.onclick = () => {
  localStorage.removeItem('usuario');
  window.location.href = '../index.html';
};
headerActions.appendChild(btnLogout);

// ===== Cargar datos al inicio =====
document.addEventListener("DOMContentLoaded", () => {
  listarProductos();
  cargarCategorias();
});

// ===== Abrir modal nuevo producto =====
btnAgregar.addEventListener("click", () => {
  form.reset();
  document.getElementById("id").value = "";
  imagenPreview.style.display = "none";
  document.getElementById("tituloModal").textContent = "Nuevo Producto";
  modal.style.display = "flex";
});

// ===== Cerrar modal =====
cerrarModal.addEventListener("click", () => modal.style.display = "none");

// ===== Cargar categorías =====
function cargarCategorias() {
  fetch(CATEGORIAS_URL)
    .then(res => res.json())
    .then(categorias => {
      const selectForm = document.querySelector("select[name='categoria.id']");
      const selectFiltro = filtroCategoria;

      selectForm.innerHTML = `<option value="">-- Selecciona una categoría --</option>`;
      selectFiltro.innerHTML = `<option value="">Todas las categorías</option>`;

      categorias.forEach(cat => {
        selectForm.innerHTML += `<option value="${cat.id}">${cat.nombre}</option>`;
        selectFiltro.innerHTML += `<option value="${cat.id}">${cat.nombre}</option>`;
      });
    })
    .catch(() => mostrarMensaje("Error", "No se pudieron cargar las categorías", "error"));
}

// ===== Listar productos =====
function listarProductos(categoriaId = "") {
  const url = categoriaId ? `${API_URL}/categoria/${categoriaId}` : API_URL;

  fetch(url)
    .then(res => res.json())
    .then(data => {
      tabla.innerHTML = "";
      data.forEach(p => {
        tabla.innerHTML += `
          <tr>
            <td>${p.id}</td>
            <td>${p.nombre}</td>
            <td>S/ ${parseFloat(p.precio).toFixed(2)}</td>
            <td>${p.stock}</td>
            <td>${p.categoria?.nombre || "Sin categoría"}</td>
            <td>${p.imagen_url ? `<img src="${p.imagen_url}" width="60" height="60">` : "Sin imagen"}</td>
            <td>
              <button class="btn-editar" onclick="editar(${p.id})">✏️ Editar</button>
              <button class="btn-eliminar" onclick="eliminar(${p.id})">🗑️ Eliminar</button>
            </td>
          </tr>`;
      });
    })
    .catch(err => console.error("Error al listar productos:", err));
}

filtroCategoria.addEventListener("change", e => listarProductos(e.target.value));

// ===== Subir imagen =====
async function subirImagen(file) {
  const formData = new FormData();
  formData.append("file", file);
  const res = await fetch(`${API_URL}/subir-imagen`, { method: "POST", body: formData });
  return await res.text();
}

// ===== Guardar / editar producto =====
form.addEventListener("submit", async e => {
  e.preventDefault();
  const id = document.getElementById("id").value;
  const file = document.getElementById("imagen").files[0];
  const categoriaId = document.querySelector("select[name='categoria.id']").value;
  let imagenUrl = document.getElementById("imagenActual").value;

  if (file) imagenUrl = await subirImagen(file);

  const producto = {
    nombre: document.getElementById("nombre").value,
    descripcion: document.getElementById("descripcion").value,
    precio: parseFloat(document.getElementById("precio").value),
    stock: parseInt(document.getElementById("stock").value),
    imagen_url: imagenUrl,
    categoria: categoriaId ? { id: parseInt(categoriaId) } : null
  };

  const method = id ? "PUT" : "POST";
  const url = id ? `${API_URL}/${id}` : API_URL;

  fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(producto)
  })
    .then(() => {
      modal.style.display = "none";
      listarProductos();
      mostrarMensaje(
        id ? "Producto actualizado" : "Producto agregado",
        id ? "El producto se actualizó correctamente ✅" : "El producto fue guardado exitosamente 🛒",
        "success"
      );
    })
    .catch(() => mostrarMensaje("Error", "No se pudo guardar el producto", "error"));
});

// ===== Editar producto =====
async function editar(id) {
  const res = await fetch(`${API_URL}/${id}`);
  const p = await res.json();

  modal.style.display = "flex";
  document.getElementById("tituloModal").textContent = "Editar Producto";
  document.getElementById("id").value = p.id;
  document.getElementById("nombre").value = p.nombre;
  document.getElementById("descripcion").value = p.descripcion || "";
  document.getElementById("precio").value = p.precio;
  document.getElementById("stock").value = p.stock;
  document.querySelector("select[name='categoria.id']").value = p.categoria?.id || "";
  document.getElementById("imagenActual").value = p.imagen_url || "";

  if (p.imagen_url) {
    imagenPreview.src = `${p.imagen_url}?t=${new Date().getTime()}`;
    imagenPreview.style.display = "block";
  } else {
    imagenPreview.style.display = "none";
  }

  document.getElementById("imagen").value = "";
}

// ===== Eliminar producto =====
function eliminar(id) {
  Swal.fire({
    title: "¿Eliminar producto?",
    text: "Esta acción no se puede deshacer.",
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: "#d33",
    cancelButtonColor: "#3085d6",
    confirmButtonText: "Sí, eliminar"
  }).then(result => {
    if (result.isConfirmed) {
      fetch(`${API_URL}/${id}`, { method: "DELETE" })
        .then(() => {
          listarProductos();
          mostrarMensaje("Eliminado", "El producto fue eliminado correctamente 🗑️", "success");
        })
        .catch(() => mostrarMensaje("Error", "No se pudo eliminar el producto", "error"));
    }
  });
}

// ===== Vista previa de imagen =====
document.getElementById("imagen").addEventListener("change", e => {
  const file = e.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = ev => {
      imagenPreview.src = ev.target.result;
      imagenPreview.style.display = "block";
    };
    reader.readAsDataURL(file);
  }
});

// ===== Mensajes personalizados =====
function mostrarMensaje(titulo, texto, icono = "success") {
  Swal.fire({
    title: titulo,
    text: texto,
    icon: icono,
    confirmButtonColor: "#3085d6",
    confirmButtonText: "Aceptar",
    timer: 2000,
    timerProgressBar: true
  });
}

// Loader
window.addEventListener("load", () => {
  const loader = document.getElementById("loader");
  loader.classList.add("oculto");
  setTimeout(() => loader.style.display = "none", 500);
});


