const CLAVE_SESION = "sesionActiva";

const planesIniciales = [
    {
        codigo: 101,
        nombre: "Plan Fibra 100M",
        velocidad: "100 Mbps",
        precio: 65000,
        descripcion: "Internet fibra óptica residencial para hogares de 1 a 2 personas.",
        activo: true
    },
    {
        codigo: 102,
        nombre: "Plan Fibra 200M",
        velocidad: "200 Mbps",
        precio: 89900,
        descripcion: "Ideal para teletrabajo con varias videollamadas simultáneas.",
        activo: true
    },
    {
        codigo: 103,
        nombre: "Plan Fibra Gamer Ultra 350M",
        velocidad: "350 Mbps",
        precio: 129900,
        descripcion: "Baja latencia para juegos en línea y streaming en 4K.",
        activo: true
    },
    {
        codigo: 104,
        nombre: "Plan Fibra Empresarial 500M",
        velocidad: "500 Mbps",
        precio: 189900,
        descripcion: "Conexión dedicada para pequeñas y medianas empresas.",
        activo: false
    }
];

const API_PLANES = API_BASE + "/api/planes";
const API_CLIENTES_PLAN = API_BASE + "/api/clientes";

let cachePlanes = [];

async function inicializarPlanes() {
    try {

        const control = new AbortController();
        const limite = setTimeout(() => control.abort(), 5000);

        const respuesta = await fetch(API_PLANES, { signal: control.signal });
        clearTimeout(limite);

        const json = await respuesta.json();
        cachePlanes = json.datos || [];
    } catch (error) {
        cachePlanes = [];
        notificarPlanes("No se pudo conectar con el servidor Java (ApiServer).", "error");
    }
}

function obtenerPlanes() {
    return cachePlanes;
}

async function guardarPlanes(listaPlanes) {
    cachePlanes = listaPlanes;
    if (typeof planesActivosCacheHora !== "undefined") {
        planesActivosCacheHora = 0;
    }

    try {
        const respuesta = await fetch(API_PLANES + "/lote", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ planes: listaPlanes })
        });
        const json = await respuesta.json().catch(() => ({}));

        if (!respuesta.ok || !json.exito) {
            notificarPlanes(json.mensaje || "No se pudieron guardar los cambios en el servidor.", "error");
        }

        await inicializarPlanes();
    } catch (error) {
        notificarPlanes("No se pudo guardar en el servidor Java.", "error");
    }
}

function formatearPrecio(valor) {
    return new Intl.NumberFormat("es-CO", {
        style: "currency",
        currency: "COP",
        maximumFractionDigits: 0
    }).format(Number(valor) || 0);
}

function obtenerSesion() {
    try {
        return JSON.parse(localStorage.getItem(CLAVE_SESION));
    } catch (error) {
        return null;
    }
}

