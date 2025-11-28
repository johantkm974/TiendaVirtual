let todasLasVentas = [];

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
      const esAdmin = 
    u.rol?.nombre?.trim().toLowerCase() === "administrador" ||
    u.rol?.nombre?.trim().toLowerCase() === "admin";


      const fila = document.createElement("tr");

      fila.innerHTML = `
        <td>${u.id}</td>
        <td>${u.nombre}</td>
        <td>${u.correo}</td>
        <td>${esAdmin ? "Administrador" : "Cliente"}</td>
        <td>
          <button class="btn btn-editar"
            onclick="abrirModal(${u.id}, '${u.nombre}', '${u.correo}', ${esAdmin})"
            ${esAdmin ? 'disabled style="opacity:0.5;cursor:not-allowed;"' : ''}>
            ✏ Editar
          </button>

          ${esAdmin ? "" : `
            <button class="btn btn-eliminar" onclick="eliminarUsuario(${u.id})">
              🗑 Eliminar
            </button>`}
        </td>
      `;

      tabla.appendChild(fila);
    });
  } catch (error) {
    console.error("Error al cargar usuarios:", error);
  }
}

/* =======================================
   ABRIR / CERRAR MODAL
======================================= */
function abrirModal(id, nombre, correo, esAdmin) {
  if (esAdmin) {
    Swal.fire("Prohibido", "No puedes editar al Administrador", "error");
    return;
  }

  document.getElementById("editId").value = id;
  document.getElementById("editNombre").value = nombre;
  document.getElementById("editCorreo").value = correo;

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

    Swal.fire("Éxito", "Usuario actualizado", "success");
    cerrarModal();
    cargarUsuarios();
  } catch (error) {
    Swal.fire("Error", "Hubo un problema al actualizar", "error");
  }
}

/* =======================================
   ELIMINAR USUARIO
======================================= */
async function eliminarUsuario(id) {
  if (!confirm("¿Eliminar usuario?")) return;

  try {
    const res = await fetch(`${API_BASE}/usuarios/${id}`, { method: "DELETE" });
    const msg = await res.text();
    alert(msg);
    cargarUsuarios();
  } catch (error) {
    alert("Error al eliminar usuario.");
  }
}

/* =======================================
   VENTAS
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
          ? `<button class="btn btn-ver" onclick="verRecibo(${v.id})">📄 PDF</button>`
          : `<button class="btn btn-confirmar" onclick="confirmarPago(${v.id})">Confirmar</button>`
        }
      </td>
    `;
    tabla.appendChild(fila);
  });
}

/* ======================================= */
document.addEventListener("DOMContentLoaded", () => {
  mostrarSeccion("usuarios");
});




  





