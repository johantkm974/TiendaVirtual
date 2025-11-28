let todasLasVentas = [];

// ================================
// API BASE
// ================================
const API_BASE = "/api";

// ================================
// Cambiar Sección
// ================================
function mostrarSeccion(id) {
  document.querySelectorAll(".seccion").forEach(s => s.classList.remove("visible"));
  document.getElementById(id).classList.add("visible");
  if (id === "usuarios") cargarUsuarios();
  if (id === "ventas") cargarVentas();
}

// ================================
// Cargar Usuarios + Agregar Botón EDITAR
// ================================
async function cargarUsuarios() {
  const tabla = document.querySelector("#tablaUsuarios tbody");
  tabla.innerHTML = "";
  try {
    const res = await fetch(`${API_BASE}/usuarios`);
    const usuarios = await res.json();

    usuarios.forEach(u => {
      const esAdmin = u.rol && (u.rol.id === 1 || u.rol.nombre?.toUpperCase() === "ADMINISTRADOR");

      const fila = document.createElement("tr");
      fila.innerHTML = `
        <td>${u.id}</td>
        <td>${u.nombre}</td>
        <td>${u.correo}</td>
        <td>${esAdmin ? "Administrador" : "Cliente"}</td>
        <td>

          <!-- BOTÓN EDITAR -->
          <button class="btn btn-editar"
              onclick="abrirModal(${u.id}, '${u.nombre}', '${u.correo}', ${esAdmin})"
              ${esAdmin ? 'disabled style="opacity:0.5;cursor:not-allowed;"' : ''}>
              ✏ Editar
          </button>

          <!-- BOTÓN ELIMINAR (solo clientes) -->
          ${esAdmin
            ? ""
            : `<button class="btn btn-eliminar" onclick="eliminarUsuario(${u.id})">🗑 Eliminar</button>`
          }

        </td>
      `;
      tabla.appendChild(fila);
    });
  } catch (error) {
    document.getElementById("mensaje").textContent = "Error al cargar usuarios.";
    console.error(error);
  }
}

// ================================
// Abrir Modal Edición
// ================================
function abrirModal(id, nombre, correo, esAdmin) {
  if (esAdmin) {
    Swal.fire("No permitido", "No puedes editar al Administrador", "error");
    return;
  }

  document.getElementById('editId').value = id;
  document.getElementById('editNombre').value = nombre;
  document.getElementById('editCorreo').value = correo;

  document.getElementById('modalEditar').style.display = "flex";
}

function cerrarModal() {
  document.getElementById('modalEditar').style.display = "none";
}

// ================================
// Guardar Cambios Usuario (PUT)
// ================================
async function guardarCambiosUsuario() {
  const id = document.getElementById('editId').value;
  const nombre = document.getElementById('editNombre').value;
  const correo = document.getElementById('editCorreo').value;
  const contrasena = document.getElementById('editContrasena').value;

  try {
    const res = await fetch(`${API_BASE}/usuarios/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nombre, correo, contrasena })
    });

    if (!res.ok) {
      const error = await res.text();
      Swal.fire("Error", error, "error");
      return;
    }

    Swal.fire("Éxito", "Usuario actualizado correctamente", "success");
    cerrarModal();
    cargarUsuarios();
  } catch (error) {
    Swal.fire("Error", "No se pudo actualizar el usuario", "error");
    console.error(error);
  }
}

// ================================
// Eliminar Usuario
// ================================
async function eliminarUsuario(id) {
  if (!confirm("¿Seguro que deseas eliminar este usuario?")) return;

  try {
    const res = await fetch(`${API_BASE}/usuarios/${id}`, { method: "DELETE" });
    const data = await res.text();
    alert(data);
    cargarUsuarios();
  } catch (error) {
    alert("Error al eliminar usuario.");
    console.error(error);
  }
}

// ================================
// Cargar Ventas
// ================================
async function cargarVentas() {
  const tabla = document.querySelector("#tablaVentas tbody");
  tabla.innerHTML = "";
  try {
    const res = await fetch(`${API_BASE}/ventas`);
    todasLasVentas = await res.json();
    mostrarVentas(todasLasVentas);
  } catch (error) {
    document.getElementById("mensaje").textContent = "Error al cargar ventas.";
    console.error(error);
  }
}

function mostrarVentas(ventas) {
  const tabla = document.querySelector("#tablaVentas tbody");
  tabla.innerHTML = "";

  ventas.forEach(v => {
    const fila = document.createElement("tr");
    fila.innerHTML = `
      <td>${v.id}</td>
      <td>${v.usuario ? v.usuario.nombre : "Sin usuario"}</td>
      <td>${v.metodoPago ? v.metodoPago.nombre : "N/A"}</td>
      <td>${v.total ? v.total.toFixed(2) : "0.00"}</td>
      <td>${v.estadoPago || "Pendiente"}</td>
      <td>
        ${v.estadoPago === "APROBADO"
          ? `<button class="btn btn-ver" onclick="verRecibo(${v.id})">📄 Ver PDF</button>`
          : `<button class="btn btn-confirmar" onclick="confirmarPago(${v.id})">✅ Confirmar</button>`}
        <button class="btn btn-eliminar" onclick="eliminarVenta(${v.id})">🗑 Eliminar</button>
      </td>
    `;
    tabla.appendChild(fila);
  });
}

// ================================
// Filtros y otros
// ================================
document.addEventListener("DOMContentLoaded", () => {
  mostrarSeccion('usuarios');
});

function filtrarVentas() {
  const termino = document.getElementById("busquedaUsuario").value.trim().toLowerCase();
  if (!termino) return mostrarVentas(todasLasVentas);

  const filtradas = todasLasVentas.filter(v =>
    (v.usuario && (v.usuario.nombre.toLowerCase().includes(termino) || v.usuario.id.toString().includes(termino)))
  );

  if (filtradas.length === 0) alert("No se encontraron ventas para este usuario.");

  mostrarVentas(filtradas);
}

async function confirmarPago(ventaId) {
  const paymentId = prompt("Ingrese el Payment ID (o vacío si no aplica):");
  if (paymentId === null) return;

  try {
    const res = await fetch(`${API_BASE}/ventas/confirmar-pago`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ paymentId: paymentId || "MANUAL", ventaId })
    });

    const data = await res.json();
    alert(data.message || "Pago confirmado.");
    cargarVentas();
  } catch (error) {
    alert("Error al confirmar pago.");
    console.error(error);
  }
}

function verRecibo(id) {
  window.open(`${API_BASE}/ventas/${id}/recibo`, "_blank");
}

async function eliminarVenta(id) {
  if (!confirm("¿Seguro que deseas eliminar esta venta?")) return;
  try {
    const res = await fetch(`${API_BASE}/ventas/${id}`, { method: "DELETE" });
    const data = await res.json().catch(() => ({}));
    alert(data.message || data.error || "Venta eliminada.");
    cargarVentas();
  } catch (error) {
    alert("Error al eliminar venta.");
    console.error(error);
  }
}



  