function puedeOperarPlanes(sesion) {
    return !!sesion && (sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN" || sesion.rol === "EMPLEADO");
}

function esAdministrador(sesion) {
    return !!sesion && (sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN");
}

function clienteSinPlan(sesion) {
    if (!sesion || sesion.rol !== "CLIENTE") return false;
    const plan = (sesion.planActual || "").trim();
    return plan === "" || plan.toLowerCase() === "sin plan";
}

const ICONOS_TOAST = { ok: "✓", error: "!", aviso: "i" };

function obtenerContenedorNotificaciones() {
    let contenedor = document.getElementById("contenedorNotificaciones");
    if (!contenedor) {
        contenedor = document.createElement("div");
        contenedor.id = "contenedorNotificaciones";
        contenedor.className = "toast-container";
        contenedor.setAttribute("role", "status");
        contenedor.setAttribute("aria-live", "polite");
        document.body.appendChild(contenedor);
    }
    return contenedor;
}

function notificarPlanes(mensaje, tipo) {
    const contenedor = obtenerContenedorNotificaciones();
    const claseTipo = tipo === "error" ? "toast-error" : (tipo === "aviso" ? "toast-aviso" : "toast-exito");

    const toast = document.createElement("div");
    toast.className = "toast " + claseTipo;
    toast.innerHTML = `
        <span class="toast-icono">${ICONOS_TOAST[tipo] || ICONOS_TOAST.ok}</span>
        <div class="toast-cuerpo">
            <p class="toast-mensaje">${mensaje}</p>
        </div>
        <button type="button" class="toast-cerrar" aria-label="Cerrar notificación">×</button>
        <span class="toast-progreso"></span>
    `;

    const cerrar = () => {
        toast.classList.remove("toast-visible");
        toast.classList.add("toast-saliendo");
        setTimeout(() => toast.remove(), 260);
    };

    toast.querySelector(".toast-cerrar").addEventListener("click", cerrar);
    contenedor.appendChild(toast);

    requestAnimationFrame(() => toast.classList.add("toast-visible"));

    setTimeout(cerrar, 4000);
}

function validarPlan(datos, planes, codigoOriginal) {
    if (!Number.isInteger(datos.codigo) || datos.codigo <= 0) {
        return "El código del plan debe ser un número entero mayor a cero.";
    }
    if (!datos.nombre) {
        return "El nombre del plan es obligatorio.";
    }
    if (!datos.velocidad) {
        return "La velocidad del plan es obligatoria (ej: 100 Mbps).";
    }
    if (!(datos.precio >= 0)) {
        return "El precio del plan no puede ser negativo.";
    }

    const otroConMismoCodigo = planes.find(p => p.codigo === datos.codigo && p.codigo !== codigoOriginal);
    if (otroConMismoCodigo) {
        return "Ya existe un plan registrado con el código: " + datos.codigo;
    }

    const otroConMismoNombre = planes.find(p =>
        p.nombre.toLowerCase() === datos.nombre.toLowerCase() && p.codigo !== codigoOriginal
    );
    if (otroConMismoNombre) {
        return "Ya existe un plan registrado con el nombre: " + datos.nombre;
    }

    return null;
}

function planesFiltrados() {
    const planes = obtenerPlanes();

    const caja = document.getElementById("buscarPlan");
    const filtro = document.getElementById("filtroEstadoPlan");
    const texto = caja ? caja.value.trim().toLowerCase() : "";
    const estado = filtro ? filtro.value : "todos";

    return planes.filter(plan => {
        const nombre = (plan.nombre || "").toLowerCase();
        const velocidad = (plan.velocidad || "").toLowerCase();
        const coincideTexto = texto === ""
            || String(plan.codigo).indexOf(texto) !== -1
            || nombre.indexOf(texto) !== -1
            || velocidad.indexOf(texto) !== -1;

        const coincideEstado = estado === "todos"
            || (estado === "activos" && plan.activo)
            || (estado === "inactivos" && !plan.activo);

        return coincideTexto && coincideEstado;
    });
}

function filtrarPlanes() {
    renderizarTablaPlanes();
}

function limpiarFiltrosPlanes() {
    const caja = document.getElementById("buscarPlan");
    if (caja) caja.value = "";

    const filtro = document.getElementById("filtroEstadoPlan");
    if (filtro) filtro.value = "todos";
}

async function recargarPlanes(boton) {
    if (boton) boton.classList.add("girando");

    await cargarPlanes();

    if (boton) boton.classList.remove("girando");
    notificarPlanes("Catálogo de planes actualizado.", "info");
}

function renderizarTablaPlanes() {
    const cuerpo = document.getElementById("tablaPlanesBody");
    if (!cuerpo) return;

    const sesion = obtenerSesion();
    const admin = esAdministrador(sesion);
    const opera = puedeOperarPlanes(sesion);
    const planes = obtenerPlanes();
    const visibles = planesFiltrados();

    const resumen = document.getElementById("resumenPlanes");
    if (resumen) {
        const total = planes.length;
        const activos = planes.filter(p => p.activo).length;
        const inactivos = total - activos;
        resumen.innerHTML = `
            <span class="plan-chip plan-chip-total">📡 Total: <strong>${total}</strong></span>
            <span class="plan-chip plan-chip-activos">✅ Activos: <strong>${activos}</strong></span>
            <span class="plan-chip plan-chip-inactivos">⛔ Inactivos: <strong>${inactivos}</strong></span>`;
    }

    const encabezadoAcciones = document.getElementById("thAccionesPlanes");
    if (encabezadoAcciones) {
        encabezadoAcciones.style.display = opera ? "" : "none";
    }

    const mostrando = document.getElementById("planMostrando");
    if (mostrando) {
        mostrando.textContent = visibles.length;
    }

    if (planes.length === 0) {
        cuerpo.innerHTML = `
            <tr>
                <td colspan="6" style="text-align:center; padding: 28px; color: #64748b;">
                    No hay planes registrados todavía.
                </td>
            </tr>`;
        aplicarPermisos(sesion);
        return;
    }

    if (visibles.length === 0) {
        cuerpo.innerHTML = `
            <tr>
                <td colspan="6" style="text-align:center; padding: 28px; color: #64748b;">
                    Ningún plan coincide con la búsqueda o el filtro aplicado.
                </td>
            </tr>`;
        aplicarPermisos(sesion);
        return;
    }

    cuerpo.innerHTML = visibles.map(plan => {

        let acciones = "";

        if (admin) {

            acciones = `
            <td class="plan-acciones">
                <button class="btn-accion btn-editar" title="Editar plan"
                        onclick="event.stopPropagation(); editarPlan(${plan.codigo})">✏️</button>
                <button class="btn-accion ${plan.activo ? "btn-desactivar" : "btn-activar"}"
                        title="${plan.activo ? "Desactivar" : "Activar"} plan"
                        onclick="event.stopPropagation(); alternarEstadoPlan(${plan.codigo})">
                    ${plan.activo ? "⏸️" : "▶️"}
                </button>
                <button class="btn-accion btn-eliminar" title="Eliminar plan"
                        onclick="event.stopPropagation(); eliminarPlanCatalogo(${plan.codigo})">🗑️</button>
            </td>`;
        } else if (opera) {

            acciones = `
            <td class="plan-acciones">
                <button class="btn-accion btn-editar" title="Editar plan"
                        onclick="event.stopPropagation(); editarPlan(${plan.codigo})">✏️</button>
                <button class="btn-accion ${plan.activo ? "btn-desactivar" : "btn-activar"}"
                        title="${plan.activo ? "Desactivar" : "Activar"} plan"
                        onclick="event.stopPropagation(); alternarEstadoPlan(${plan.codigo})">
                    ${plan.activo ? "⏸️" : "▶️"}
                </button>
            </td>`;
        }

        return `
        <tr class="fila-plan ${plan.activo ? "fila-activo" : "fila-inactivo"}"
            onclick="abrirDetallePlan(${plan.codigo})"
            title="Ver detalle del plan ${plan.nombre}">
            <td><span class="plan-codigo">${plan.codigo}</span></td>
            <td>
                <span class="plan-nombre">${plan.nombre}</span>
                <span class="plan-descripcion">${plan.descripcion}</span>
            </td>
            <td class="plan-velocidad">${plan.velocidad}</td>
            <td class="plan-precio"><span class="plan-precio-valor">${formatearPrecio(plan.precio)}</span></td>
            <td class="plan-estado">
                <span class="badge ${plan.activo ? "badge-activo" : "badge-inactivo"}">
                    ${plan.activo ? "ACTIVO" : "INACTIVO"}
                </span>
            </td>
            ${acciones}
        </tr>`;
    }).join("");

    aplicarPermisos(sesion);
}

function aplicarPermisos(sesion) {
    const admin = esAdministrador(sesion);
    const opera = puedeOperarPlanes(sesion);
    const sinPlan = clienteSinPlan(sesion);
    const conPlan = !!sesion && sesion.rol === "CLIENTE" && !sinPlan;

    const barraAdmin = document.getElementById("barraAdminPlanes");
    if (barraAdmin) barraAdmin.style.display = admin ? "flex" : "none";

    const modalFormulario = document.getElementById("modalPlan");
    if (modalFormulario && !opera) modalFormulario.style.display = "none";

    const resumen = document.getElementById("resumenPlanes");
    if (resumen) resumen.style.display = opera ? "flex" : "none";

    const tabla = document.getElementById("tablaPlanesWrapper");
    if (tabla) tabla.style.display = opera ? "block" : "none";

    const barra = document.getElementById("toolbarPlanes");
    if (barra) barra.style.display = opera ? "flex" : "none";

    const panelContratar = document.getElementById("panelContratacion");
    if (panelContratar) {
        panelContratar.style.display = sinPlan ? "block" : "none";
        if (sinPlan) cargarSelectPlanesActivos();
    }

    const panelPlanActual = document.getElementById("panelPlanActual");
    if (panelPlanActual) {
        panelPlanActual.style.display = conPlan ? "block" : "none";
        if (conPlan) renderizarPlanActual(sesion);
    }

    const descripcion = document.getElementById("descripcionPlanes");
    if (descripcion) {
        if (opera) {
            descripcion.innerHTML = 'Planes comerciales de fibra óptica. Un plan <strong>activo</strong> puede ' +
                                    'contratarse; uno <strong>inactivo</strong> queda fuera del catálogo de contratación.';
        } else if (conPlan) {
            descripcion.innerHTML = 'Aquí puedes consultar las condiciones del plan que tienes contratado. ' +
                                    'Si necesitas cambiarlo, escríbenos y lo gestionamos contigo.';
        } else {
            descripcion.innerHTML = 'Bienvenido a WiNet Control. Elige el plan que mejor se ajuste a tu hogar ' +
                                    'y actívalo al instante, sin trámites.';
        }
    }
}

function clienteAlDia(sesion) {
    return !sesion || sesion.pagoAlDia !== false;
}

function renderizarPlanActual(sesion) {
    const contenedor = document.getElementById("detallePlanActual");
    if (!contenedor) return;

    const nombrePlan = sesion.planActual;
    const alDia = clienteAlDia(sesion);

    const plan = obtenerPlanes().find(p =>
        (p.nombre || "").toLowerCase() === (nombrePlan || "").toLowerCase()
    );

    const bloquePago = alDia
        ? '<span class="badge badge-activo">Al día</span>'
        : '<span class="badge" style="background:#fef3c7; color:#b45309;">Pendiente</span>';

    if (!plan) {
        contenedor.innerHTML = `
            <div class="detalle-cabecera">
                <span class="detalle-codigo">Plan contratado</span>
                <h4 id="txtPlanActual">${nombrePlan || "—"}</h4>
                <p class="detalle-velocidad">Consultando datos del plan…</p>
            </div>
            <div class="detalle-cuerpo">
                <div class="detalle-fila">
                    <span class="detalle-etiqueta">Pago</span>
                    <span class="detalle-valor">${bloquePago}</span>
                </div>
                <p class="plan-mi-plan-aviso">
                    Este plan no figura en el catálogo vigente. Consulta con la administración de tu sucursal.
                </p>
            </div>
            ${pieContacto()}`;
        renderizarOtrosPlanes(sesion);
        return;
    }

    contenedor.innerHTML = `
        <div class="detalle-cabecera">
            <span class="detalle-codigo">Código ${plan.codigo}</span>
            <h4 id="txtPlanActual">${plan.nombre}</h4>
            <p class="detalle-velocidad">${plan.velocidad} de velocidad contratada</p>
        </div>

        <div class="detalle-cuerpo">
            <div class="detalle-fila">
                <span class="detalle-etiqueta">Velocidad</span>
                <span class="detalle-valor">${plan.velocidad}</span>
            </div>
            <div class="detalle-fila">
                <span class="detalle-etiqueta">Precio mensual</span>
                <span class="detalle-valor">
                    <span class="plan-precio-valor">${formatearPrecio(plan.precio)}</span>
                </span>
            </div>
            <div class="detalle-fila">
                <span class="detalle-etiqueta">Pago</span>
                <span class="detalle-valor">${bloquePago}</span>
            </div>
            <div class="detalle-fila">
                <span class="detalle-etiqueta">Servicio</span>
                <span class="detalle-valor">${plan.activo ? "Habilitado" : "Suspendido"}</span>
            </div>
        </div>

        <p class="detalle-descripcion">${plan.descripcion || "Sin descripción registrada."}</p>

        ${pieContacto()}`;
    renderizarOtrosPlanes(sesion);
}

function renderizarOtrosPlanes(sesion) {
    const contenedor = document.getElementById("otrosPlanesDisponibles");
    if (!contenedor) return;

    const planActual = (sesion.planActual || "").trim().toLowerCase();
    const otros = obtenerPlanes().filter(function (p) {
        return p.activo && (p.nombre || "").trim().toLowerCase() !== planActual;
    });

    if (otros.length === 0) {
        contenedor.innerHTML = "";
        return;
    }

    const esc = function (t) {
        const d = document.createElement("div");
        d.textContent = t == null ? "" : String(t);
        return d.innerHTML;
    };

    let filas = "";
    otros.forEach(function (p) {
        filas += `
            <div style="display:flex; justify-content:space-between; align-items:center; gap:12px; padding:12px 14px; border:1px solid #e2e8f0; border-radius:10px; background:#fff; flex-wrap:wrap;">
                <div>
                    <strong style="color:#1e293b;">${esc(p.nombre)}</strong>
                    <span style="margin-left:8px; font-size:12px; color:#2563eb; font-weight:600;">${esc(p.velocidad)}</span>
                    <div style="font-size:12.5px; color:#64748b; margin-top:2px;">${esc(p.descripcion || "Plan de fibra óptica")}</div>
                </div>
                <div style="display:flex; align-items:center; gap:12px;">
                    <strong style="color:#16a34a;">${formatearPrecio(p.precio)}/mes</strong>
                    <button type="button" class="btn btn-primary btn-auto" onclick="solicitarCambioPlan(${Number(p.codigo)})">
                        Solicitar cambio
                    </button>
                </div>
            </div>
        `;
    });

    contenedor.innerHTML = `
        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:16px;">
            <h4 style="margin:0 0 6px 0; color:#1e293b;">📡 Otros planes disponibles</h4>
            <p style="margin:0 0 12px 0; font-size:13px; color:#64748b;">
                Si deseas mejorar o cambiar tu servicio, envía una solicitud de cambio de plan y un asesor la atenderá.
            </p>
            <div style="display:flex; flex-direction:column; gap:10px;">
                ${filas}
            </div>
        </div>
    `;
}

async function solicitarCambioPlan(codigoPlan) {
    const sesion = obtenerSesion();
    if (!sesion || sesion.rol !== "CLIENTE") return;

    const plan = obtenerPlanes().find(p => Number(p.codigo) === Number(codigoPlan));
    if (!plan) {
        notificarPlanes("No se encontró el plan seleccionado.", "error");
        return;
    }

    const confirmado = await confirmarAccionPlanes({
        titulo: "Solicitar cambio de plan",
        mensaje: '¿Deseas enviar una solicitud para cambiar de "' + (sesion.planActual || "Sin plan")
                 + '" al plan "' + plan.nombre + '" (' + plan.velocidad + ', ' + formatearPrecio(plan.precio) + '/mes)?',
        textoAceptar: "Enviar solicitud",
        textoCancelar: "Cancelar",
        peligro: false
    });
    if (!confirmado) return;

    const nombreCompleto = ((sesion.nombre || "") + " " + (sesion.apellido || "")).trim() || sesion.identificacion;

    try {
        const respuesta = await fetch(API_BASE + "/api/soporte", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                usuario: nombreCompleto,
                correo: sesion.correo || "",
                categoria: "Cambio de Plan",
                asunto: "Solicitud de cambio a " + plan.nombre,
                mensaje: "Cédula: " + sesion.identificacion
                         + " | Plan actual: " + (sesion.planActual || "Sin plan")
                         + " | Plan solicitado: " + plan.nombre + " (" + plan.velocidad + ")"
            })
        });
        const json = await respuesta.json();
        if (!respuesta.ok || !json.exito) {
            notificarPlanes(json.mensaje || "No se pudo enviar la solicitud.", "error");
            return;
        }
        notificarPlanes('¡Solicitud enviada! Puedes ver su estado en la sección de Soporte.', "ok");
    } catch (error) {
        notificarPlanes("No se pudo conectar con el servidor Java (ApiServer).", "error");
    }
}

