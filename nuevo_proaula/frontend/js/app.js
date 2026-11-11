/* ==========================================================================
   LÓGICA JAVASCRIPT - WINET CONTROL (HTML & CSS PARA PRINCIPIANTES)
   Autor: Alejandro Escudero
   Explicación: Este archivo maneja la autenticación por roles (Superusuario,
   Administrador Local y Cliente), registro de clientes, permisos en el 
   dashboard y cierre de sesión.
   ========================================================================== */

// ==========================================================================
// 1. AUTENTICACIÓN
// --------------------------------------------------------------------------
// Ya NO hay usuarios guardados en el navegador. El login pregunta al servidor
// Java (POST /api/login), que valida en:
//   UsuarioController.autenticarUsuario()  -> superadmin, admin y clientes
//   EmpleadoController.autenticar()        -> empleados
// La contraseña se compara con su hash SHA-256 y se verifica que esté activo.
// ==========================================================================
const API_LOGIN = "http://localhost:8080/api/login";

// ==========================================================================
// 2. FUNCIÓN DE INICIO DE SESIÓN (LOGIN UNIVERSAL CON ROLES)
// ==========================================================================
async function procesarLogin(evento) {
    evento.preventDefault();

    const idInput = document.getElementById("loginIdentificacion").value.trim();
    const claveInput = document.getElementById("loginContrasena").value.trim();
    const alertBox = document.getElementById("loginAlert");

    alertBox.style.display = "none";
    alertBox.className = "alert";

    if (idInput === "" || claveInput === "") {
        mostrarAlerta(alertBox, "Por favor complete todos los campos requeridos.", "error");
        return;
    }

    let json;

    try {
        const respuesta = await fetch(API_LOGIN, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ usuario: idInput, contrasena: claveInput })
        });
        json = await respuesta.json();
    } catch (error) {
        mostrarAlerta(alertBox, "No se pudo conectar con el servidor. Arranca ApiServer e inténtalo de nuevo.", "error");
        return;
    }

    if (!json.exito) {
        mostrarAlerta(alertBox, json.mensaje, "error");
        return;
    }

    // Sesión: los datos vienen del servidor (identificación, nombre, rol, plan...)
    const usuario = json.datos;
    localStorage.setItem("sesionActiva", JSON.stringify(usuario));
    sessionStorage.removeItem("seccionAbierta");

    const textoRol = usuario.rol === "SUPERADMIN" ? "Superusuario" :
                     usuario.rol === "ADMIN" ? "Administrador Local" :
                     usuario.rol === "EMPLEADO" ? "Empleado" : "Cliente";

    mostrarAlerta(alertBox, "¡Bienvenido, " + usuario.nombre + " (" + textoRol + ")! Redirigiendo...", "success");

    setTimeout(function () {
        window.location.href = "dashboard.html";
    }, 900);
}

function procesarRegistro(evento) {
    evento.preventDefault();

    const alertBox = document.getElementById("registroAlert");
    alertBox.style.display = "none";

    const nombres = document.getElementById("regNombres").value.trim();
    const apellidos = document.getElementById("regApellidos").value.trim();
    const tipo = document.getElementById("regTipo").value;
    const identificacion = document.getElementById("regIdentificacion").value.trim();
    const telefono = document.getElementById("regTelefono").value.trim();
    const correo = document.getElementById("regCorreo").value.trim().toLowerCase();
    const direccion = document.getElementById("regDireccion").value.trim();
    const contrasena = document.getElementById("regContrasena").value;
    const confContrasena = document.getElementById("regConfContrasena").value;

    // 1. Campos obligatorios
    if (!nombres || !apellidos || !identificacion || !telefono || !correo || !direccion || !contrasena) {
        mostrarAlerta(alertBox, "Todos los campos marcados son obligatorios.", "error");
        return;
    }

    // 2. Validación de identificación numérica (8 a 10 dígitos)
    if (!/^\d{8,10}$/.test(identificacion)) {
        mostrarAlerta(alertBox, "La identificación debe tener entre 8 y 10 dígitos numéricos.", "error");
        return;
    }

    // 3. Validación de celular
    if (!/^\d{10}$/.test(telefono)) {
        mostrarAlerta(alertBox, "El teléfono celular debe contener exactamente 10 dígitos.", "error");
        return;
    }

    // 4. Mínimo 6 caracteres
    if (contrasena.length < 6) {
        mostrarAlerta(alertBox, "La contraseña debe tener mínimo 6 caracteres.", "error");
        return;
    }

    if (contrasena !== confContrasena) {
        mostrarAlerta(alertBox, "Las contraseñas no coinciden.", "error");
        return;
    }

    // Nuevo cliente con rol CLIENTE
    const nuevoCliente = {
        identificacion: identificacion,
        usuario: identificacion,
        contrasena: contrasena,
        nombre: nombres,
        apellido: apellidos,
        tipo: tipo,
        telefono: telefono,
        correo: correo,
        direccion: direccion,
        rol: "CLIENTE",
        sucursal: "Sucursal Local Norte",
        planActual: "",
        activo: true
    };

    // 1) Se guarda en el servidor Java: así aparece en la tabla de Gestión de Clientes
    fetch(API_CLIENTES, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            identificacion: identificacion,
            nombre: nombres,
            apellido: apellidos,
            tipo: tipo,
            telefono: telefono,
            correo: correo,
            direccion: direccion,
            contrasena: contrasena,
            activo: "true"
        })
    })
    .then(function (respuesta) { return respuesta.json(); })
    .then(function (json) {
        if (!json.exito) {
            mostrarAlerta(alertBox, json.mensaje, "error");
            return;
        }

        mostrarAlerta(alertBox, "¡Cuenta creada exitosamente! Redirigiendo al inicio de sesión...", "success");

        setTimeout(function () {
            window.location.href = "login.html";
        }, 1400);
    })
    .catch(function () {
        mostrarAlerta(alertBox, "No se pudo conectar con el servidor Java. Arranca ApiServer e inténtalo de nuevo.", "error");
    });
}

