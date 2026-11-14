/* ==========================================================================
   MÓDULO: CATÁLOGO DE PLANES Y CONTRATACIÓN (Issue #9)
   Autora: Yeini Álvarez
   Archivo: js/planes.js
   Explicación: esta pantalla NO guarda nada por su cuenta. Pide el catálogo al
   servidor Java (ApiServer -> PlanController -> PlanModel -> ListaPlanes) y
   contrata planes con UsuarioController.asignarPlan(). Toda la lógica de
   negocio vive en el backend del equipo; aquí solo se dibuja y se valida el
   formulario.

   Permisos:
   - ADMIN / SUPERADMIN -> administran el catálogo (crear, editar, activar,
     desactivar y eliminar planes).
   - CLIENTE sin plan   -> ven el catálogo y el panel de contratación.
   - CLIENTE con plan   -> solo ven el catálogo y su plan actual.
   ========================================================================== */

// 1. CLAVES DEL ALMACENAMIENTO
const CLAVE_SESION = "sesionActiva";

// 2. CATÁLOGO INICIAL (solo se carga la primera vez)
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

// 3. LECTURA Y ESCRITURA DEL CATÁLOGO
// ------------------------------------------------------------------
// Los planes ya NO se guardan en el navegador: se piden al servidor Java
// (ApiServer -> PlanController -> ListaPlanes). Aquí solo se guarda una
// copia en memoria (cachePlanes) para que la pantalla pueda dibujar rápido.
// ------------------------------------------------------------------
const API_PLANES = API_BASE + "/api/planes";
const API_CLIENTES_PLAN = API_BASE + "/api/clientes";

let cachePlanes = [];