function pieContacto() {
    return `
        <div class="plan-contacto">
            <div class="plan-contacto-texto">
                <strong>¿Quieres cambiar de plan?</strong>
                <span>Escríbenos y la administración de tu sucursal lo gestiona contigo.</span>
            </div>
            <button type="button" class="btn btn-primary plan-btn-contacto" onclick="irASoportePlanes()">
                Contactar a mi sucursal
            </button>
        </div>`;
}

function irASoportePlanes() {
    const enlaceSoporte = document.querySelector('a[href="#soporte"]');
    if (enlaceSoporte) {
        enlaceSoporte.click();
        const seccion = document.getElementById("seccion-soporte");
        if (seccion && seccion.scrollIntoView) {
            seccion.scrollIntoView({ behavior: "smooth", block: "start" });
        }
    }
}

function abrirFormularioNuevo() {
    if (!esAdministrador(obtenerSesion())) return;

    document.getElementById("tituloFormularioPlan").textContent = "➕ Nuevo plan";
    document.getElementById("formCodigoOriginal").value = "";
    document.getElementById("formCodigo").value = "";
    document.getElementById("formNombre").value = "";
    document.getElementById("formVelocidad").value = "";
    document.getElementById("formPrecio").value = "";
    document.getElementById("formDescripcion").value = "";
    document.getElementById("modalPlan").style.display = "flex";
    document.getElementById("formCodigo").focus();
}

