/* ==========================================================================
   LÓGICA JAVASCRIPT - WINET CONTROL (HTML & CSS PARA PRINCIPIANTES)
   Autor: Alejandro Escudero
   Explicación: Este archivo maneja la autenticación por roles (Superusuario,
   Administrador Local y Cliente), registro de clientes, permisos en el 
   dashboard y cierre de sesión.
   ========================================================================== */

// 1. CUENTAS INICIALES POR ROLES DEL SISTEMA
const usuariosIniciales = [
    {
        identificacion: "9999999999",
        usuario: "superadmin",
        contrasena: "Super123!",
        nombre: "Super Administrador",
        apellido: "Global",
        tipo: "CC",
        telefono: "3009999999",
        correo: "superadmin@winetcontrol.com",
        direccion: "Sede Central Corporativa WiNet",
        rol: "SUPERADMIN",
        sucursal: "Sede Central (Acceso Global)",
        planActual: "Acceso Corporativo Total",
        activo: true // ACTIVO
    },
    {
        identificacion: "1000000001",
        usuario: "admin123",
        contrasena: "Admin123!",
        nombre: "Administrador",
        apellido: "Local",
        tipo: "CC",
        telefono: "3008888888",
        correo: "admin.local@winetcontrol.com",
        direccion: "Sucursal Local Norte # 45-20",
        rol: "ADMIN",
        sucursal: "Sucursal Local Norte",
        planActual: "Administración Local",
        activo: true // ACTIVO
    },
    {
        identificacion: "1045678901",
        usuario: "1045678901",
        contrasena: "Clave123",
        nombre: "Alejandro",
        apellido: "Escudero",
        tipo: "CC",
        telefono: "3001234567",
        correo: "alejandrocastellar04@gmail.com",
        direccion: "Calle 10 # 20-30",
        rol: "CLIENTE",
        sucursal: "Residencial Norte",
        planActual: "Plan Fibra Gamer 300M",
        activo: true // ACTIVO
    },
    {
        identificacion: "1098765432",
        usuario: "1098765432",
        contrasena: "Clave456",
        nombre: "María",
        apellido: "Gómez",
        tipo: "CC",
        telefono: "3109876543",
        correo: "maria.gomez@email.com",
        direccion: "Carrera 15 # 45-10",
        rol: "CLIENTE",
        sucursal: "Residencial Centro",
        planActual: "Plan Básico 50M",
        activo: false // INACTIVO: Para probar que el login bloquea clientes inactivos
    }
];

// Inicializar / actualizar usuarios en localStorage asegurando que los roles existan
(function inicializarAlmacenamiento() {
    const guardados = localStorage.getItem("usuariosWiNet");
    if (!guardados) {
        localStorage.setItem("usuariosWiNet", JSON.stringify(usuariosIniciales));
    } else {
        // Aseguramos que superadmin y admin123 siempre estén presentes
        const lista = JSON.parse(guardados);
        let huboCambios = false;
        usuariosIniciales.forEach(defecto => {
            const existe = lista.some(u => 
                u.identificacion === defecto.identificacion || 
                (u.usuario && defecto.usuario && u.usuario.toLowerCase() === defecto.usuario.toLowerCase())
            );
            if (!existe) {
                lista.push(defecto);
                huboCambios = true;
            }
        });
        if (huboCambios) {
            localStorage.setItem("usuariosWiNet", JSON.stringify(lista));
        }
    }
})();

// ==========================================================================
// 2. FUNCIÓN DE INICIO DE SESIÓN (LOGIN UNIVERSAL CON ROLES)
// ==========================================================================
function procesarLogin(evento) {
    evento.preventDefault();

    const idInput = document.getElementById("loginIdentificacion").value.trim();
    const claveInput = document.getElementById("loginContrasena").value.trim();
    const alertBox = document.getElementById("loginAlert");

    // Limpiar alertas previas
    alertBox.style.display = "none";
    alertBox.className = "alert";

    if (idInput === "" || claveInput === "") {
        mostrarAlerta(alertBox, "Por favor complete todos los campos requeridos.", "error");
        return;
    }

    const listaUsuarios = JSON.parse(localStorage.getItem("usuariosWiNet")) || usuariosIniciales;

    // Buscar si existe un usuario que coincida por cédula o por alias de usuario
    const usuarioEncontrado = listaUsuarios.find(u => {
        const coincideId = u.identificacion === idInput;
        const coincideUsuario = u.usuario && (u.usuario.toLowerCase() === idInput.toLowerCase());
        return (coincideId || coincideUsuario) && u.contrasena === claveInput;
    });

    if (!usuarioEncontrado) {
        mostrarAlerta(alertBox, "Usuario/identificación o contraseña incorrectos.", "error");
        return;
    }

    // Regla de negocio: Validar estado ACTIVO / INACTIVO
    if (!usuarioEncontrado.activo) {
        mostrarAlerta(alertBox, "La cuenta se encuentra INACTIVA. Comuníquese con la administración para reactivarla.", "error");
        return;
    }

    // Guardar sesión activa con rol en el almacenamiento del navegador
    localStorage.setItem("sesionActiva", JSON.stringify(usuarioEncontrado));

    const textoRol = usuarioEncontrado.rol === "SUPERADMIN" ? "Superusuario" :
                     usuarioEncontrado.rol === "ADMIN" ? "Administrador Local" : "Cliente";

    mostrarAlerta(alertBox, "¡Bienvenido, " + usuarioEncontrado.nombre + " (" + textoRol + ")! Redirigiendo...", "success");

    setTimeout(() => {
        window.location.href = "dashboard.html";
    }, 900);
}

// ==========================================================================
// 3. REGISTRO DE NUEVOS CLIENTES
// ==========================================================================
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

    const listaUsuarios = JSON.parse(localStorage.getItem("usuariosWiNet")) || [];

    const yaExiste = listaUsuarios.some(u => u.identificacion === identificacion || u.correo === correo);
    if (yaExiste) {
        mostrarAlerta(alertBox, "Ya existe un usuario registrado con esa identificación o correo electrónico.", "error");
        return;
    }

    // Nuevo cliente registrado con rol CLIENTE y estado ACTIVO
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
        planActual: "Plan Básico 50M",
        activo: true
    };

    listaUsuarios.push(nuevoCliente);
    localStorage.setItem("usuariosWiNet", JSON.stringify(listaUsuarios));

    mostrarAlerta(alertBox, "¡Cuenta creada exitosamente! Redirigiendo al inicio de sesión...", "success");

    setTimeout(() => {
        window.location.href = "login.html";
    }, 1400);
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
    window.location.href = "login.html";
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