// ==========================================================================
// 4. CARGAR DASHBOARD Y ADAPTAR SEGÚN EL ROL
// ==========================================================================
function cargarDashboard() {
    const sesion = localStorage.getItem("sesionActiva");

    if (!sesion) {
        window.location.href = "login.html";
        return;
    }

    const usuario = JSON.parse(sesion);
    const nombreCompleto = (usuario.nombre || usuario.nombres) + " " + (usuario.apellido || usuario.apellidos);

    // 1. Datos del Encabezado
    if (document.getElementById("dashNombreHeader")) {
        document.getElementById("dashNombreHeader").textContent = nombreCompleto;
    }
    if (document.getElementById("dashNombreBienvenida")) {
        document.getElementById("dashNombreBienvenida").textContent = nombreCompleto;
    }
    if (document.getElementById("dashCorreoHeader")) {
        document.getElementById("dashCorreoHeader").textContent = usuario.correo;
    }
    if (document.getElementById("dashAvatar")) {
        document.getElementById("dashAvatar").textContent = (usuario.nombre || usuario.nombres || "U").charAt(0).toUpperCase();
    }

    // 2. Badge de Rol
    const badgeRol = document.getElementById("dashRolBadge");
    if (badgeRol) {
        if (usuario.rol === "SUPERADMIN") {
            badgeRol.textContent = "👑 SUPERUSUARIO";
            badgeRol.className = "badge badge-superadmin";
        } else if (usuario.rol === "ADMIN") {
            badgeRol.textContent = "🏢 ADMINISTRADOR LOCAL";
            badgeRol.className = "badge badge-admin";
        } else if (usuario.rol === "EMPLEADO") {
            badgeRol.textContent = "👔 EMPLEADO";
            badgeRol.className = "badge badge-admin";
        } else {
            badgeRol.textContent = "👤 CLIENTE SUSCRIPTOR";
            badgeRol.className = "badge badge-cliente";
        }
    }

    // Badge de Sucursal / Ámbito
    const badgeSucursal = document.getElementById("dashSucursalBadge");
    if (badgeSucursal) {
        badgeSucursal.textContent = usuario.sucursal || "Sede WiNet";
    }

    // 3. Descripción del Banner según Rol
    const descBienvenida = document.getElementById("dashDescBienvenida");
    if (descBienvenida) {
        if (usuario.rol === "SUPERADMIN") {
            descBienvenida.textContent = "Panel Central con control total sobre todas las sedes, clientes, planes comerciales, facturación y auditoría global.";
        } else if (usuario.rol === "ADMIN") {
            descBienvenida.textContent = "Panel de Gestión Local: Administra los clientes, activación de servicios, facturación y tickets de soporte de tu sucursal.";
        } else if (usuario.rol === "EMPLEADO") {
            descBienvenida.textContent = "Portal Operativo: consulta y registra clientes, y atiende las solicitudes de soporte de los abonados.";
        } else {
            descBienvenida.textContent = "Bienvenido a tu portal de suscriptor. Consulta el estado de tu servicio de fibra óptica, tus facturas y solicita soporte técnico.";
        }
    }

    // 4. Adaptar Tarjetas KPI (Stats) según Rol
    renderizarEstadisticasPorRol(usuario);

    // 5. Control de Permisos en la Navegación y Secciones
    adaptarVistasPorRol(usuario);
}

// Renderizar tarjetas de estadísticas según el rol
function renderizarEstadisticasPorRol(usuario) {
    const statsContainer = document.getElementById("dashStatsGrid");
    if (!statsContainer) return;

    if (usuario.rol === "SUPERADMIN") {
        statsContainer.innerHTML = `
            <div class="stat-card">
                <h5>Ámbito de Acceso</h5>
                <div class="stat-value" style="font-size: 18px; color: #6d28d9;">👑 Acceso Global</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Todas las sedes y módulos</div>
            </div>
            <div class="stat-card">
                <h5>Suscriptores Totales</h5>
                <div class="stat-value">1,420 <span style="font-size: 13px; color: #16a34a; font-weight: normal;">(Red WiNet)</span></div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Clientes en toda la plataforma</div>
            </div>
            <div class="stat-card">
                <h5>Planes Comerciales</h5>
                <div class="stat-value" style="color: #2563eb;">4 Activos</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Catálogo comercial vigente</div>
            </div>
            <div class="stat-card">
                <h5>Recaudación Mensual</h5>
                <div class="stat-value" style="color: #059669;">$84.5M COP</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Facturación consolidada</div>
            </div>
        `;
    } else if (usuario.rol === "ADMIN") {
        statsContainer.innerHTML = `
            <div class="stat-card">
                <h5>Sucursal Asignada</h5>
                <div class="stat-value" style="font-size: 18px; color: #0369a1;">🏢 Norte Local</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Gestión de sede local</div>
            </div>
            <div class="stat-card">
                <h5>Clientes de la Sede</h5>
                <div class="stat-value">328 <span style="font-size: 13px; color: #16a34a; font-weight: normal;">(Activos)</span></div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">4 pendientes por reconexión</div>
            </div>
            <div class="stat-card">
                <h5>Facturas de la Sede</h5>
                <div class="stat-value" style="color: #2563eb;">324 Emitidas</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">98% al día en pago</div>
            </div>
            <div class="stat-card">
                <h5>Tickets en Cola (FIFO)</h5>
                <div class="stat-value" style="color: #d97706;">2 en Espera</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Atención en orden de llegada</div>
            </div>
        `;
    } else if (usuario.rol === "EMPLEADO") {
        // EMPLEADO: personal operativo, sin acceso a facturación ni catálogo
        statsContainer.innerHTML = `
            <div class="stat-card">
                <h5>Mi Rol</h5>
                <div class="stat-value" style="font-size: 18px; color: #1d4ed8;">👔 Empleado</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">${usuario.planActual || "Personal operativo"}</div>
            </div>
            <div class="stat-card">
                <h5>Sucursal</h5>
                <div class="stat-value" style="font-size: 18px; color: #0369a1;">${usuario.sucursal || "Sucursal Norte"}</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Sede asignada</div>
            </div>
            <div class="stat-card">
                <h5>Permisos</h5>
                <div class="stat-value" style="font-size: 16px; color: #16a34a;">Clientes y soporte</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Consulta planes · Sin eliminar clientes</div>
            </div>
            <div class="stat-card">
                <h5>Tickets Pendientes</h5>
                <div class="stat-value" style="color: #d97706;">Módulo de Soporte</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Issue #10 — Juan Esteban Navia</div>
            </div>
        `;
    } else {
        // CLIENTE
        statsContainer.innerHTML = `
            <div class="stat-card">
                <h5>Estado del Servicio</h5>
                <div style="margin-top: 8px;">
                    <span class="badge ${usuario.activo ? 'badge-activo' : 'badge-inactivo'}">
                        ${usuario.activo ? 'ACTIVO' : 'INACTIVO'}
                    </span>
                </div>
                <div style="font-size: 12px; color: #64748b; margin-top: 6px;">Línea de fibra en línea</div>
            </div>
            <div class="stat-card">
                <h5>Plan Contratado</h5>
                <div class="stat-value" style="font-size: 18px; color: #2563eb;">${usuario.planActual || 'Plan Básico 50M'}</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Internet de alta velocidad</div>
            </div>
            <div class="stat-card">
                <h5>Velocidad de Conexión</h5>
                <div class="stat-value">300 <span style="font-size: 14px; font-weight: 500; color: #64748b;">Mbps Simétrico</span></div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Fibra óptica directa al hogar</div>
            </div>
            <div class="stat-card">
                <h5>Última Factura (LIFO)</h5>
                <div class="stat-value" style="color: #16a34a; font-size: 18px;">Al Día ✅</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Próximo corte: 15/11/2026</div>
            </div>
        `;
    }
}

