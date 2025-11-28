let todasLasVentas = [];

// Base API
const API_BASE = "/api";

/* =======================================
   CAMBIAR SECCIÓN
======================================= */
function mostrarSeccion(id) {
  document.querySelectorAll(".seccion").forEach(s => s.classList.remove("visible"));
  document.getElementById(id).classList.add("visible");

  if (id === "usuarios") cargarUsuarios();
  if (id === "ventas") cargarVentas();
}

/* =======================================
   CARGAR USUARIOS
======================================= */
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
          ${
            esAdmin
              ? "" 
              : `
                <button class="btn btn-editar" onclick="abrirModal(${u.id}, '${u.nombre}', '${u.correo}')">
                  ✏ Editar
                </button>

                <button class="btn btn-eliminar" onclick="eliminarUsuario(${u.id})">
                  🗑 Eliminar
                </button>
              `
          }
        </td>
      `;

      tabla.appendChild(fila);
    });

  } catch (error) {
    console.error("Error al cargar usuarios:", error);
  }
}

/* =======================================
   MODAL EDITAR USUARIO
======================================= */
function abrirModal(id, nombre, correo) {
  document.getElementById("editId").value = id;
  document.getElementById("editNombre").value = nombre;
  document.getElementById("editCorreo").value = correo;

  const pass = document.getElementById("editContrasena");
  pass.value = "";
  pass.style.background = "white";
  pass.style.color = "black";

  document.getElementById("modalEditar").style.display = "flex";
}

function cerrarModal() {
  document.getElementById("modalEditar").style.display = "none";
}

/* =======================================
   GUARDAR CAMBIOS
======================================= */
async function guardarCambiosUsuario() {
  const id = document.getElementById("editId").value;
  const nombre = document.getElementById("editNombre").value;
  const correo = document.getElementById("editCorreo").value;
  const contrasena = document.getElementById("editContrasena").value;

  const data = { nombre, correo };
  if (contrasena.trim() !== "") data.contrasena = contrasena;

  try {
    const res = await fetch(`${API_BASE}/usuarios/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
    });

    if (!res.ok) {
      Swal.fire("Error", await res.text(), "error");
      return;
    }

    Swal.fire("Éxito", "Usuario actualizado correctamente", "success");
    cerrarModal();
    cargarUsuarios();

  } catch (error) {
    Swal.fire("Error", "No se pudo actualizar el usuario", "error");
  }
}

/* =======================================
   ELIMINAR USUARIO
======================================= */
async function eliminarUsuario(id) {
  const confirmar = await Swal.fire({
    title: "¿Eliminar usuario?",
    text: "Esta acción no se puede deshacer",
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "Sí, eliminar",
    cancelButtonText: "Cancelar"
  });

  if (!confirmar.isConfirmed) return;

  try {
    const res = await fetch(`${API_BASE}/usuarios/${id}`, { method: "DELETE" });

    if (!res.ok) {
      Swal.fire("Error", await res.text(), "error");
      return;
    }

    Swal.fire({
      icon: "success",
      title: "Usuario eliminado",
      timer: 1500,
      showConfirmButton: false
    });

    cargarUsuarios();

  } catch (error) {
    Swal.fire("Error", "No se pudo eliminar el usuario", "error");
  }
}

/* =======================================
   CARGAR VENTAS
======================================= */
async function cargarVentas() {
  const tabla = document.querySelector("#tablaVentas tbody");
  tabla.innerHTML = "";

  try {
    const res = await fetch(`${API_BASE}/ventas`);
    todasLasVentas = await res.json();
    mostrarVentas(todasLasVentas);
  } catch (error) {
    console.error("Error cargando ventas:", error);
  }
}

/* =======================================
   MOSTRAR VENTAS EN TABLA
======================================= */
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
      <td>${v.estadoPago || "PENDIENTE"}</td>
      <td>
        ${
          v.estadoPago === "APROBADO"
            ? `<button class="btn btn-ver" onclick="verRecibo(${v.id})">📄 PDF</button>`
            : `<button class="btn btn-confirmar" onclick="confirmarPago(${v.id})">Confirmar</button>`
        }
        <button class="btn btn-eliminar" onclick="eliminarVenta(${v.id})">🗑 Eliminar</button>
      </td>
    `;

    tabla.appendChild(fila);
  });
}

/* =======================================
   CONFIRMAR PAGO
======================================= */
async function confirmarPago(ventaId) {
  const paymentId = prompt("Ingrese Payment ID (o vacío si es manual):");
  if (paymentId === null) return;

  try {
    const res = await fetch(`${API_BASE}/ventas/confirmar-pago`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        ventaId: ventaId,
        paymentId: paymentId || "MANUAL"
      })
    });

    const data = await res.json();
    alert(data.message || "Pago confirmado.");
    cargarVentas();

  } catch (error) {
    alert("Error confirmando pago");
    console.error(error);
  }
}

/* =======================================
   ELIMINAR VENTA
======================================= */
async function eliminarVenta(id) {
  if (!confirm("¿Seguro de eliminar esta venta?")) return;

  try {
    const res = await fetch(`${API_BASE}/ventas/${id}`, { method: "DELETE" });
    const data = await res.json().catch(() => ({}));

    alert(data.message || data.error || "Venta eliminada.");
    cargarVentas();

  } catch (error) {
    console.error("Error al eliminar venta:", error);
    alert("No se pudo eliminar la venta.");
  }
}

/* =======================================
   ABRIR PDF DEL RECIBO
======================================= */
function verRecibo(id) {
  window.open(`${API_BASE}/ventas/${id}/recibo`, "_blank");
}

/* =======================================
   INICIAR
======================================= */
document.addEventListener("DOMContentLoaded", () => {
  mostrarSeccion("usuarios");
});