function editarPlan(codigo) {

    if (!puedeOperarPlanes(obtenerSesion())) return;

    const plan = obtenerPlanes().find(p => p.codigo === Number(codigo));
    if (!plan) return;

    document.getElementById("tituloFormularioPlan").textContent = "✏️ Editar plan " + plan.codigo;
    document.getElementById("formCodigoOriginal").value = plan.codigo;
    document.getElementById("formCodigo").value = plan.codigo;
    document.getElementById("formNombre").value = plan.nombre;
    document.getElementById("formVelocidad").value = plan.velocidad;
    document.getElementById("formPrecio").value = plan.precio;
    document.getElementById("formDescripcion").value = plan.descripcion || "";
    document.getElementById("modalPlan").style.display = "flex";
    document.getElementById("formNombre").focus();
}

document.addEventListener("keydown", function (evento) {
    if (evento.key !== "Escape") return;
    const modal = document.getElementById("modalPlan");
    if (modal && modal.style.display === "flex") {
        cerrarFormularioPlan();
    }
});

function cerrarFormularioPlan() {

    if (document.activeElement && document.activeElement.blur) {
        document.activeElement.blur();
    }
    document.getElementById("modalPlan").style.display = "none";
}

function guardarFormularioPlan(evento) {
    evento.preventDefault();

    const sesion = obtenerSesion();
    if (!puedeOperarPlanes(sesion)) return;

    const codigoOriginal = document.getElementById("formCodigoOriginal").value;
    if (codigoOriginal === "" && !esAdministrador(sesion)) return;
    const datos = {
        codigo: Number(document.getElementById("formCodigo").value.trim()),
        nombre: document.getElementById("formNombre").value.trim(),
        velocidad: document.getElementById("formVelocidad").value.trim(),
        precio: Number(document.getElementById("formPrecio").value.trim()),
        descripcion: document.getElementById("formDescripcion").value.trim()
    };

    const planes = obtenerPlanes();
    const original = codigoOriginal === "" ? null : Number(codigoOriginal);

    const error = validarPlan(datos, planes, original);
    if (error) {
        notificarPlanes(error, "error");
        return;
    }

    if (original === null) {

        planes.push({ ...datos, activo: true });
        guardarPlanes(planes);
        notificarPlanes("Plan creado exitosamente en el catálogo.", "ok");
    } else {

        const indice = planes.findIndex(p => p.codigo === original);
        planes[indice] = { ...planes[indice], ...datos };
        guardarPlanes(planes);
        notificarPlanes("Plan actualizado correctamente.", "ok");
    }

    cerrarFormularioPlan();
    renderizarTablaPlanes();
}