// Adaptar la visibilidad de enlaces y secciones según rol
function adaptarVistasPorRol(usuario) {
    // Gestión de Empleados: solo ADMIN y SUPERADMIN
    const enlaceEmpleados = document.getElementById("navLinkEmpleados");
    if (enlaceEmpleados) {
        const puedeGestionar = (usuario.rol === "ADMIN" || usuario.rol === "SUPERADMIN");
        enlaceEmpleados.style.display = puedeGestionar ? "flex" : "none";
    }

    const esCliente = (usuario.rol === "CLIENTE");

    // El cliente no administra la lista de clientes: ve solo sus propios datos
    const enlaceClientes = document.getElementById("navLinkClientes");
    if (enlaceClientes) {
        enlaceClientes.style.display = esCliente ? "none" : "flex";
    }

    // "Mi perfil" solo existe para el cliente
    const enlacePerfil = document.getElementById("navLinkPerfil");
    if (enlacePerfil) {
        enlacePerfil.style.display = esCliente ? "flex" : "none";
    }

    // Al cliente, "Catálogo de Planes" se le presenta como "Mi plan"
    const enlacePlanesTexto = document.getElementById("navLinkPlanes");
    if (enlacePlanesTexto) {
        enlacePlanesTexto.innerHTML = esCliente
            ? '<span>📡</span> Mi plan'
            : '<span>📡</span> Catálogo de Planes';
    }

    // Catálogo de Planes y Facturación: ADMIN, SUPERADMIN y EMPLEADO
    // (el empleado los consulta, pero no crea ni elimina)
    const puedeVerOperacion = (usuario.rol === "ADMIN" || usuario.rol === "SUPERADMIN"
                               || usuario.rol === "EMPLEADO");

    const enlacePlanes = document.getElementById("navLinkPlanes");
    if (enlacePlanes) {
        // El cliente SÍ lo ve (es "Mi plan"), pero no puede administrar
        enlacePlanes.style.display = "flex";
    }

    // Facturación: todos la ven. El cliente ve SUS facturas;
    // admin y empleado ven la facturación completa.
    const enlaceFacturacion = document.getElementById("navLinkFacturacion");
    if (enlaceFacturacion) {
        enlaceFacturacion.style.display = "flex";
    }

    const enlaceConfig = document.getElementById("navLinkConfiguracion");
    if (enlaceConfig) {
        // La sección de configuración global solo es visible para SUPERADMIN
        enlaceConfig.style.display = (usuario.rol === "SUPERADMIN") ? "flex" : "none";
    }

    const bannerAdminNotice = document.getElementById("clienteNoticeClientes");
    if (bannerAdminNotice) {
        // En la sección clientes, si es un cliente normal mostramos aviso
        bannerAdminNotice.style.display = (usuario.rol === "CLIENTE") ? "block" : "none";
    }

    const contenidoClientesAdmin = document.getElementById("adminContenidoClientes");
    if (contenidoClientesAdmin) {
        contenidoClientesAdmin.style.display = (usuario.rol === "CLIENTE") ? "none" : "block";
    }
}

// ==========================================================================
// 5. CERRAR SESIÓN
// ==========================================================================
function cerrarSesion() {
    localStorage.removeItem("sesionActiva");

    // Se olvida la sección donde estaba: al volver a entrar arranca en Inicio
    sessionStorage.removeItem("seccionAbierta");

    window.location.href = "login.html";
}

// ¿Este rol puede ver esta sección?
function seccionPermitida(seccion, rol) {
    const permisos = {
        inicio:    ["SUPERADMIN", "ADMIN", "EMPLEADO", "CLIENTE"],
        clientes:  ["SUPERADMIN", "ADMIN", "EMPLEADO"],
        perfil:    ["CLIENTE"],
        empleados: ["SUPERADMIN", "ADMIN"],
        planes:    ["SUPERADMIN", "ADMIN", "EMPLEADO", "CLIENTE"],
        facturas:  ["SUPERADMIN", "ADMIN", "EMPLEADO", "CLIENTE"],
        soporte:   ["SUPERADMIN", "ADMIN", "EMPLEADO", "CLIENTE"]
    };

    if (!permisos[seccion]) return false;
    return permisos[seccion].indexOf(rol) !== -1;
}

// ==========================================================================
// 6. UTILIDAD PARA MOSTRAR ALERTAS
// ==========================================================================
function mostrarAlerta(elementoAlerta, mensaje, tipo) {
    elementoAlerta.textContent = mensaje;
    elementoAlerta.style.display = "block";
    if (tipo === "error") {
        elementoAlerta.className = "alert alert-error";
    } else {
        elementoAlerta.className = "alert alert-success";
    }
}


// ==========================================================================
// PERMISOS POR ROL
// --------------------------------------------------------------------------
// ADMIN y SUPERADMIN pueden todo. EMPLEADO puede ver, registrar y editar,
// pero NO eliminar clientes ni cambiar el estado del servicio.
// ==========================================================================
function sesionActual() {
    try {
        return JSON.parse(localStorage.getItem("sesionActiva"));
    } catch (error) {
        return null;
    }
}

function esSuperAdminActual() {
    const sesion = sesionActual();
    return !!sesion && sesion.rol === "SUPERADMIN";
}

