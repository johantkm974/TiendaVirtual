let todasLasVentas = [];

// ✅ Base para las APIs (sin dominio)
const API_BASE = "/api";

function mostrarSeccion(id) {
  document.querySelectorAll(".seccion").forEach(s => s.classList.remove("visible"));
  document.getElementById(id).classList.add("visible");
  if (id === "usuarios") cargarUsuarios();
  if (id === "ventas") cargarVentas();
}

async function cargarUsuarios() {
  const tabla = document.querySelector("#tablaUsuarios tbody");
  tabla.innerHTML = "";
  try {
    const res = await fetch(`${API_BASE}/usuarios`);
    const usuarios = await res.json();

    usuarios.forEach(u => {
      const fila = document.createElement("tr");
      fila.innerHTML = `
        <td>${u.id}</td>
        <td>${u.nombre}</td>
        <td>${u.correo}</td>
        <td>${u.rol && (u.rol.id === 1 || u.rol.nombre?.toUpperCase() === "ADMIN") ? "Administrador" : "Cliente"}</td>
        <td>
          ${u.rol && (u.rol.id === 1 || u.rol.nombre?.toUpperCase() === "ADMINISTRADOR")
            ? "" 
            : `<button class="btn btn-eliminar" onclick="eliminarUsuario(${u.id})">🗑 Eliminar</button>`}
        </td>
      `;
      tabla.appendChild(fila);
    });
  } catch (error) {
    const msg = document.getElementById("mensaje");
    if (msg) msg.textContent = "Error al cargar usuarios.";
    console.error(error);
  }
}

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

async function cargarVentas() {
  const tabla = document.querySelector("#tablaVentas tbody");
  tabla.innerHTML = "";
  try {
    const res = await fetch(`${API_BASE}/ventas`);
    todasLasVentas = await res.json();
    mostrarVentas(todasLasVentas);
  } catch (error) {
    const msg = document.getElementById("mensaje");
    if (msg) msg.textContent = "Error al cargar ventas.";
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

// ===== Cargar Usuarios al entrar automáticamente =====
document.addEventListener("DOMContentLoaded", () => {
  mostrarSeccion('usuarios'); // fuerza que cargue Usuarios al inicio
});

function filtrarVentas() {
  const termino = document.getElementById("busquedaUsuario").value.trim().toLowerCase();
  if (!termino) {
    mostrarVentas(todasLasVentas);
    return;
  }

  const filtradas = todasLasVentas.filter(v =>
    (v.usuario && (v.usuario.nombre.toLowerCase().includes(termino) || v.usuario.id.toString().includes(termino)))
  );

  if (filtradas.length === 0) {
    alert("No se encontraron ventas para este usuario.");
  }

  mostrarVentas(filtradas);
}

async function confirmarPago(ventaId) {
  const paymentId = prompt("Ingrese el Payment ID (o deje vacío si no aplica):");
  if (paymentId === null) return;
  try {
    const res = await fetch(`${API_BASE}/ventas/confirmar-pago`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ paymentId: paymentId || "MANUAL", ventaId })
    });
    const data = await res.json();
    alert(data.message || "Pago confirmado correctamente.");
    cargarVentas();
  } catch (error) {
    alert("Error al confirmar pago.");
    console.error(error);
  }
}

function verRecibo(id) {
  // Abre el PDF generado por el backend
  window.open(`${API_BASE}/ventas/${id}/recibo`, "_blank");
}

async function eliminarVenta(id) {
  if (!confirm("¿Seguro que deseas eliminar esta venta?")) return;
  try {
    const res = await fetch(`${API_BASE}/ventas/${id}`, { method: "DELETE" });
    const data = await res.json().catch(() => ({}));
    alert(data.message || data.error || "Venta eliminada correctamente.");
    cargarVentas();
  } catch (error) {
    alert("Error al eliminar venta.");
    console.error(error);
  }
}


  



  

