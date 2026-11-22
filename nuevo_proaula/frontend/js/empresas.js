const API_EMPRESAS = API_BASE + "/api/empresas";
const API_NOTIFICACIONES = API_BASE + "/api/notificaciones";

let empresasTodas = [];
let empresasFiltradas = [];
let empresaModo = "nuevo";
let empresaIdEditando = "";
let notifInterval = null;

function escEmp(texto) {
    if (texto === null || texto === undefined) return "";
    return String(texto)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

async function cargarEmpresas() {
    try {
        const resp = await fetch(API_EMPRESAS);
        const json = await resp.json();
        empresasTodas = (json && Array.isArray(json.datos)) ? json.datos : [];
        filtrarEmpresas();
    } catch (e) {
        const tbody = document.getElementById("tablaEmpresas");
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="7" class="tabla-vacia">No se pudo conectar con ApiServer.</td></tr>';
        }
    }
}

function filtrarEmpresas() {
    const texto = (document.getElementById("buscarEmpresa")?.value || "").trim().toLowerCase();
    const estado = document.getElementById("filtroEstadoEmpresa")?.value || "todos";

    let lista = empresasTodas.slice();
    if (texto !== "") {
        lista = lista.filter(function (e) {
            return (e.nombre || "").toLowerCase().includes(texto)
                || (e.nit || "").toLowerCase().includes(texto)
                || (e.idEmpresa || "").toLowerCase().includes(texto)
                || (e.adminNombre || "").toLowerCase().includes(texto);
        });
    }
    if (estado === "activos") {
        lista = lista.filter(function (e) { return e.activo; });
    } else if (estado === "inactivos") {
        lista = lista.filter(function (e) { return !e.activo; });
    }

    empresasFiltradas = lista;
    pintarTablaEmpresas();
}

function pintarTablaEmpresas() {
    const tbody = document.getElementById("tablaEmpresas");
    if (!tbody) return;

    const totalEl = document.getElementById("empChipEmpresasTotal");
    const actEl = document.getElementById("empChipEmpresasActivas");
    const inacEl = document.getElementById("empChipEmpresasInactivas");
    const contEl = document.getElementById("totalEmpresasMostradas");

    const activas = empresasTodas.filter(e => e.activo).length;
    if (totalEl) totalEl.textContent = empresasTodas.length;
    if (actEl) actEl.textContent = activas;
    if (inacEl) inacEl.textContent = empresasTodas.length - activas;
    if (contEl) contEl.textContent = empresasFiltradas.length;

    if (empresasFiltradas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="tabla-vacia">No se encontraron empresas clientes.</td></tr>';
        return;
    }

    tbody.innerHTML = empresasFiltradas.map(function (e) {
        const precio = Number(e.precioMensual || 0).toLocaleString("es-CO");
        return `
        <tr class="${e.activo ? "fila-activo" : "fila-inactivo"}">
            <td><span class="cli-cedula">${escEmp(e.idEmpresa)}</span><br><small style="color:#64748b;">NIT: ${escEmp(e.nit)}</small></td>
            <td>
                <div class="cli-persona">
                    <span class="cli-avatar">${escEmp((e.nombre || "E").charAt(0).toUpperCase())}</span>
                    <div>
                        <span class="cli-nombre">${escEmp(e.nombre)}</span>
                        <span class="cli-telefono">📧 ${escEmp(e.correo)} · 📞 ${escEmp(e.telefono)}</span>
                    </div>
                </div>
            </td>
            <td>
                <strong>${escEmp(e.adminNombre)}</strong><br>
                <small style="color:#64748b;">ID: ${escEmp(e.adminIdentificacion || "-")}</small>
            </td>
            <td>
                <span class="cli-plan">${escEmp(e.planPlataforma)}</span><br>
                <small style="color:#16a34a; font-weight:700;">$${precio} / mes</small>
            </td>
            <td>
                <small style="color:#334155;">👥 ${Number(e.totalClientes || 0)} clientes · 👔 ${Number(e.totalEmpleados || 0)} empleados</small>
            </td>
            <td>
                <span class="badge ${e.activo ? "badge-activo" : "badge-inactivo"}">
                    <span class="cli-punto"></span> ${e.activo ? "ACTIVA" : "SUSPENDIDA"}
                </span>
            </td>
            <td>
                <button class="btn-icono" onclick="abrirModalEditarEmpresa('${escEmp(e.idEmpresa)}')" title="Editar empresa">✏️</button>
                <button class="btn-icono peligro" onclick="cambiarEstadoEmpresa('${escEmp(e.idEmpresa)}')" title="${e.activo ? "Desactivar empresa (bloquear por pago)" : "Activar empresa"}">
                    ${e.activo ? "⏻" : "▶"}
                </button>
                <button class="btn-icono peligro" onclick="eliminarEmpresa('${escEmp(e.idEmpresa)}')" title="Eliminar empresa">🗑️</button>
            </td>
        </tr>`;
    }).join("");
}