// Eliminar empleados: ADMIN y SUPERADMIN
function puedeEliminarEmpleados() {
    const sesion = sesionActual();
    return !!sesion && (sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN");
}

function puedeAdministrarClientes() {
    const sesion = sesionActual();
    return !!sesion && (sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN");
}

// ==========================================================================
// 7. GESTIÓN DE CLIENTES  ->  LLAMA A LOS MÉTODOS DE JAVA
// --------------------------------------------------------------------------
// La página no guarda ni calcula nada por su cuenta: pide los datos a
// ApiServer (que corre en localhost:8080) y ese servidor llama a los métodos
// de controladores.UsuarioController. Así las estructuras de datos
// (ListaClientes, ListaDoble, Pila, Cola) son las que hacen el trabajo.
// ==========================================================================
const API_CLIENTES = "http://localhost:8080/api/clientes";
const API_PLANES_CATALOGO = "http://localhost:8080/api/planes";

const FILAS_POR_PAGINA = 5;   // cuántos clientes se ven por página
let paginaActual = 1;         // en qué página estamos
let clientesFiltrados = [];   // resultado actual (lo que se dibuja)
let clientesTodos = [];       // copia de TODOS los clientes del servidor
let temporizadorBusqueda = null;

function pintarTabla(lista) {
    const cuerpo = document.getElementById("tablaClientes");

    if (lista.length === 0) {
        cuerpo.innerHTML = `
            <tr>
                <td colspan="6" class="tabla-vacia">No se encontraron clientes con esos criterios.</td>
            </tr>
        `;
    } else {
        cuerpo.innerHTML = lista.map(persona => `
        <tr class="${persona.activo ? "fila-activo" : "fila-inactivo"}">
            <td><span class="cli-cedula">${persona.identificacion}</span></td>
            <td>
                <div class="cli-persona">
                    <span class="cli-avatar">${persona.nombre.charAt(0).toUpperCase()}</span>
                    <div>
                        <span class="cli-nombre">${persona.nombre} ${persona.apellido}</span>
                        <span class="cli-telefono">📞 ${persona.telefono}</span>
                    </div>
                </div>
            </td>
            <td>${persona.correo}</td>
            <td><span class="cli-plan">${persona.planActual || "Sin plan"}</span></td>
            <td>
                <span class="badge ${persona.activo ? "badge-activo" : "badge-inactivo"}">
                    <span class="cli-punto"></span> ${persona.activo ? "Activo" : "Inactivo"}
                </span>
            </td>
            <td>
                <button class="btn-icono" aria-label="Editar cliente" onclick="abrirModal('${persona.identificacion}')" title="Editar">✏️</button>
                ${puedeAdministrarClientes() ? `<button class="btn-icono peligro" aria-label="Eliminar cliente" onclick="eliminarCliente('${persona.identificacion}')" title="Eliminar">🗑️</button>` : ""}
            </td>
        </tr>
        `).join("");
    }
}

// Rehace la lista visible a partir de clientesTodos (sin pedir nada al servidor).
// Se usa después de guardar o eliminar, para que la tabla refleje el cambio.
function refrescarListaClientesLocal() {
    const texto = document.getElementById("buscarCliente").value.trim();
    const estado = document.getElementById("filtroEstado").value;

    let lista = clientesTodos.slice();

    if (texto !== "") {
        lista = lista.filter(function (c) {
            const nombreCompleto = (c.nombre + " " + c.apellido).toLowerCase();
            return c.identificacion.includes(texto) || nombreCompleto.includes(texto.toLowerCase());
        });
    }
    if (estado === "activos") {
        lista = lista.filter(function (c) { return c.activo; });
    } else if (estado === "inactivos") {
        lista = lista.filter(function (c) { return !c.activo; });
    }

    clientesFiltrados = lista;
    mostrarPaginaSinSaltos();
}

// Dibuja sin que la página "salte": guarda y restaura la posición del scroll
function mostrarPaginaSinSaltos() {
    const area = document.querySelector(".content-area");
    const posicion = area ? area.scrollTop : 0;

    mostrarPagina();

    if (area) area.scrollTop = posicion;
}

function mostrarPagina() {
    // Se recuerda dónde estaba el scroll para que el repintado no lo mueva
    const areaScroll = document.querySelector(".content-area");
    const posicionScroll = areaScroll ? areaScroll.scrollTop : 0;
    const posicionLateral = areaScroll ? areaScroll.scrollLeft : 0;
    const scrollVentana = window.scrollY;

    const totalPaginas = Math.max(1, Math.ceil(clientesFiltrados.length / FILAS_POR_PAGINA));

    if (paginaActual > totalPaginas) {
        paginaActual = totalPaginas;
    }

    const desde = (paginaActual - 1) * FILAS_POR_PAGINA;
    const filaDeLaPagina = clientesFiltrados.slice(desde, desde + FILAS_POR_PAGINA);

    pintarTabla(filaDeLaPagina);
    pintarPaginacion(totalPaginas);

    document.getElementById("totalClientes").textContent = clientesFiltrados.length;

    // Chips de colores con el resumen
    const activos = clientesFiltrados.filter(c => c.activo).length;
    document.getElementById("chipTotal").textContent = clientesFiltrados.length;
    document.getElementById("chipActivos").textContent = activos;
    document.getElementById("chipInactivos").textContent = clientesFiltrados.length - activos;

    // Se devuelve el scroll a su sitio en varios momentos, porque el navegador
    // puede reajustarlo mientras recalcula la altura de la tabla.
    function fijarScroll() {
        if (areaScroll) {
            areaScroll.scrollTop = posicionScroll;
            areaScroll.scrollLeft = posicionLateral;
        }
        window.scrollTo(0, scrollVentana);
    }

    fijarScroll();

    requestAnimationFrame(function () {
        fijarScroll();
        requestAnimationFrame(fijarScroll);
    });

    setTimeout(fijarScroll, 120);
}

function irPagina(numero) {
    paginaActual = numero;
    mostrarPagina();
}

function pintarPaginacion(totalPaginas) {
    const contenedor = document.getElementById("paginacion");

    if (totalPaginas <= 1) {
        contenedor.innerHTML = "";
        return;
    }

    let html = "";

    html += `<button class="pagina-btn" onclick="irPagina(${paginaActual - 1})" ${paginaActual === 1 ? "disabled" : ""}>Anterior</button>`;

    for (let i = 1; i <= totalPaginas; i++) {
        html += `<button class="pagina-btn ${i === paginaActual ? "activa" : ""}" onclick="irPagina(${i})">${i}</button>`;
    }

    html += `<button class="pagina-btn" onclick="irPagina(${paginaActual + 1})" ${paginaActual === totalPaginas ? "disabled" : ""}>Siguiente</button>`;

    contenedor.innerHTML = html;
}

// --------------------------------------------------------------------------
// MENSAJES EMERGENTES (TOASTS)
// Muestran un aviso abajo a la derecha en vez de una ventana molesta.
// tipo: "exito" (verde), "error" (rojo) o "info" (azul)
// --------------------------------------------------------------------------
function notificar(mensaje, tipo) {
    let contenedor = document.getElementById("toastCliContenedor");

    if (!contenedor) {
        contenedor = document.createElement("div");
        contenedor.id = "toastCliContenedor";
        contenedor.className = "toast-cli-contenedor";
        document.body.appendChild(contenedor);
    }

    let icono = "✅";
    if (tipo === "error") icono = "⛔";
    if (tipo === "info") icono = "ℹ️";

    const toast = document.createElement("div");
    toast.className = "toast-cli " + (tipo || "exito");
    toast.innerHTML = '<span class="toast-cli-icono">' + icono + '</span>'
        + '<span class="toast-cli-texto">' + mensaje + '</span>'
        + '<button class="toast-cli-cerrar" aria-label="Cerrar">✕</button>';

    toast.querySelector(".toast-cli-cerrar").onclick = function () { cerrarToast(toast); };

    contenedor.appendChild(toast);
    setTimeout(function () { cerrarToast(toast); }, 4000);
}

function cerrarToast(toast) {
    toast.classList.add("saliendo");
    setTimeout(function () { toast.remove(); }, 200);
}

// Aviso cuando el servidor Java no está arrancado
function mostrarErrorServidor() {
    document.getElementById("tablaClientes").innerHTML = `
        <tr>
            <td colspan="6" class="tabla-vacia">
                No se pudo conectar con el servidor Java.<br>
                Arranca <strong>ApiServer</strong> y vuelve a intentarlo.
            </td>
        </tr>`;
    document.getElementById("paginacion").innerHTML = "";
    document.getElementById("totalClientes").textContent = "0";
    notificar("No se pudo conectar con el servidor Java.", "error");
}

// Listar clientes  ->  UsuarioController.listarClientes()
async function cargarClientes(mantenerPagina) {
    try {
        const respuesta = await fetch(API_CLIENTES);
        const json = await respuesta.json();
        clientesTodos = json.datos || [];
        clientesFiltrados = clientesTodos;

        // Al abrir la sección se vuelve a la página 1;
        // después de guardar o eliminar, se queda en la página donde estaba.
        if (mantenerPagina !== true) {
            paginaActual = 1;
        }

        mostrarPagina();

        // Se dejan los planes listos en memoria para que el modal abra sin esperar
        obtenerPlanesActivos();
    } catch (error) {
        mostrarErrorServidor();
    }
}

// Espera a que dejes de escribir (300 ms) antes de buscar
function alEscribirBusqueda() {
    clearTimeout(temporizadorBusqueda);
    temporizadorBusqueda = setTimeout(filtrarClientes, 300);
}

// Buscar y filtrar  ->  UsuarioController.buscarCliente()
//                      UsuarioController.listarClientesPorEstado()
async function filtrarClientes() {
    const texto = document.getElementById("buscarCliente").value.trim();
    const estado = document.getElementById("filtroEstado").value;

    try {
        // ---------- CASO 1: hay texto en el buscador ----------
        if (texto !== "") {
            // 1a. Coincidencias parciales con lo que ya tenemos cargado
            const parciales = clientesTodos.filter(c =>
                c.identificacion.includes(texto) ||
                (c.nombre + " " + c.apellido).toLowerCase().includes(texto.toLowerCase())
            );

            if (parciales.length > 0) {
                clientesFiltrados = parciales;
                paginaActual = 1;
                mostrarPagina();
                return;
            }

            // 1b. Si parece una cédula completa, se pregunta al servidor
            //     con el método buscarCliente(identificacion)
            if (texto.length >= 8) {
                const respuesta = await fetch(API_CLIENTES + "/" + encodeURIComponent(texto));
                const json = await respuesta.json();
                clientesFiltrados = (json.exito && json.datos) ? [json.datos] : [];
            } else {
                clientesFiltrados = [];
            }

            paginaActual = 1;
            mostrarPagina();
            return;
        }

        // ---------- CASO 2: sin texto, se filtra por estado ----------
        let url = API_CLIENTES;

        if (estado === "activos" || estado === "inactivos") {
            // listarClientesPorEstado(true / false)
            url = API_CLIENTES + "?estado=" + estado;
        }

        const respuesta = await fetch(url);
        const json = await respuesta.json();

        clientesFiltrados = json.datos || [];

        // Si se pidió la lista completa, se refresca también la copia local
        if (texto === "" && estado === "todos") {
            clientesTodos = json.datos || [];
        }

        paginaActual = 1;
        mostrarPagina();
    } catch (error) {
        mostrarErrorServidor();
    }
}

// --------------------------------------------------------------------------
// VENTANA DE CONFIRMACIÓN PROPIA
// Sustituye al confirm() del navegador (ese que dice "127.0.0.1:5500 dice").
// Devuelve una promesa: true si el usuario acepta, false si cancela.
// --------------------------------------------------------------------------
let respuestaConfirmacion = null;

function confirmarAccion(titulo, mensaje, textoAceptar) {
    document.getElementById("confirmaTitulo").textContent = titulo;
    document.getElementById("confirmaMensaje").textContent = mensaje;
    document.querySelector(".confirma-cli-peligro").textContent = textoAceptar;

    document.getElementById("modalConfirmar").style.display = "flex";

    return new Promise(function (resolver) {
        respuestaConfirmacion = resolver;
    });
}

function responderConfirmacion(acepto) {
    if (document.activeElement && document.activeElement.blur) {
        document.activeElement.blur();
    }
    document.getElementById("modalConfirmar").style.display = "none";

    if (respuestaConfirmacion) {
        respuestaConfirmacion(acepto);
        respuestaConfirmacion = null;
    }
}

// Eliminar  ->  UsuarioController.eliminarCliente()
async function eliminarCliente(identificacion) {
    const cliente = clientesFiltrados.find(p => p.identificacion === identificacion);
    const nombre = cliente ? cliente.nombre + " " + cliente.apellido : identificacion;

    const confirmado = await confirmarAccion(
        "Eliminar cliente",
        'Se eliminará a "' + nombre + '" (cédula ' + identificacion + '). Esta acción no se puede deshacer.',
        "Sí, eliminar"
    );

    if (!confirmado) return;

    try {
        const respuesta = await fetch(API_CLIENTES + "/" + encodeURIComponent(identificacion), {
            method: "DELETE"
        });
        const json = await respuesta.json();

        if (!json.exito) {
            notificar(json.mensaje, "error");
            return;
        }

        notificar("Cliente " + nombre + " eliminado correctamente.", "exito");

        // Se vuelve a pedir la lista, manteniéndose en la misma página
        cargarClientes(true);
    } catch (error) {
        mostrarErrorServidor();
    }
}

// ==========================================================================
// 8. MODAL DE CLIENTES (SIRVE PARA REGISTRAR Y PARA ACTUALIZAR)
// ==========================================================================
// Guarda en qué modo está abierto el formulario: "nuevo" o "editar".
// Se usa para no sobrescribir un cliente existente por accidente.
let modoFormulario = "nuevo";

// Llena el desplegable de planes con los planes ACTIVOS del servidor.
// Si el cliente tiene un plan que ya está inactivo, se conserva (marcado)
// para no perderlo sin querer.
// Los planes activos se guardan en memoria un rato, para que el modal
// abra al instante (antes se pedían al servidor cada vez que se abría).
let planesActivosCache = null;
let planesActivosCacheHora = 0;
const PLANES_CACHE_MS = 300000;   // 5 minutos

async function obtenerPlanesActivos() {
    const ahora = Date.now();

    if (planesActivosCache && (ahora - planesActivosCacheHora) < PLANES_CACHE_MS) {
        return planesActivosCache;
    }

    try {
        const respuesta = await fetch(API_PLANES_CATALOGO + "?estado=activos");
        const json = await respuesta.json();
        planesActivosCache = json.datos || [];
        planesActivosCacheHora = ahora;
        return planesActivosCache;
    } catch (error) {
        // Si falla, se devuelve lo que hubiera en memoria (aunque esté viejo)
        return planesActivosCache || [];
    }
}

// Dibuja las opciones del desplegable
function pintarOpcionesDePlan(planes, planActual) {
    const select = document.getElementById("editPlan");
    if (!select) return;

    select.innerHTML = "";

    planes.forEach(function (plan) {
        const opcion = document.createElement("option");
        opcion.value = plan.nombre;
        opcion.textContent = plan.nombre + " — " + plan.velocidad;
        select.appendChild(opcion);
    });

    // El plan que ya tiene el cliente, si está inactivo, se agrega igual
    if (planActual && !planes.some(function (p) { return p.nombre === planActual; })) {
        const opcion = document.createElement("option");
        opcion.value = planActual;
        opcion.textContent = planActual + " (inactivo)";
        select.appendChild(opcion);
    }

    if (planActual) {
        select.value = planActual;
    }
}

async function cargarPlanesEnSelect(planActual) {
    const select = document.getElementById("editPlan");
    if (!select) return;

    // 1) Se dibuja al instante con lo que ya está en memoria (sin esperar a la red)
    pintarOpcionesDePlan(planesActivosCache || [], planActual);

    // 2) Y por detrás se refrescan los planes, por si alguien los cambió
    const planes = await obtenerPlanesActivos();

    if (planes.length > 0) {
        pintarOpcionesDePlan(planes, planActual);
    }
}

async function abrirModal(identificacion) {
    modoFormulario = "editar";

    // El cliente ya viene del servidor Java (está en la tabla que se ve)
    const cliente = clientesFiltrados.find(p => p.identificacion === identificacion);

    if (!cliente) {
        notificar("No se encontró el cliente en la lista.", "error");
        return;
    }

    document.getElementById("modalTitulo").textContent = "✏️ Actualizar Cliente";

    document.getElementById("editIdentificacion").value = cliente.identificacion;
    document.getElementById("editIdentificacion").readOnly = true;
    document.getElementById("editNombre").value = cliente.nombre;
    document.getElementById("editApellido").value = cliente.apellido;
    document.getElementById("editCorreo").value = cliente.correo;
    document.getElementById("editTelefono").value = cliente.telefono;
    await cargarPlanesEnSelect(cliente.planActual);
    document.getElementById("editEstado").value = cliente.activo ? "Activo" : "Inactivo";

    document.getElementById("modalCliente").style.display = "flex";
}

async function abrirModalNuevo() {
    modoFormulario = "nuevo";

    document.getElementById("modalTitulo").textContent = "➕ Registrar Nuevo Cliente";

    document.getElementById("editIdentificacion").value = "";
    document.getElementById("editIdentificacion").readOnly = false;
    document.getElementById("editNombre").value = "";
    document.getElementById("editApellido").value = "";
    document.getElementById("editCorreo").value = "";
    document.getElementById("editTelefono").value = "";
    await cargarPlanesEnSelect(null);
    document.getElementById("editEstado").value = "Activo";

    document.getElementById("modalCliente").style.display = "flex";
}

function cerrarModal() {
    // Se quita el foco ANTES de ocultar: si el botón pulsado sigue enfocado,
    // el navegador desplaza la vista al desaparecer (el "salto" al guardar).
    if (document.activeElement && document.activeElement.blur) {
        document.activeElement.blur();
    }
    document.getElementById("modalCliente").style.display = "none";
}

// Red de seguridad: si algún formulario intenta enviarse (y recargar la
// página), se cancela. Nuestro módulo trabaja con fetch, no con formularios.
document.addEventListener("submit", function (evento) {
    evento.preventDefault();
});

// Deja solo dígitos en un campo (para cédula y teléfono)
function soloNumeros(campo) {
    campo.value = campo.value.replace(/[^0-9]/g, "");
}

function guardarCliente() {
    try {
        guardarClienteInterno();
    } catch (error) {
        console.error("Error al guardar el cliente:", error);
        notificar("Error al guardar: " + error.message, "error");
    }
}

function guardarClienteInterno() {
    const valor = (id) => document.getElementById(id).value.trim();

    const identificacion = valor("editIdentificacion");
    const nombre = valor("editNombre");
    const apellido = valor("editApellido");
    const correo = valor("editCorreo");
    const telefono = valor("editTelefono");

    // ---------------- VALIDACIONES (antes de llamar al servidor) ----------------
    // 1. Que ningún campo quede vacío
    if (identificacion === "" || nombre === "" || apellido === "" || correo === "" || telefono === "") {
        notificar("Todos los campos son obligatorios. Completa la información.", "error");
        return;
    }

    // 2. Formatos
    if (!/^\d{8,10}$/.test(identificacion)) {
        notificar("La cédula debe tener entre 8 y 10 dígitos numéricos.", "error");
        return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
        notificar("El correo no es válido. Ejemplo: nombre@dominio.com", "error");
        return;
    }
    if (!/^\d{10}$/.test(telefono)) {
        notificar("El teléfono debe tener exactamente 10 dígitos.", "error");
        return;
    }


    // Lo que se envía al servidor Java
    const datos = {
        identificacion: identificacion,
        nombre: nombre,
        apellido: apellido,
        tipo: "CC",
        telefono: telefono,
        correo: correo,
        direccion: "Sin dirección",
        contrasena: "Clave123",
        planActual: document.getElementById("editPlan").value,
        activo: document.getElementById("editEstado").value === "Activo" ? "true" : "false"
    };

    enviarCliente(datos);
}

// Registrar  ->  UsuarioController.registrarCliente()
// Actualizar ->  UsuarioController.actualizarCliente()
async function enviarCliente(datos) {
    const esNuevo = (modoFormulario === "nuevo");

    const url = esNuevo
        ? API_CLIENTES
        : API_CLIENTES + "/" + encodeURIComponent(datos.identificacion);

    const metodo = esNuevo ? "POST" : "PUT";

    try {
        const respuesta = await fetch(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datos)
        });

        const json = await respuesta.json();

        // Si el backend rechaza algo, se muestra SU mensaje
        if (!json.exito) {
            notificar(json.mensaje, "error");
            return;
        }

        cerrarModal();
        notificar(esNuevo ? "Cliente registrado correctamente." : "Cliente actualizado correctamente.", "exito");

        // Se vuelve a pedir la lista, manteniéndose en la misma página
        cargarClientes(true);

    } catch (error) {
        notificar("No se pudo conectar con el servidor Java. Arranca ApiServer.", "error");
    }
}