function confirmarAccionPlanes(opciones) {
    const { titulo, mensaje, textoAceptar, textoCancelar, peligro } = opciones;

    return new Promise((resolver) => {
        const fondo = document.createElement("div");
        fondo.className = "modal-fondo";
        fondo.setAttribute("role", "dialog");
        fondo.setAttribute("aria-modal", "true");

        const caja = document.createElement("div");
        caja.className = "modal-caja" + (peligro ? " modal-peligro" : "");

        const icono = document.createElement("div");
        icono.className = "modal-icono";
        icono.textContent = peligro ? "!" : "?";

        const tituloEl = document.createElement("h4");
        tituloEl.className = "modal-titulo";
        tituloEl.textContent = titulo;

        const mensajeEl = document.createElement("p");
        mensajeEl.className = "modal-mensaje";
        mensajeEl.textContent = mensaje;

        const acciones = document.createElement("div");
        acciones.className = "modal-acciones";

        const btnCancelar = document.createElement("button");
        btnCancelar.type = "button";
        btnCancelar.className = "btn btn-secondary modal-cancelar";
        btnCancelar.textContent = textoCancelar || "Cancelar";

        const btnAceptar = document.createElement("button");
        btnAceptar.type = "button";
        btnAceptar.className = "btn " + (peligro ? "btn-danger" : "btn-primary") + " modal-aceptar";
        btnAceptar.textContent = textoAceptar || "Aceptar";

        const cerrar = (resultado) => {
            fondo.classList.add("modal-saliendo");
            setTimeout(() => { if (fondo.remove) fondo.remove(); }, 180);
            resolver(resultado);
        };

        btnAceptar.addEventListener("click", () => cerrar(true));
        btnCancelar.addEventListener("click", () => cerrar(false));
        fondo.addEventListener("click", (evento) => {
            if (evento.target === fondo) cerrar(false);
        });
        document.addEventListener("keydown", function alPresionarEscape(evento) {
            if (evento.key === "Escape") {
                document.removeEventListener("keydown", alPresionarEscape);
                cerrar(false);
            }
        });

        acciones.appendChild(btnCancelar);
        acciones.appendChild(btnAceptar);
        caja.appendChild(icono);
        caja.appendChild(tituloEl);
        caja.appendChild(mensajeEl);
        caja.appendChild(acciones);
        fondo.appendChild(caja);
        document.body.appendChild(fondo);
        requestAnimationFrame(() => fondo.classList.add("modal-visible"));
    });
}