function abrirModalNuevaEmpresa() {
    empresaModo = "nuevo";
    empresaIdEditando = "";
    document.getElementById("modalEmpresaTitulo").textContent = "🏢 Registrar Nueva Empresa Cliente";
    document.getElementById("empCliNombre").value = "";
    document.getElementById("empCliNit").value = "";
    document.getElementById("empCliCorreo").value = "";
    document.getElementById("empCliTelefono").value = "";
    document.getElementById("empCliDireccion").value = "";
    document.getElementById("empCliPlan").value = "Plan SaaS Empresarial";
    document.getElementById("empCliPrecio").value = "250000";
    document.getElementById("empCliEstado").value = "Activo";

    const bloqueAdmin = document.getElementById("bloqueAdminNuevaEmpresa");
    if (bloqueAdmin) bloqueAdmin.style.display = "block";
    document.getElementById("empAdminId").value = "";
    document.getElementById("empAdminNombre").value = "";
    document.getElementById("empAdminApellido").value = "";
    document.getElementById("empAdminCorreo").value = "";
    document.getElementById("empAdminTelefono").value = "";
    document.getElementById("empAdminClave").value = "";

    document.getElementById("modalEmpresa").style.display = "flex";
}

function abrirModalEditarEmpresa(idEmpresa) {
    const emp = empresasTodas.find(x => x.idEmpresa === idEmpresa);
    if (!emp) return;
    empresaModo = "editar";
    empresaIdEditando = emp.idEmpresa;
    document.getElementById("modalEmpresaTitulo").textContent = "✏️ Editar Empresa: " + emp.nombre;
    document.getElementById("empCliNombre").value = emp.nombre || "";
    document.getElementById("empCliNit").value = emp.nit || "";
    document.getElementById("empCliCorreo").value = emp.correo || "";
    document.getElementById("empCliTelefono").value = emp.telefono || "";
    document.getElementById("empCliDireccion").value = emp.direccion || "";
    document.getElementById("empCliPlan").value = emp.planPlataforma || "Plan SaaS Empresarial";
    document.getElementById("empCliPrecio").value = String(emp.precioMensual || 250000);
    document.getElementById("empCliEstado").value = emp.activo ? "Activo" : "Inactivo";

    const bloqueAdmin = document.getElementById("bloqueAdminNuevaEmpresa");
    if (bloqueAdmin) bloqueAdmin.style.display = "none";

    document.getElementById("modalEmpresa").style.display = "flex";
}

function cerrarModalEmpresa() {
    if (document.activeElement && document.activeElement.blur) document.activeElement.blur();
    document.getElementById("modalEmpresa").style.display = "none";
}

async function guardarEmpresa() {
    const val = (id) => (document.getElementById(id)?.value || "").trim();
    const payload = {
        nombre: val("empCliNombre"),
        nit: val("empCliNit"),
        correo: val("empCliCorreo"),
        telefono: val("empCliTelefono"),
        direccion: val("empCliDireccion") || "Sede Principal",
        planPlataforma: val("empCliPlan"),
        precioMensual: val("empCliPrecio") || "250000",
        activo: val("empCliEstado") === "Activo" ? "true" : "false"
    };

    if (!payload.nombre || !payload.nit || !payload.correo || !payload.telefono) {
        notificar("Completa nombre, NIT, correo y teléfono de la empresa.", "error");
        return;
    }

    if (empresaModo === "nuevo") {
        payload.adminIdentificacion = val("empAdminId");
        payload.adminNombre = val("empAdminNombre");
        payload.adminApellido = val("empAdminApellido");
        payload.adminCorreo = val("empAdminCorreo");
        payload.adminTelefono = val("empAdminTelefono");
        payload.adminContrasena = document.getElementById("empAdminClave")?.value || "";

        if (!payload.adminIdentificacion || !payload.adminNombre || !payload.adminApellido || !payload.adminCorreo || !payload.adminContrasena) {
            notificar("Completa los datos y contraseña del Administrador asignado a la empresa.", "error");
            return;
        }
    }

    const url = empresaModo === "nuevo" ? API_EMPRESAS : API_EMPRESAS + "/" + encodeURIComponent(empresaIdEditando);
    const metodo = empresaModo === "nuevo" ? "POST" : "PUT";

    try {
        const resp = await fetch(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const json = await resp.json();
        if (!json.exito) {
            notificar(json.mensaje || "No se pudo guardar la empresa.", "error");
            return;
        }
        cerrarModalEmpresa();
        notificar(json.mensaje || "Empresa guardada correctamente.", "exito");
        cargarEmpresas();
    } catch (e) {
        notificar("Error de conexión con ApiServer.", "error");
    }
}

async function cambiarEstadoEmpresa(idEmpresa) {
    const emp = empresasTodas.find(x => x.idEmpresa === idEmpresa);
    if (!emp) return;
    const nuevoEstado = !emp.activo;
    const msg = nuevoEstado
        ? 'Se reactivará la empresa "' + emp.nombre + '" y su administrador podrá volver a ingresar.'
        : 'Al desactivar "' + emp.nombre + '", su administrador será bloqueado y redirigido al aviso de pago pendiente del sistema.';

    const ok = await confirmarAccion(
        nuevoEstado ? "Activar empresa" : "Desactivar empresa",
        msg,
        nuevoEstado ? "Sí, activar" : "Sí, desactivar"
    );
    if (!ok) return;

    try {
        const resp = await fetch(API_EMPRESAS + "/" + encodeURIComponent(idEmpresa) + "/estado", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                activo: String(nuevoEstado),
                motivoSuspension: "Pago de suscripción del sistema pendiente."
            })
        });
        const json = await resp.json();
        if (!json.exito) {
            notificar(json.mensaje || "No se pudo cambiar el estado.", "error");
            return;
        }
        notificar(json.mensaje, nuevoEstado ? "exito" : "info");
        cargarEmpresas();
    } catch (e) {
        notificar("No se pudo conectar con ApiServer.", "error");
    }
}