// --------------------------------------------------------------------------
// BOTONES DE RECARGAR
// Vuelven a pedir la lista al servidor (por si alguien más cambió los datos)
// --------------------------------------------------------------------------
async function recargarClientes(boton) {
    if (boton) boton.classList.add("girando");

    await cargarClientes();

    if (boton) boton.classList.remove("girando");
    notificar("Lista de clientes actualizada.", "info");
}

async function recargarEmpleados(boton) {
    if (boton) boton.classList.add("girando");

    await cargarEmpleados();

    if (boton) boton.classList.remove("girando");
    notificar("Lista de empleados actualizada.", "info");
}

// ==========================================================================
// 9. GESTIÓN DE EMPLEADOS (ISSUE #13)
// --------------------------------------------------------------------------
// Igual que clientes: la página no guarda nada, todo se pide al servidor Java
// (ApiServer -> EmpleadoController -> EmpleadoModel -> ListaEmpleados).
// Los nombres llevan prefijo EMP_ para no chocar con los de otros módulos.
// ==========================================================================
const EMP_API = "http://localhost:8080/api/empleados";
const EMP_FILAS_POR_PAGINA = 4;   // 4 por página: la tabla no se hace eterna

let empTodos = [];
let empFiltrados = [];
let empPagina = 1;
let empModo = "nuevo";          // "nuevo" o "editar"
let empTemporizador = null;

