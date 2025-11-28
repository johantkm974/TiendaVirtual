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
   CARGAR USUARIOS (LÓGICA ORIGINAL DEL ADMIN)
======================================= */
async function cargarUsuarios() {
  const tabla = document.querySelector("#tablaUsuarios tbody");
  tabla.innerHTML = "";

  try {
    const res = await fetch(`${API_BASE}/usuarios`);
    const usuarios = await res.json();

    usuarios.forEach(u => {

      // 👉 MISMA LÓGICA QUE USABAS EN EL BOTÓN ELIMINAR (FUNCIONA PERFECTO)
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
   MODAL → ABRIR / CERRAR
======================================= */
function abrirModal(id, nombre, correo) {
  document.getElementById("editId").value = id;
  document.getElementById("editNombre").value = nombre;
  document.getElementById("editCorreo").value = correo;

  // Contraseña limpia y visible correctamente
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
   GUARDAR CAMBIOS DE USUARIO (PUT)
======================================= */
async function guardarCambiosUsuario() {
  const id = document.getElementById("editId").value;
  const nombre = document.getElementById("editNombre").value;
  const correo = document.getElementById("editCorreo").value;
  const contrasena = document.getElementById("editContrasena").value;

  // Solo se envían datos modificados
  const data = { nombre, correo };

  // Contraseña solo si fue modificada
  if (contrasena.trim() !== "") {
    data.contrasena = contrasena;
  }

  try {
    const res = await fetch(`${API_BASE}/usuarios/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data)
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
  }
}

/* =======================================
   ELIMINAR USUARIO
======================================= */
async function eliminarUsuario(id) {
  if (!confirm("¿Seguro que deseas eliminar este usuario?")) return;

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
        ${
          v.estadoPago === "APROBADO"
            ? `<button class="btn btn-ver" onclick="verRecibo(${v.id})">📄 PDF</button>`
            : `<button class="btn btn-confirmar" onclick="confirmarPago(${v.id})">Confirmar</button>`
        }
        <button class="btn btn-eliminar" onclick="eliminarVenta(${v.id})">🗑</button>
      </td>
    `;
    tabla.appendChild(fila);
  });
}

/* =======================================
   EVENTO AL INICIAR
======================================= */
document.addEventListener("DOMContentLoaded", () => {
  mostrarSeccion("usuarios");
});









