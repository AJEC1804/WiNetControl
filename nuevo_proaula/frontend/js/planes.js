/* ==========================================================================
   MÓDULO: CATÁLOGO DE PLANES Y CONTRATACIÓN (Issue #9)
   Autora: Yeini Álvarez
   Archivo: js/planes.js
   Explicación: replica en JavaScript las reglas que el equipo escribió en Java
   (backend/src/modelos/PlanModel.java y ClienteModel.asignarPlan). El sitio es
   estático (GitHub Pages), así que el "backend" del navegador es localStorage,
   igual que hace app.js con los usuarios (clave "usuariosWiNet").

   Permisos:
   - ADMIN / SUPERADMIN -> administran el catálogo (crear, editar, activar,
     desactivar y eliminar planes).
   - CLIENTE sin plan   -> ven el catálogo y el panel de contratación.
   - CLIENTE con plan   -> solo ven el catálogo y su plan actual.
   ========================================================================== */

// 1. CLAVES DEL ALMACENAMIENTO
const CLAVE_PLANES = "planesWiNet";
const CLAVE_USUARIOS = "usuariosWiNet";
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
function inicializarPlanes() {
    const guardados = localStorage.getItem(CLAVE_PLANES);
    if (!guardados) {
        localStorage.setItem(CLAVE_PLANES, JSON.stringify(planesIniciales));
        return;
    }
    try {
        JSON.parse(guardados);
    } catch (error) {
        // Si el contenido está dañado, se restaura el catálogo base
        localStorage.setItem(CLAVE_PLANES, JSON.stringify(planesIniciales));
    }
}

function obtenerPlanes() {
    inicializarPlanes();
    return JSON.parse(localStorage.getItem(CLAVE_PLANES)) || [];
}