// ---------- LISTAR ----------
async function cargarEmpleados(mantenerPagina) {
    try {
        const respuesta = await fetch(EMP_API);
        const json = await respuesta.json();
        empTodos = json.datos || [];
        empFiltrados = empTodos;

        // Al abrir la sección se vuelve a la página 1;
        // después de guardar, cambiar estado o eliminar, se queda en la página donde estaba.
        if (mantenerPagina !== true) {
            empPagina = 1;
        }

        empAplicarFiltro(mantenerPagina);
    } catch (error) {
        empErrorServidor();
    }
}

// ---------- FILTRAR (en el navegador, sobre lo ya cargado) ----------
function filtrarEmpleados() {
    clearTimeout(empTemporizador);
    empTemporizador = setTimeout(function () { empAplicarFiltro(false); }, 250);
}

function empAplicarFiltro(mantenerPagina) {
    const texto = document.getElementById("buscarEmpleado").value.trim().toLowerCase();
    const estado = document.getElementById("filtroEstadoEmpleado").value;

    let lista = empTodos.slice();

    if (texto !== "") {
        lista = lista.filter(function (e) {
            const nombreCompleto = (e.nombre + " " + e.apellido).toLowerCase();
            return e.identificacion.includes(texto) || nombreCompleto.includes(texto);
        });
    }

    if (estado === "activos") {
        lista = lista.filter(function (e) { return e.activo; });
    } else if (estado === "inactivos") {
        lista = lista.filter(function (e) { return !e.activo; });
    }

    empFiltrados = lista;
    if (mantenerPagina !== true) {
        empPagina = 1;
    }
    empMostrarPagina();
}