// Pide el catálogo al servidor y lo deja en la copia en memoria
async function inicializarPlanes() {
    try {
        // Si el servidor no responde en 5 segundos, se cancela la petición
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

// Devuelve la copia en memoria (la pantalla la usa de forma inmediata)
function obtenerPlanes() {
    return cachePlanes;
}

// Manda el catálogo completo al servidor y refresca la copia
async function guardarPlanes(listaPlanes) {
    cachePlanes = listaPlanes;
    if (typeof planesActivosCacheHora !== "undefined") {
        planesActivosCacheHora = 0;
    }

    try {
        await fetch(API_PLANES + "/lote", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ planes: listaPlanes })
        });

        // Se vuelve a leer del servidor para quedar sincronizados
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

// 4. SESIÓN Y PERMISOS
function obtenerSesion() {
    try {
        return JSON.parse(localStorage.getItem(CLAVE_SESION));
    } catch (error) {
        return null;
    }
}

// El empleado puede consultar el catálogo y activar/desactivar planes,
// pero NO crearlos, editarlos ni eliminarlos (eso es de ADMIN/SUPERADMIN).
function puedeOperarPlanes(sesion) {
    return !!sesion && (sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN" || sesion.rol === "EMPLEADO");
}

function esAdministrador(sesion) {
    return !!sesion && (sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN");
}

// Un cliente "sin plan" es el que todavía no ha contratado nada.
// Si un administrador se lo asignó, planActual tendrá texto y no verá contratación.
//
// CONTRATO CON EL MÓDULO DE LOGIN (js/app.js, autorregistro):
// esta función considera "sin plan" cualquiera de estos valores:
//     ""  | null | undefined | "Sin plan"
// Mientras app.js siga asignando "Plan Básico 50M" por defecto, el cliente
// cuenta como que YA tiene plan y solo se le muestra el panel "Tu plan actual"
// (no es un error, simplemente no aparece la contratación).
// Cuando ese valor por defecto quede vacío, la contratación aparece sola,
// sin tocar este archivo.
function clienteSinPlan(sesion) {
    if (!sesion || sesion.rol !== "CLIENTE") return false;
    const plan = (sesion.planActual || "").trim();
    return plan === "" || plan.toLowerCase() === "sin plan";
}

// 5. NOTIFICACIONES EMERGENTES (toasts) EN LA ESQUINA
//    Se crean dentro de un contenedor fijo para no mover el contenido de la
//    página cuando aparece un mensaje.
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

    // Animación de entrada (en el siguiente frame para que el navegador la aplique)
    requestAnimationFrame(() => toast.classList.add("toast-visible"));

    // Se cierra sola a los 4 segundos (el usuario también puede cerrarla)
    setTimeout(cerrar, 4000);
}

// 6. VALIDACIONES (espejo de PlanModel.crearPlan / actualizarPlan)
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

    return null; // sin errores
}

// 6.b BUSCADOR Y FILTROS DEL CATÁLOGO
//     Mismo comportamiento que el buscador de Clientes: filtra sobre la copia
//     que ya está en memoria (cachePlanes), sin volver a pedirla al servidor.
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

// Se invoca desde el buscador y desde el selector de estado
function filtrarPlanes() {
    renderizarTablaPlanes();
}

// Deja la barra como recién abierta: buscador vacío y estado en "Todos"
function limpiarFiltrosPlanes() {
    const caja = document.getElementById("buscarPlan");
    if (caja) caja.value = "";

    const filtro = document.getElementById("filtroEstadoPlan");
    if (filtro) filtro.value = "todos";
}

// Vuelve a pedir el catálogo al servidor (botón Recargar)
async function recargarPlanes(boton) {
    if (boton) boton.classList.add("girando");

    await cargarPlanes();

    if (boton) boton.classList.remove("girando");
    notificarPlanes("Catálogo de planes actualizado.", "info");
}

// 7. PINTAR LA TABLA (y decidir qué columnas ve cada rol)
function renderizarTablaPlanes() {
    const cuerpo = document.getElementById("tablaPlanesBody");
    if (!cuerpo) return;

    const sesion = obtenerSesion();
    const admin = esAdministrador(sesion);
    const opera = puedeOperarPlanes(sesion);   // admin o empleado
    const planes = obtenerPlanes();
    const visibles = planesFiltrados();

    // Contador del encabezado
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

    // La columna "Acciones" solo existe para administradores
    // La columna "Acciones" se muestra a quien tenga algún botón:
    // admin y superadmin (editar, estado, eliminar) y empleado (solo estado).
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
        // Columna de acciones: botones solo para ADMIN / SUPERADMIN
        let acciones = "";

        if (admin) {
            // ADMIN y SUPERADMIN: editar, activar/desactivar y eliminar
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
            // EMPLEADO: puede editar la información y activar/desactivar,
            // pero NO crear planes nuevos ni eliminarlos.
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

// 8. APLICAR PERMISOS A LA INTERFAZ
function aplicarPermisos(sesion) {
    const admin = esAdministrador(sesion);
    const opera = puedeOperarPlanes(sesion);
    const sinPlan = clienteSinPlan(sesion);
    const conPlan = !!sesion && sesion.rol === "CLIENTE" && !sinPlan;

    // Barra y formulario de administración
    const barraAdmin = document.getElementById("barraAdminPlanes");
    if (barraAdmin) barraAdmin.style.display = admin ? "flex" : "none";

    const modalFormulario = document.getElementById("modalPlan");
    if (modalFormulario && !opera) modalFormulario.style.display = "none";

    // El catálogo completo (contadores + tabla) es solo para administradores.
    // El cliente no debe ver el catálogo: ve su plan o, si no tiene, la contratación.
    const resumen = document.getElementById("resumenPlanes");
    if (resumen) resumen.style.display = opera ? "flex" : "none";

    const tabla = document.getElementById("tablaPlanesWrapper");
    if (tabla) tabla.style.display = opera ? "block" : "none";

    // La barra de búsqueda solo tiene sentido si se ve el catálogo
    const barra = document.getElementById("toolbarPlanes");
    if (barra) barra.style.display = opera ? "flex" : "none";

    // Panel de contratación: solo clientes sin plan
    const panelContratar = document.getElementById("panelContratacion");
    if (panelContratar) {
        panelContratar.style.display = sinPlan ? "block" : "none";
        if (sinPlan) cargarSelectPlanesActivos();
    }

    // Panel informativo para clientes que ya tienen plan
    const panelPlanActual = document.getElementById("panelPlanActual");
    if (panelPlanActual) {
        panelPlanActual.style.display = conPlan ? "block" : "none";
        if (conPlan) renderizarPlanActual(sesion);
    }

    // Descripción de la sección según quién la esté viendo
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

// 8.b TARJETA DEL PLAN DEL CLIENTE (mismo diseño que la ficha de detalle)
//      El estado de pago del cliente llega en sesion.pagoAlDia.
//      Si el dato no existe todavía (módulo de Facturación pendiente) se
//      asume "al día", porque no hay deuda registrada.
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

    // El plan asignado puede no existir en el catálogo (datos de ejemplo del login)
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
}

// Bloque final con el aviso resaltado y el botón de contacto
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

// Lleva al cliente al módulo de soporte del sistema
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

// 9. FORMULARIO DE CREAR / EDITAR
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
    // Admin y empleado pueden editar; crear y eliminar siguen siendo de admin
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

// Escape cierra la ventana del formulario de planes
document.addEventListener("keydown", function (evento) {
    if (evento.key !== "Escape") return;
    const modal = document.getElementById("modalPlan");
    if (modal && modal.style.display === "flex") {
        cerrarFormularioPlan();
    }
});

function cerrarFormularioPlan() {
    // Se quita el foco antes de ocultar para que el navegador no haga un salto
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
        // CREAR
        planes.push({ ...datos, activo: true });
        guardarPlanes(planes);
        notificarPlanes("Plan creado exitosamente en el catálogo.", "ok");
    } else {
        // ACTUALIZAR (conserva el estado activo/inactivo)
        const indice = planes.findIndex(p => p.codigo === original);
        planes[indice] = { ...planes[indice], ...datos };
        guardarPlanes(planes);
        notificarPlanes("Plan actualizado correctamente.", "ok");
    }

    cerrarFormularioPlan();
    renderizarTablaPlanes();
}

// 9.b MODAL DE CONFIRMACIÓN (reemplaza al confirm() del navegador)
//      Devuelve una promesa que se resuelve con true (Aceptar) o false (Cancelar).
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

// 9.c DETALLE DEL PLAN EN VENTANA EMERGENTE (al hacer clic en una fila)
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

    // Cabecera azul con el nombre del plan
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

    // Cuerpo con los datos
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

    // Descripción
    const descripcion = document.createElement("p");
    descripcion.className = "detalle-descripcion";
    descripcion.textContent = plan.descripcion || "Sin descripción registrada.";

    // Pie con acciones
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

// 10. ACTIVAR / DESACTIVAR  (PlanController.cambiarEstadoPlan)
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

// 11. ELIMINAR CON REASIGNACIÓN AUTOMÁTICA (PlanController.eliminarPlan)
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

    // Se quita del catálogo. La reasignación de los clientes afectados la
    // hace el backend automáticamente (PlanModel.eliminarPlan -> cascada).
    const restantes = planes.filter(p => p.codigo !== plan.codigo);
    await guardarPlanes(restantes);

    notificarPlanes("Plan eliminado del catálogo.", "ok");
    renderizarTablaPlanes();
}

// 12. CONTRATACIÓN (ClienteModel.asignarPlan)
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

    // 1. Se asigna el plan en el servidor:
    //    UsuarioController.asignarPlan(identificacion, nombrePlan)
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

    // 2. Se actualiza la sesión del navegador (solo para mostrar el plan)
    sesion.planActual = plan.nombre;
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));

    notificarPlanes('¡Plan "' + plan.nombre + '" contratado exitosamente!', "ok");
    renderizarTablaPlanes();
}

// 13. ARRANQUE
// Pide los planes al servidor y dibuja la tabla.
// Se llama al abrir la sección (igual que hace Clientes con cargarClientes).
async function cargarPlanes() {
    // Al cargar o recargar se vuelve al estado inicial: sin texto de búsqueda y
    // sin filtro de estado, de manera que se ven TODOS los planes otra vez.
    // Es el mismo comportamiento del botón Recargar de Clientes.
    limpiarFiltrosPlanes();

    try {
        await inicializarPlanes();
        renderizarTablaPlanes();
    } catch (error) {
        console.error("Error al cargar los planes:", error);
        notificarPlanes("Error al cargar los planes: " + error.message, "error");
    }
}

// Carga inicial por si la página abre directamente en esta sección
document.addEventListener("DOMContentLoaded", cargarPlanes);

// Al entrar a la sección Planes se repinta (siempre con datos frescos)
document.addEventListener("click", (evento) => {
    const enlace = evento.target.closest('a[href="#planes"]');
    if (enlace) {
        renderizarTablaPlanes();
    }
});