async function eliminarEmpresa(idEmpresa) {
    const emp = empresasTodas.find(x => x.idEmpresa === idEmpresa);
    if (!emp) return;
    const ok = await confirmarAccion(
        "Eliminar empresa",
        'Se eliminará a "' + emp.nombre + '" (' + emp.idEmpresa + ').',
        "Sí, eliminar"
    );
    if (!ok) return;

    try {
        const resp = await fetch(API_EMPRESAS + "/" + encodeURIComponent(idEmpresa), { method: "DELETE" });
        const json = await resp.json();
        if (!json.exito) {
            notificar(json.mensaje || "No se pudo eliminar la empresa.", "error");
            return;
        }
        notificar(json.mensaje, "exito");
        cargarEmpresas();
    } catch (e) {
        notificar("No se pudo conectar con ApiServer.", "error");
    }
}

async function cargarNotificacionesWeb() {
    const sesion = localStorage.getItem("sesionActiva");
    if (!sesion) return;

    try {
        const resp = await fetch(API_NOTIFICACIONES);
        if (!resp.ok) return;
        const json = await resp.json();
        const lista = (json && Array.isArray(json.datos)) ? json.datos : [];
        const noLeidas = Number(json.noLeidas || 0);

        const badge = document.getElementById("notifBadge");
        if (badge) {
            badge.textContent = noLeidas > 9 ? "9+" : String(noLeidas);
            badge.style.display = noLeidas > 0 ? "inline-flex" : "none";
        }

        const listaEl = document.getElementById("notifLista");
        if (!listaEl) return;

        if (lista.length === 0) {
            listaEl.innerHTML = '<div class="notif-vacia">No tienes notificaciones por ahora.</div>';
            return;
        }

        listaEl.innerHTML = lista.map(function (n) {
            return `
            <div class="notif-item ${n.leida ? "leida" : "no-leida"}" onclick="abrirNotificacion(${n.idNotificacion}, '${escEmp(n.tipo)}')">
                <div class="notif-item-titulo">
                    <span>${n.leida ? "📩" : "🔔"} ${escEmp(n.titulo)}</span>
                    <small>${escEmp(n.fecha)}</small>
                </div>
                <p class="notif-item-msg">${escEmp(n.mensaje)}</p>
            </div>`;
        }).join("");
    } catch (e) {

    }
}

function alternarPanelNotificaciones(evento) {
    if (evento) evento.stopPropagation();
    const panel = document.getElementById("notifDropdown");
    if (!panel) return;
    const abierto = panel.style.display === "block";
    panel.style.display = abierto ? "none" : "block";
    if (!abierto) {
        cargarNotificacionesWeb();
    }
}

async function abrirNotificacion(idNotificacion, tipo) {
    try {
        await fetch(API_NOTIFICACIONES + "/leer", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ idNotificacion: String(idNotificacion) })
        });
    } catch (e) {}

    const panel = document.getElementById("notifDropdown");
    if (panel) panel.style.display = "none";
    await cargarNotificacionesWeb();

    const sesion = JSON.parse(localStorage.getItem("sesionActiva") || "null");
    if (sesion && sesion.rol === "ADMIN" && tipo === "TICKET_CERRADO") {
        irAlModulo("soporte-plataforma");
    } else {
        irAlModulo("soporte");
    }
}

async function marcarTodasNotificacionesLeidas() {
    try {
        await fetch(API_NOTIFICACIONES + "/leer-todas", { method: "PUT" });
        await cargarNotificacionesWeb();
    } catch (e) {}
}

document.addEventListener("click", function (e) {
    const cont = document.getElementById("notifContenedor");
    const panel = document.getElementById("notifDropdown");
    if (panel && cont && !cont.contains(e.target)) {
        panel.style.display = "none";
    }
});

document.addEventListener("DOMContentLoaded", function () {
    cargarNotificacionesWeb();
    if (notifInterval) clearInterval(notifInterval);
    notifInterval = setInterval(cargarNotificacionesWeb, 15000);
});