// ---------- DIBUJAR ----------
function empMostrarPagina() {
    const totalPaginas = Math.max(1, Math.ceil(empFiltrados.length / EMP_FILAS_POR_PAGINA));

    if (empPagina > totalPaginas) empPagina = totalPaginas;

    const desde = (empPagina - 1) * EMP_FILAS_POR_PAGINA;
    const fila = empFiltrados.slice(desde, desde + EMP_FILAS_POR_PAGINA);

    const cuerpo = document.getElementById("tablaEmpleados");

    if (fila.length === 0) {
        cuerpo.innerHTML = '<tr><td colspan="7" class="tabla-vacia">No se encontraron empleados con esos criterios.</td></tr>';
    } else {
        cuerpo.innerHTML = fila.map(function (e) {
            return `
        <tr class="${e.activo ? "fila-activo" : "fila-inactivo"}">
            <td><span class="cli-cedula">${e.identificacion}</span></td>
            <td>
                <div class="cli-persona">
                    <span class="cli-avatar">${e.nombre.charAt(0).toUpperCase()}</span>
                    <div>
                        <span class="cli-nombre">${e.nombre} ${e.apellido}</span>
                        <span class="cli-telefono">📞 ${e.telefono}</span>
                    </div>
                </div>
            </td>
            <td>${e.correo}</td>
            <td>
                <span class="cli-plan">${e.cargo}</span>
                ${e.rol === "ADMIN" ? ' <span class="badge badge-admin">ADMIN</span>' : ""}
            </td>
            <td>${e.sucursal}</td>
            <td>
                <span class="badge ${e.activo ? "badge-activo" : "badge-inactivo"}">
                    <span class="cli-punto"></span> ${e.activo ? "Activo" : "Inactivo"}
                </span>
            </td>
            <td>
                <button class="btn-icono" aria-label="Editar empleado" onclick="abrirModalEmpleado('${e.identificacion}')" title="Editar">✏️</button>
                <button class="btn-icono peligro" aria-label="${e.activo ? "Desactivar" : "Activar"} empleado" onclick="cambiarEstadoEmpleado('${e.identificacion}')" title="${e.activo ? "Desactivar" : "Activar"}">${e.activo ? "⏻" : "▶"}</button>
                ${puedeEliminarEmpleados() ? `<button class="btn-icono peligro" aria-label="Eliminar empleado" onclick="eliminarEmpleado('${e.identificacion}')" title="Eliminar empleado">🗑️</button>` : ""}
            </td>
        </tr>`;
        }).join("");
    }

    empPintarPaginacion(totalPaginas);

    document.getElementById("empTotal").textContent = empFiltrados.length;

    const activos = empFiltrados.filter(function (e) { return e.activo; }).length;
    document.getElementById("empChipTotal").textContent = empFiltrados.length;
    document.getElementById("empChipActivos").textContent = activos;
    document.getElementById("empChipInactivos").textContent = empFiltrados.length - activos;
}

function empPintarPaginacion(totalPaginas) {
    const contenedor = document.getElementById("empPaginacion");

    if (totalPaginas <= 1) {
        contenedor.innerHTML = "";
        return;
    }

    let html = `<button class="pagina-btn" onclick="empIrPagina(${empPagina - 1})" ${empPagina === 1 ? "disabled" : ""}>Anterior</button>`;

    for (let i = 1; i <= totalPaginas; i++) {
        html += `<button class="pagina-btn ${i === empPagina ? "activa" : ""}" onclick="empIrPagina(${i})">${i}</button>`;
    }

    html += `<button class="pagina-btn" onclick="empIrPagina(${empPagina + 1})" ${empPagina === totalPaginas ? "disabled" : ""}>Siguiente</button>`;

    contenedor.innerHTML = html;
}

function empIrPagina(numero) {
    empPagina = numero;
    empMostrarPagina();
}

function empErrorServidor() {
    const cuerpo = document.getElementById("tablaEmpleados");
    if (cuerpo) {
        cuerpo.innerHTML = `<tr><td colspan="7" class="tabla-vacia">
            No se pudo conectar con el servidor Java.<br>Arranca <strong>ApiServer</strong> y vuelve a intentarlo.
        </td></tr>`;
    }
    notificar("No se pudo conectar con el servidor Java.", "error");
}

// ---------- MODAL ----------
// Si el SUPERADMIN elige "Administrador", se oculta la lista de cargos de empleado
// y queda como "Administrador de sucursal". Si elige "Empleado", escoge su cargo.
function alCambiarRolEmpleado() {
    const selectRol = document.getElementById("empRol");
    const grupoCargo = document.getElementById("grupoEmpCargo");
    if (!selectRol || !grupoCargo) return;

    if (selectRol.value === "ADMIN") {
        grupoCargo.style.display = "none";
    } else {
        grupoCargo.style.display = "block";
    }
}

async function abrirModalEmpleado(identificacion) {
    empModo = "editar";

    const empleado = empTodos.find(function (e) { return e.identificacion === identificacion; });

    if (!empleado) {
        notificar("No se encontró el empleado en la lista.", "error");
        return;
    }

    document.getElementById("modalEmpleadoTitulo").textContent = "✏️ Actualizar Empleado";

    document.getElementById("empIdentificacion").value = empleado.identificacion;
    document.getElementById("empIdentificacion").readOnly = true;
    document.getElementById("empNombre").value = empleado.nombre;
    document.getElementById("empApellido").value = empleado.apellido;
    document.getElementById("empCorreo").value = empleado.correo;
    document.getElementById("empTelefono").value = empleado.telefono;
    document.getElementById("empCargo").value = empleado.cargo;
    document.getElementById("empSucursal").value = empleado.sucursal;
    document.getElementById("empEstado").value = empleado.activo ? "Activo" : "Inactivo";

    // Solo el SUPERADMIN puede elegir si es ADMIN o EMPLEADO
    const filaRol = document.getElementById("filaEmpRol");
    if (filaRol) {
        filaRol.style.display = esSuperAdminActual() ? "block" : "none";
        document.getElementById("empRol").value = (empleado.rol === "ADMIN") ? "ADMIN" : "EMPLEADO";
        alCambiarRolEmpleado();
    }

    document.getElementById("modalEmpleado").style.display = "flex";
}

function abrirModalEmpleadoNuevo() {
    empModo = "nuevo";

    document.getElementById("modalEmpleadoTitulo").textContent = "➕ Registrar Nuevo Empleado";

    document.getElementById("empIdentificacion").value = "";
    document.getElementById("empIdentificacion").readOnly = false;
    document.getElementById("empNombre").value = "";
    document.getElementById("empApellido").value = "";
    document.getElementById("empCorreo").value = "";
    document.getElementById("empTelefono").value = "";
    document.getElementById("empCargo").selectedIndex = 0;
    document.getElementById("empSucursal").selectedIndex = 0;
    document.getElementById("empEstado").value = "Activo";

    // Solo el SUPERADMIN puede elegir si el nuevo empleado será ADMIN o EMPLEADO
    const filaRol = document.getElementById("filaEmpRol");
    if (filaRol) {
        filaRol.style.display = esSuperAdminActual() ? "block" : "none";
        document.getElementById("empRol").value = "EMPLEADO";
        alCambiarRolEmpleado();
    }

    document.getElementById("modalEmpleado").style.display = "flex";
}

function cerrarModalEmpleado() {
    if (document.activeElement && document.activeElement.blur) {
        document.activeElement.blur();
    }
    document.getElementById("modalEmpleado").style.display = "none";
}

// ---------- GUARDAR ----------
function guardarEmpleado() {
    const valor = function (id) { return document.getElementById(id).value.trim(); };

    const identificacion = valor("empIdentificacion");
    const nombre = valor("empNombre");
    const apellido = valor("empApellido");
    const correo = valor("empCorreo");
    const telefono = valor("empTelefono");

    if (identificacion === "" || nombre === "" || apellido === "" || correo === "" || telefono === "") {
        notificar("Todos los campos son obligatorios. Completa la información.", "error");
        return;
    }

    if (!/^\d{8,10}$/.test(identificacion)) {
        notificar("La cédula debe tener entre 8 y 10 dígitos numéricos.", "error");
        return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
        notificar("El correo no es válido. Ejemplo: nombre@dominio.com", "error");
        return;
    }
    if (!/^\d{10}$/.test(telefono)) {
        notificar("El teléfono debe tener exactamente 10 dígitos.", "error");
        return;
    }

    const rolElegido = esSuperAdminActual() ? document.getElementById("empRol").value : "EMPLEADO";
    const cargoElegido = (rolElegido === "ADMIN")
        ? "Administrador de sucursal"
        : document.getElementById("empCargo").value;

    const datos = {
        identificacion: identificacion,
        nombre: nombre,
        apellido: apellido,
        tipo: "CC",
        telefono: telefono,
        correo: correo,
        direccion: "Sin dirección",
        cargo: cargoElegido,
        sucursal: document.getElementById("empSucursal").value,
        rol: rolElegido,
        activo: document.getElementById("empEstado").value === "Activo" ? "true" : "false"
    };

    empEnviar(datos);
}