function guardarPlanes(listaPlanes) {
    localStorage.setItem(CLAVE_PLANES, JSON.stringify(listaPlanes));
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

// 7. PINTAR LA TABLA (y decidir qué columnas ve cada rol)
function renderizarTablaPlanes() {
    const cuerpo = document.getElementById("tablaPlanesBody");
    if (!cuerpo) return;

    const sesion = obtenerSesion();
    const admin = esAdministrador(sesion);
    const planes = obtenerPlanes();

    // Contador del encabezado
    const resumen = document.getElementById("resumenPlanes");
    if (resumen) {
        const total = planes.length;
        const activos = planes.filter(p => p.activo).length;
        const inactivos = total - activos;
        resumen.innerHTML = `
            <span class="plan-chip plan-chip-total">
                <span class="plan-chip-num">${total}</span> en el catálogo
            </span>
            <span class="plan-chip plan-chip-activos">
                <span class="plan-chip-num">${activos}</span> activos
            </span>
            <span class="plan-chip plan-chip-inactivos">
                <span class="plan-chip-num">${inactivos}</span> inactivos
            </span>`;
    }

    // La columna "Acciones" solo existe para administradores
    const encabezadoAcciones = document.getElementById("thAccionesPlanes");
    if (encabezadoAcciones) {
        encabezadoAcciones.style.display = admin ? "" : "none";
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

    cuerpo.innerHTML = planes.map(plan => {
        // Columna de acciones: botones solo para ADMIN / SUPERADMIN
        const acciones = admin ? `
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
            </td>` : "";

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
    const sinPlan = clienteSinPlan(sesion);
    const conPlan = !!sesion && sesion.rol === "CLIENTE" && !sinPlan;

    // Barra y formulario de administración
    const barraAdmin = document.getElementById("barraAdminPlanes");
    if (barraAdmin) barraAdmin.style.display = admin ? "flex" : "none";

    const formulario = document.getElementById("panelFormularioPlan");
    if (formulario && !admin) formulario.style.display = "none";

    // El catálogo completo (contadores + tabla) es solo para administradores.
    // El cliente no debe ver el catálogo: ve su plan o, si no tiene, la contratación.
    const resumen = document.getElementById("resumenPlanes");
    if (resumen) resumen.style.display = admin ? "flex" : "none";

    const tabla = document.getElementById("tablaPlanesWrapper");
    if (tabla) tabla.style.display = admin ? "block" : "none";

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
        if (admin) {
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

    document.getElementById("tituloFormularioPlan").textContent = "Nuevo plan";
    document.getElementById("formCodigoOriginal").value = "";
    document.getElementById("formCodigo").value = "";
    document.getElementById("formNombre").value = "";
    document.getElementById("formVelocidad").value = "";
    document.getElementById("formPrecio").value = "";
    document.getElementById("formDescripcion").value = "";
    document.getElementById("panelFormularioPlan").style.display = "block";
    document.getElementById("formCodigo").focus();
}

function editarPlan(codigo) {
    if (!esAdministrador(obtenerSesion())) return;

    const plan = obtenerPlanes().find(p => p.codigo === Number(codigo));
    if (!plan) return;

    document.getElementById("tituloFormularioPlan").textContent = "Editar plan " + plan.codigo;
    document.getElementById("formCodigoOriginal").value = plan.codigo;
    document.getElementById("formCodigo").value = plan.codigo;
    document.getElementById("formNombre").value = plan.nombre;
    document.getElementById("formVelocidad").value = plan.velocidad;
    document.getElementById("formPrecio").value = plan.precio;
    document.getElementById("formDescripcion").value = plan.descripcion || "";
    document.getElementById("panelFormularioPlan").style.display = "block";
    document.getElementById("formNombre").focus();
}

function cerrarFormularioPlan() {
    document.getElementById("panelFormularioPlan").style.display = "none";
}

function guardarFormularioPlan(evento) {
    evento.preventDefault();
    if (!esAdministrador(obtenerSesion())) return;

    const codigoOriginal = document.getElementById("formCodigoOriginal").value;
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
    const admin = esAdministrador(sesion);

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

    if (admin) {
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
    if (!esAdministrador(obtenerSesion())) return;

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

    const afectados = clientesConPlan(plan.nombre);
    const aviso = afectados.length > 0
        ? " " + afectados.length + " cliente(s) tienen este plan y serán reasignados automáticamente."
        : "";

    const confirmado = await confirmarAccionPlanes({
        titulo: "Eliminar plan del catálogo",
        mensaje: 'Se eliminará el plan "' + plan.nombre + '" (código ' + plan.codigo + ').' + aviso,
        textoAceptar: "Sí, eliminar",
        textoCancelar: "Cancelar",
        peligro: true
    });
    if (!confirmado) return;

    // 1. Quitar el plan del catálogo
    const restantes = planes.filter(p => p.codigo !== plan.codigo);
    guardarPlanes(restantes);

    // 2. Reasignar a los clientes afectados al primer plan disponible
    const sustituto = restantes.length > 0 ? restantes[0].nombre : "Sin plan";
    reasignarClientes(plan.nombre, sustituto);

    notificarPlanes(
        "Plan eliminado. " + afectados.length + " cliente(s) reasignados a: " + sustituto + ".",
        "ok"
    );
    renderizarTablaPlanes();
}

function clientesConPlan(nombrePlan) {
    const usuarios = JSON.parse(localStorage.getItem(CLAVE_USUARIOS)) || [];
    return usuarios.filter(u =>
        u.planActual && u.planActual.toLowerCase() === nombrePlan.toLowerCase()
    );
}

function reasignarClientes(nombreViejo, nombreNuevo) {
    const usuarios = JSON.parse(localStorage.getItem(CLAVE_USUARIOS)) || [];
    let cambios = 0;

    usuarios.forEach(u => {
        if (u.planActual && u.planActual.toLowerCase() === nombreViejo.toLowerCase()) {
            u.planActual = nombreNuevo;
            cambios++;
        }
    });

    if (cambios > 0) {
        localStorage.setItem(CLAVE_USUARIOS, JSON.stringify(usuarios));

        // Si el cliente afectado es el que tiene la sesión abierta, se actualiza también
        const sesion = obtenerSesion();
        if (sesion && sesion.planActual && sesion.planActual.toLowerCase() === nombreViejo.toLowerCase()) {
            sesion.planActual = nombreNuevo;
            localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
        }
    }
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

    // 1. Guardar el plan en el usuario
    const usuarios = JSON.parse(localStorage.getItem(CLAVE_USUARIOS)) || [];
    const indice = usuarios.findIndex(u => u.identificacion === sesion.identificacion);
    if (indice >= 0) {
        usuarios[indice].planActual = plan.nombre;
        localStorage.setItem(CLAVE_USUARIOS, JSON.stringify(usuarios));
    }

    // 2. Actualizar la sesión activa
    sesion.planActual = plan.nombre;
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));

    notificarPlanes('¡Plan "' + plan.nombre + '" contratado exitosamente!', "ok");
    renderizarTablaPlanes();
}

// 12.b USUARIO DE PRUEBA (rol CLIENTE con un plan del catálogo)
//      Sirve para demostrar cómo ve un cliente su plan.
//      Credenciales: 1045678902 / Demo123!
const CLAVE_DEMO = "demoClienteCreado";
const CLIENTE_DEMO = {
    identificacion: "1045678902",
    usuario: "1045678902",
    contrasena: "Demo123!",
    nombre: "Camila",
    apellido: "Restrepo",
    tipo: "CC",
    telefono: "3001234567",
    correo: "camila.restrepo@winetcontrol.com",
    direccion: "Calle 45 # 12-30",
    rol: "CLIENTE",
    sucursal: "Residencial Norte",
    activo: true
};

function asegurarClienteDemo() {
    if (localStorage.getItem(CLAVE_DEMO) === "1") return;

    const usuarios = JSON.parse(localStorage.getItem(CLAVE_USUARIOS)) || [];
    const yaExiste = usuarios.some(u => u.identificacion === CLIENTE_DEMO.identificacion);

    if (!yaExiste) {
        // Se le asigna el primer plan activo del catálogo, así siempre es un plan real
        const activos = obtenerPlanes().filter(p => p.activo);
        usuarios.push(Object.assign({}, CLIENTE_DEMO, {
            planActual: activos.length > 0 ? activos[0].nombre : "",
            pagoAlDia: true
        }));
        localStorage.setItem(CLAVE_USUARIOS, JSON.stringify(usuarios));
    }

    localStorage.setItem(CLAVE_DEMO, "1");
}

// 13. ARRANQUE
document.addEventListener("DOMContentLoaded", () => {
    inicializarPlanes();
    asegurarClienteDemo();
    renderizarTablaPlanes();
});

// Al entrar a la sección Planes se repinta (siempre con datos frescos)
document.addEventListener("click", (evento) => {
    const enlace = evento.target.closest('a[href="#planes"]');
    if (enlace) {
        renderizarTablaPlanes();
    }
});