function abrirDetallePlan(codigo) {
    const plan = obtenerPlanes().find(p => p.codigo === Number(codigo));
    if (!plan) return;

    const sesion = obtenerSesion();
    const opera = puedeOperarPlanes(sesion);

    const fondo = document.createElement("div");
    fondo.className = "modal-fondo";
    fondo.setAttribute("role", "dialog");
    fondo.setAttribute("aria-modal", "true");

    const caja = document.createElement("div");
    caja.className = "modal-caja modal-detalle";

    const cabecera = document.createElement("div");
    cabecera.className = "detalle-cabecera";

    const codigoEl = document.createElement("span");
    codigoEl.className = "detalle-codigo";
    codigoEl.textContent = "Código " + plan.codigo;

    const nombreEl = document.createElement("h4");
    nombreEl.textContent = plan.nombre;

    const velocidadEl = document.createElement("p");
    velocidadEl.className = "detalle-velocidad";
    velocidadEl.textContent = plan.velocidad + " de velocidad contratada";

    cabecera.appendChild(codigoEl);
    cabecera.appendChild(nombreEl);
    cabecera.appendChild(velocidadEl);

    const cuerpo = document.createElement("div");
    cuerpo.className = "detalle-cuerpo";

    const crearFila = (etiqueta, valor, esBadge) => {
        const fila = document.createElement("div");
        fila.className = "detalle-fila";

        const etq = document.createElement("span");
        etq.className = "detalle-etiqueta";
        etq.textContent = etiqueta;

        const val = document.createElement("span");
        val.className = "detalle-valor";
        if (esBadge) {
            val.innerHTML = '<span class="badge ' + (plan.activo ? "badge-activo" : "badge-inactivo") +
                            '">' + (plan.activo ? "ACTIVO" : "INACTIVO") + "</span>";
        } else {
            val.textContent = valor;
        }

        fila.appendChild(etq);
        fila.appendChild(val);
        return fila;
    };

    cuerpo.appendChild(crearFila("Velocidad", plan.velocidad));
    cuerpo.appendChild(crearFila("Precio mensual", formatearPrecio(plan.precio)));
    cuerpo.appendChild(crearFila("Estado", null, true));
    cuerpo.appendChild(crearFila("Contratable", plan.activo ? "Sí, disponible en el catálogo" : "No, plan inactivo"));

    const descripcion = document.createElement("p");
    descripcion.className = "detalle-descripcion";
    descripcion.textContent = plan.descripcion || "Sin descripción registrada.";

    const pie = document.createElement("div");
    pie.className = "detalle-pie";

    const btnCerrar = document.createElement("button");
    btnCerrar.type = "button";
    btnCerrar.className = "btn btn-secondary modal-cancelar";
    btnCerrar.textContent = "Cerrar";

    const cerrar = () => {
        fondo.classList.add("modal-saliendo");
        setTimeout(() => { if (fondo.remove) fondo.remove(); }, 180);
    };

    btnCerrar.addEventListener("click", cerrar);

    pie.appendChild(btnCerrar);

    if (opera) {
        const btnEditar = document.createElement("button");
        btnEditar.type = "button";
        btnEditar.className = "btn btn-primary modal-aceptar";
        btnEditar.textContent = "Editar plan";
        btnEditar.addEventListener("click", () => {
            cerrar();
            editarPlan(plan.codigo);
        });
        pie.appendChild(btnEditar);
    }

    fondo.addEventListener("click", (evento) => {
        if (evento.target === fondo) cerrar();
    });
    document.addEventListener("keydown", function alPresionarEscape(evento) {
        if (evento.key === "Escape") {
            document.removeEventListener("keydown", alPresionarEscape);
            cerrar();
        }
    });

    caja.appendChild(cabecera);
    caja.appendChild(cuerpo);
    caja.appendChild(descripcion);
    caja.appendChild(pie);
    fondo.appendChild(caja);
    document.body.appendChild(fondo);
    requestAnimationFrame(() => fondo.classList.add("modal-visible"));
}