async function empEnviar(datos) {
    const esNuevo = (empModo === "nuevo");

    const url = esNuevo ? EMP_API : EMP_API + "/" + encodeURIComponent(datos.identificacion);
    const metodo = esNuevo ? "POST" : "PUT";

    try {
        const respuesta = await fetch(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datos)
        });

        const json = await respuesta.json();

        if (!json.exito) {
            notificar(json.mensaje, "error");
            return;
        }

        cerrarModalEmpleado();
        notificar(esNuevo ? "Empleado registrado correctamente." : "Empleado actualizado correctamente.", "exito");
        cargarEmpleados(true);

    } catch (error) {
        notificar("No se pudo conectar con el servidor Java. Arranca ApiServer.", "error");
    }
}

// ---------- ELIMINAR (solo SUPERADMIN) ----------
async function eliminarEmpleado(identificacion) {
    if (!puedeEliminarEmpleados()) {
        notificar("Solo un administrador puede eliminar empleados.", "error");
        return;
    }

    const empleado = empTodos.find(function (e) { return e.identificacion === identificacion; });
    if (!empleado) return;

    const nombre = empleado.nombre + " " + empleado.apellido;

    const confirmado = await confirmarAccion(
        "Eliminar empleado",
        'Se eliminará definitivamente a "' + nombre + '" del sistema. Esta acción no se puede deshacer.',
        "Sí, eliminar"
    );

    if (!confirmado) return;

    try {
        const respuesta = await fetch(EMP_API + "/" + encodeURIComponent(identificacion), {
            method: "DELETE"
        });
        const json = await respuesta.json();

        if (!json.exito) {
            notificar(json.mensaje, "error");
            return;
        }

        notificar(json.mensaje, "exito");
        cargarEmpleados(true);

    } catch (error) {
        notificar("No se pudo conectar con el servidor Java.", "error");
    }
}

// ---------- ACTIVAR / DESACTIVAR ----------
async function cambiarEstadoEmpleado(identificacion) {
    const empleado = empTodos.find(function (e) { return e.identificacion === identificacion; });
    if (!empleado) return;

    const nuevoEstado = !empleado.activo;
    const nombre = empleado.nombre + " " + empleado.apellido;

    const confirmado = await confirmarAccion(
        nuevoEstado ? "Activar empleado" : "Desactivar empleado",
        (nuevoEstado ? "Se reactivará" : "Se suspenderá") + ' a "' + nombre + '". No se elimina su historial.',
        nuevoEstado ? "Sí, activar" : "Sí, desactivar"
    );

    if (!confirmado) return;

    try {
        const respuesta = await fetch(EMP_API + "/" + encodeURIComponent(identificacion) + "/estado", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ activo: String(nuevoEstado) })
        });

        const json = await respuesta.json();

        if (!json.exito) {
            notificar(json.mensaje, "error");
            return;
        }

        notificar(json.mensaje, "exito");
        cargarEmpleados(true);

    } catch (error) {
        notificar("No se pudo conectar con el servidor Java.", "error");
    }
}


// ==========================================================================
// 10. MI PERFIL (SOLO ROL CLIENTE)
// --------------------------------------------------------------------------
// El cliente no ve la lista de clientes: ve y edita SOLO sus propios datos.
// Se lee con UsuarioController.buscarCliente() y se guarda con
// UsuarioController.actualizarCliente().
// ==========================================================================
async function cargarPerfil() {
    const sesion = sesionActual();
    if (!sesion) return;

    try {
        const respuesta = await fetch(API_CLIENTES + "/" + encodeURIComponent(sesion.identificacion));
        const json = await respuesta.json();

        if (!json.exito || !json.datos) {
            notificar(json.mensaje || "No se pudieron cargar tus datos.", "error");
            return;
        }

        const cliente = json.datos;

        document.getElementById("perfilIdentificacion").value = cliente.identificacion;
        document.getElementById("perfilNombre").value = cliente.nombre;
        document.getElementById("perfilApellido").value = cliente.apellido;
        document.getElementById("perfilCorreo").value = cliente.correo;
        document.getElementById("perfilTelefono").value = cliente.telefono;
        document.getElementById("perfilDireccion").value = cliente.direccion === "Sin dirección" ? "" : cliente.direccion;

    } catch (error) {
        notificar("No se pudo conectar con el servidor Java.", "error");
    }
}

async function guardarPerfil() {
    const sesion = sesionActual();
    if (!sesion) return;

    const valor = function (id) { return document.getElementById(id).value.trim(); };

    const nombre = valor("perfilNombre");
    const apellido = valor("perfilApellido");
    const correo = valor("perfilCorreo");
    const telefono = valor("perfilTelefono");
    const direccion = valor("perfilDireccion");
    const contrasena = document.getElementById("perfilContrasena").value;
    const contrasena2 = document.getElementById("perfilContrasena2").value;

    if (nombre === "" || apellido === "" || correo === "" || telefono === "") {
        notificar("El nombre, apellido, correo y teléfono son obligatorios.", "error");
        return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
        notificar("El correo no es válido. Ejemplo: nombre@dominio.com", "error");
        return;
    }
    if (!/^\d{10}$/.test(telefono)) {
        notificar("El teléfono debe tener exactamente 10 dígitos.", "error");
        return;
    }

    // Contraseña: solo si el cliente escribió una nueva
    if (contrasena !== "" || contrasena2 !== "") {
        if (contrasena.length < 6) {
            notificar("La contraseña debe tener mínimo 6 caracteres.", "error");
            return;
        }
        if (!/[A-Za-z]/.test(contrasena) || !/\d/.test(contrasena)) {
            notificar("La contraseña debe incluir letras y números.", "error");
            return;
        }
        if (contrasena !== contrasena2) {
            notificar("Las contraseñas no coinciden.", "error");
            return;
        }
    }


    try {
        // UsuarioController.actualizarCliente(identificacion, nombre, apellido, tipo,
        //                                     telefono, correo, direccion, contrasena, plan)
        const respuesta = await fetch(API_CLIENTES + "/" + encodeURIComponent(sesion.identificacion), {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                nombre: nombre,
                apellido: apellido,
                tipo: "CC",
                telefono: telefono,
                correo: correo,
                direccion: direccion === "" ? "Sin dirección" : direccion,
                contrasena: contrasena          // vacía = se conserva la actual
            })
        });

        const json = await respuesta.json();

        if (!json.exito) {
            notificar(json.mensaje, "error");
            return;
        }

        // Se actualiza también la sesión del navegador
        sesion.nombre = nombre;
        sesion.apellido = apellido;
        sesion.correo = correo;
        sesion.telefono = telefono;
        localStorage.setItem("sesionActiva", JSON.stringify(sesion));

        notificar(contrasena !== ""
            ? "Tus datos y tu contraseña se actualizaron correctamente."
            : "Tus datos se actualizaron correctamente.", "exito");

        // Se limpian los campos de contraseña
        document.getElementById("perfilContrasena").value = "";
        document.getElementById("perfilContrasena2").value = "";
        cargarDashboard();

    } catch (error) {
        notificar("No se pudo conectar con el servidor Java.", "error");
    }
}