function alternarEstadoPlan(codigo) {
    if (!puedeOperarPlanes(obtenerSesion())) return;

    const planes = obtenerPlanes();
    const plan = planes.find(p => p.codigo === Number(codigo));
    if (!plan) return;

    plan.activo = !plan.activo;
    guardarPlanes(planes);
    notificarPlanes(
        'Plan "' + plan.nombre + '" ahora está ' + (plan.activo ? "ACTIVO" : "INACTIVO") + ".",
        "ok"
    );
    renderizarTablaPlanes();
}

async function eliminarPlanCatalogo(codigo) {
    if (!esAdministrador(obtenerSesion())) return;

    const planes = obtenerPlanes();
    const plan = planes.find(p => p.codigo === Number(codigo));
    if (!plan) return;

    const aviso = " Los clientes que lo tengan serán reasignados automáticamente por el sistema.";

    const confirmado = await confirmarAccionPlanes({
        titulo: "Eliminar plan del catálogo",
        mensaje: 'Se eliminará el plan "' + plan.nombre + '" (código ' + plan.codigo + ').' + aviso,
        textoAceptar: "Sí, eliminar",
        textoCancelar: "Cancelar",
        peligro: true
    });
    if (!confirmado) return;

    const restantes = planes.filter(p => p.codigo !== plan.codigo);
    await guardarPlanes(restantes);

    notificarPlanes("Plan eliminado del catálogo.", "ok");
    renderizarTablaPlanes();
}

function cargarSelectPlanesActivos() {
    const select = document.getElementById("selectPlanContratar");
    if (!select) return;

    const activos = obtenerPlanes().filter(p => p.activo);

    if (activos.length === 0) {
        select.innerHTML = '<option value="">No hay planes disponibles</option>';
        return;
    }

    select.innerHTML = activos.map(p =>
        `<option value="${p.codigo}">${p.nombre} · ${p.velocidad} · ${formatearPrecio(p.precio)}</option>`
    ).join("");
}

async function contratarPlan() {
    const sesion = obtenerSesion();
    if (!clienteSinPlan(sesion)) return;

    const select = document.getElementById("selectPlanContratar");
    const codigo = Number(select.value);
    const plan = obtenerPlanes().find(p => p.codigo === codigo);

    if (!plan) {
        notificarPlanes("Selecciona un plan disponible para contratar.", "error");
        return;
    }
    if (!plan.activo) {
        notificarPlanes('El plan "' + plan.nombre + '" está INACTIVO y no puede contratarse.', "error");
        return;
    }
    const confirmado = await confirmarAccionPlanes({
        titulo: "Contratar plan",
        mensaje: 'Vas a contratar el "' + plan.nombre + '" (' + plan.velocidad + ') por ' +
                 formatearPrecio(plan.precio) + " al mes.",
        textoAceptar: "Contratar",
        textoCancelar: "Cancelar",
        peligro: false
    });
    if (!confirmado) return;

    try {
        const respuesta = await fetch(
            API_CLIENTES_PLAN + "/" + encodeURIComponent(sesion.identificacion) + "/plan",
            {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ plan: plan.nombre })
            }
        );
        const json = await respuesta.json();

        if (!json.exito) {
            notificarPlanes(json.mensaje || "No se pudo contratar el plan.", "error");
            return;
        }
    } catch (error) {
        notificarPlanes("No se pudo conectar con el servidor Java (ApiServer).", "error");
        return;
    }

    sesion.planActual = plan.nombre;
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));

    notificarPlanes('¡Plan "' + plan.nombre + '" contratado exitosamente!', "ok");
    renderizarTablaPlanes();
}

async function cargarPlanes() {

    limpiarFiltrosPlanes();

    try {
        await inicializarPlanes();
        renderizarTablaPlanes();
    } catch (error) {
        console.error("Error al cargar los planes:", error);
        notificarPlanes("Error al cargar los planes: " + error.message, "error");
    }
}

document.addEventListener("DOMContentLoaded", cargarPlanes);

document.addEventListener("click", (evento) => {
    const enlace = evento.target.closest('a[href="#planes"]');
    if (enlace) {
        renderizarTablaPlanes();
    }
});
