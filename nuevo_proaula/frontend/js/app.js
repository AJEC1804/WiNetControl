const API_LOGIN = API_BASE + "/api/login";

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
        if (json.empresaSuspendida) {
            sessionStorage.setItem("empresaSuspendidaInfo", JSON.stringify(json));
            window.location.href = "empresa-suspendida.html";
            return;
        }
        mostrarAlerta(alertBox, json.mensaje, "error");
        return;
    }

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

    if (!nombres || !apellidos || !identificacion || !telefono || !correo || !direccion || !contrasena) {
        mostrarAlerta(alertBox, "Todos los campos marcados son obligatorios.", "error");
        return;
    }

    if (!/^\d{8,10}$/.test(identificacion)) {
        mostrarAlerta(alertBox, "La identificación debe tener entre 8 y 10 dígitos numéricos.", "error");
        return;
    }

    if (!/^\d{10}$/.test(telefono)) {
        mostrarAlerta(alertBox, "El teléfono celular debe contener exactamente 10 dígitos.", "error");
        return;
    }

    if (contrasena.length < 6 || !/[A-Za-z]/.test(contrasena) || !/\d/.test(contrasena)) {
        mostrarAlerta(alertBox, "La contraseña debe tener mínimo 6 caracteres e incluir letras y números.", "error");
        return;
    }

    if (contrasena !== confContrasena) {
        mostrarAlerta(alertBox, "Las contraseñas no coinciden.", "error");
        return;
    }

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

function cargarDashboard() {
    const sesion = localStorage.getItem("sesionActiva");

    if (!sesion) {
        window.location.href = "login.html";
        return;
    }

    const usuario = JSON.parse(sesion);
    const nombreCompleto = (usuario.nombre || usuario.nombres) + " " + (usuario.apellido || usuario.apellidos);

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

    const badgeSucursal = document.getElementById("dashSucursalBadge");
    if (badgeSucursal) {
        badgeSucursal.textContent = usuario.sucursal || "Sede WiNet";
    }

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

    renderizarEstadisticasPorRol(usuario);

    adaptarVistasPorRol(usuario);
}

async function renderizarEstadisticasPorRol(usuario) {
    pintarBienvenida(usuario);

    const contenedor = document.getElementById("dashStatsGrid");
    if (!contenedor) return;

    contenedor.innerHTML = tarjetaCargando("Consultando datos…");

    const datos = await reunirDatosInicio(usuario);
    if (datos.esCliente && datos.clientes && datos.clientes.datos) {
        const cFresh = datos.clientes.datos;
        usuario.activo = cFresh.activo;
        if (cFresh.planActual !== undefined) usuario.planActual = cFresh.planActual;
        if (cFresh.direccion !== undefined) usuario.direccion = cFresh.direccion;
        try {
            localStorage.setItem("sesionActiva", JSON.stringify(usuario));
        } catch (e) {

        }
    }
    const tarjetas = tarjetasSegunRol(usuario, datos);
    contenedor.innerHTML = tarjetas.map(pintarTarjeta).join("");

    contenedor.setAttribute("data-cantidad", String(tarjetas.length));

    const sello = document.getElementById("kpiActualizado");
    if (sello) {
        sello.textContent = "actualizado " + new Date().toLocaleTimeString("es-CO", { hour: "2-digit", minute: "2-digit" });
    }

}

function pintarBienvenida(usuario) {
    const hora = new Date().getHours();
    let saludo = "Buenas noches";
    let icono = "🌙";
    if (hora < 12) { saludo = "Buenos días"; icono = "☀️"; }
    else if (hora < 19) { saludo = "Buenas tardes"; icono = "🌤️"; }

    const nombre = ((usuario.nombre || "") + " " + (usuario.apellido || "")).trim() || "de nuevo";
    const saludoEl = document.getElementById("inicioSaludo");
    if (saludoEl) {
        saludoEl.textContent = saludo + ", " + nombre + " 👋";
    }

    const iconoEl = document.getElementById("inicioIcono");
    if (iconoEl) {
        iconoEl.textContent = icono;
    }

    const descEl = document.getElementById("dashDescBienvenida");
    if (descEl) {
        if (usuario.rol === "SUPERADMIN") {
            descEl.textContent = "Tienes control global: todas las sedes, clientes, catálogo, facturación y auditoría.";
        } else if (usuario.rol === "ADMIN") {
            descEl.textContent = "Administra los clientes, el catálogo, la facturación y el soporte de tu sucursal.";
        } else if (usuario.rol === "EMPLEADO") {
            descEl.textContent = "Consulta y actualiza clientes, y atiende las solicitudes de soporte en orden de llegada.";
        } else {
            descEl.textContent = "Consulta el estado de tu servicio, tu plan contratado y tus solicitudes de soporte.";
        }
    }

    const rolEl = document.getElementById("inicioChipRol");
    if (rolEl) {
        const etiquetas = { SUPERADMIN: "👑 Superusuario", ADMIN: "🛡️ Administrador local",
                            EMPLEADO: "👔 Empleado", CLIENTE: "👤 Cliente" };
        rolEl.textContent = etiquetas[usuario.rol] || usuario.rol || "Usuario";
    }

    const sucursalEl = document.getElementById("inicioChipSucursal");
    if (sucursalEl) {
        const sede = usuario.sucursal || (usuario.rol === "CLIENTE" ? "Cliente residencial" : "Sede principal");
        sucursalEl.textContent = "🏢 " + sede;
    }

    const dia = new Date().toLocaleDateString("es-CO", { weekday: "long", day: "numeric", month: "long" });
    const diaEl = document.getElementById("inicioFechaDia");
    if (diaEl) {
        diaEl.textContent = dia.charAt(0).toUpperCase() + dia.slice(1);
    }
    const horaEl = document.getElementById("inicioFechaHora");
    if (horaEl) {
        horaEl.textContent = new Date().toLocaleTimeString("es-CO", { hour: "2-digit", minute: "2-digit" });
    }
}

async function traer(ruta) {
    const respuesta = await fetch(API_BASE + ruta);
    return respuesta.json();
}

async function reunirDatosInicio(usuario) {
    const esCliente = usuario.rol === "CLIENTE";
    const esSuper = usuario.rol === "SUPERADMIN";
    const esEmpPlataforma = usuario.rol === "EMPLEADO" && usuario.empresaId === "PLATAFORMA";
    const puedeVerEmpleados = usuario.rol === "ADMIN" || esSuper;
    const cedulaEnc = encodeURIComponent(usuario.identificacion || "");

    const pedidos = [
        esSuper
            ? traer("/api/empresas").catch(function () { return null; })
            : (esEmpPlataforma
                ? Promise.resolve(null)
                : (esCliente
                    ? traer("/api/clientes/" + cedulaEnc).catch(function () { return null; })
                    : traer("/api/clientes").catch(function () { return null; }))),
        esEmpPlataforma ? Promise.resolve(null) : traer("/api/planes").catch(function () { return null; }),
        esEmpPlataforma
            ? Promise.resolve(null)
            : (esCliente
                ? traer("/api/facturas?usuario=" + cedulaEnc).catch(function () { return null; })
                : traer("/api/facturas").catch(function () { return null; })),
        traer("/api/soporte").catch(function () { return null; }),
        puedeVerEmpleados ? traer("/api/empleados").catch(function () { return null; }) : Promise.resolve(null)
    ];

    const resultados = await Promise.all(pedidos);
    return {
        esCliente: esCliente,
        esSuper: esSuper,
        esEmpPlataforma: esEmpPlataforma,
        empresas: esSuper ? resultados[0] : null,
        clientes: esSuper ? null : resultados[0],
        planes: resultados[1],
        facturas: resultados[2],
        soporte: resultados[3],
        empleados: resultados[4]
    };
}

function porcentaje(parte, total) {
    if (!total) return 0;
    return Math.round((parte / total) * 100);
}

function plural(n, singular, pluralForma) {
    return n + " " + (n === 1 ? singular : pluralForma);
}

function semaforo(pendientes) {
    if (pendientes === 0) return "verde";
    if (pendientes <= 2) return "ambar";
    return "rojo";
}

function tarjetasSegunRol(usuario, d) {
    const empresas = (d.empresas && d.empresas.datos) || [];
    const clientes = (d.clientes && d.clientes.datos) || [];
    const planes = (d.planes && d.planes.datos) || [];
    const facturas = (d.facturas && d.facturas.datos) || [];
    const pendientes = (d.soporte && d.soporte.pendientes) || [];
    const enProceso = (d.soporte && d.soporte.enProceso) || [];
    const atendidos = (d.soporte && d.soporte.atendidos) || [];
    const empleados = (d.empleados && d.empleados.datos) || [];

    if (d.esSuper) {
        const empActivas = empresas.filter(function (e) { return e.activo; }).length;
        const empInactivas = empresas.length - empActivas;
        const empleadosActivos = empleados.filter(function (e) { return e.activo; }).length;
        const planesActivos = planes.filter(function (p) { return p.activo; }).length;
        return [
            { icono: "🏢", etiqueta: "Empresas clientes", valor: String(empresas.length),
              pie: empActivas + " activas · " + empInactivas + " suspendidas",
              extra: empInactivas ? plural(empInactivas, "empresa bloqueada por pago", "empresas bloqueadas por pago")
                                  : "Todas las empresas al día",
              tono: empInactivas > 0 ? "ambar" : "verde", ir: "empresas" },
            { icono: "👔", etiqueta: "Empleados plataforma", valor: String(empleados.length),
              pie: plural(empleadosActivos, "activo", "activos") + " de " + empleados.length,
              extra: "Equipo propio de soporte SaaS",
              tono: "azul", ir: "empleados" },
            { icono: "🎧", etiqueta: "Tickets de admins", valor: String(pendientes.length + enProceso.length),
              pie: pendientes.length + " en cola · " + enProceso.length + " en proceso",
              extra: atendidos.length + " resueltos históricamente",
              tono: semaforo(pendientes.length), ir: "soporte" },
            { icono: "📡", etiqueta: "Planes activos", valor: String(planesActivos),
              pie: "Catálogo comercial del sistema",
              extra: plural(planes.length - planesActivos, "inactivo", "inactivos"),
              tono: "violeta", ir: "planes" }
        ];
    }

    if (d.esEmpPlataforma) {
        return [
            { icono: "🎧", etiqueta: "Tickets de admins", valor: String(pendientes.length),
              pie: pendientes.length === 0 ? "Sin tickets en espera" : "Orden FIFO por atender",
              extra: enProceso.length + " en proceso actualmente",
              tono: semaforo(pendientes.length), ir: "soporte" },
            { icono: "🛠️", etiqueta: "En atención", valor: String(enProceso.length),
              pie: "Tickets iniciados esperando cierre",
              extra: "Escribe la solución para notificar al admin",
              tono: enProceso.length > 0 ? "ambar" : "azul", ir: "soporte" },
            { icono: "✅", etiqueta: "Tickets cerrados", valor: String(atendidos.length),
              pie: "Resueltos en plataforma",
              extra: "Notificados por correo y web",
              tono: "verde", ir: "soporte" }
        ];
    }

    if (d.esCliente) {
        const mias = facturas.filter(function (f) { return f.identificacionCliente === usuario.identificacion; });
        const pagadas = mias.filter(function (f) { return f.estado === "PAGADA"; }).length;
        const porPagar = mias.filter(function (f) { return f.estado !== "PAGADA" && f.estado !== "ANULADA"; }).length;
        const alDia = porcentaje(pagadas, mias.length);
        const misTickets = pendientes.filter(function (t) { return t.correo === usuario.correo; }).length;
        const miPlan = planes.find(function (p) {
            return (p.nombre || "").toLowerCase() === (usuario.planActual || "").toLowerCase();
        });

        return [
            { icono: "📡", etiqueta: "Mi plan", valor: usuario.planActual || "Sin plan",
              pie: miPlan ? miPlan.velocidad + " · $" + Number(miPlan.precio).toLocaleString("es-CO")
                          : "Contrátalo desde el catálogo",
              extra: miPlan ? "Estado del plan: " + miPlan.estado : "Aún no tienes un plan contratado",
              tono: "violeta", ir: "planes" },
            { icono: "🧾", etiqueta: "Mis facturas", valor: String(mias.length),
              pie: pagadas + " pagadas · " + porPagar + " por pagar",
              extra: mias.length ? alDia + "% al día en pago" : "Todavía no tienes facturas",
              barra: mias.length ? alDia : null,
              tono: semaforo(porPagar), ir: "facturas" },
            { icono: "🎧", etiqueta: "Mis solicitudes", valor: String(misTickets),
              pie: misTickets ? "Abiertas en la mesa de ayuda" : "Ninguna abierta",
              extra: atendidos.length ? atendidos.length + " atendidas históricamente" : "Sin historial de atención",
              tono: semaforo(misTickets), ir: "soporte" },
            { icono: "✅", etiqueta: "Estado del servicio", valor: usuario.activo ? "ACTIVO" : "INACTIVO",
              pie: usuario.activo ? "Línea en servicio" : "Contacta a tu sucursal",
              extra: usuario.correo || "Cliente residencial",
              tono: usuario.activo ? "verde" : "rojo", ir: "perfil" }
        ];
    }

    const activos = clientes.filter(function (c) { return c.activo; }).length;
    const inactivos = clientes.length - activos;
    const planesActivos = planes.filter(function (p) { return p.activo; }).length;
    const pagadas = facturas.filter(function (f) { return f.estado === "PAGADA"; }).length;
    const porCobrar = facturas.filter(function (f) { return f.estado !== "PAGADA" && f.estado !== "ANULADA"; }).length;
    const alDia = porcentaje(pagadas, facturas.length);
    const empleadosActivos = empleados.filter(function (e) { return e.activo; }).length;

    if (usuario.rol === "EMPLEADO") {
        return [
            { icono: "👥", etiqueta: "Clientes", valor: String(clientes.length),
              pie: activos + " activos · " + inactivos + " inactivos",
              extra: inactivos ? plural(inactivos, "pendiente por reconexión", "pendientes por reconexión")
                               : "Sin pendientes por reconexión",
              tono: "azul", ir: "clientes" },
            { icono: "🎧", etiqueta: "Tickets en cola", valor: String(pendientes.length),
              pie: pendientes.length === 0 ? "Sin tickets pendientes" : "Atención en orden de llegada (FIFO)",
              extra: atendidos.length + " atendidos hasta hoy",
              tono: semaforo(pendientes.length), ir: "soporte" },
            { icono: "🧾", etiqueta: "Facturas por cobrar", valor: String(porCobrar),
              pie: pagadas + " pagadas de " + facturas.length,
              extra: facturas.length ? alDia + "% al día en pago" : "Sin facturas emitidas",
              barra: facturas.length ? alDia : null,
              tono: semaforo(porCobrar), ir: "facturas" },
            { icono: "📡", etiqueta: "Planes activos", valor: String(planesActivos),
              pie: "En el catálogo comercial",
              extra: plural(planes.length - planesActivos, "inactivo", "inactivos"),
              tono: "violeta", ir: "planes" }
        ];
    }

    const tarjetas = [
        { icono: "👥", etiqueta: "Clientes", valor: String(clientes.length),
          pie: activos + " activos · " + inactivos + " inactivos",
          extra: inactivos ? plural(inactivos, "pendiente por reconexión", "pendientes por reconexión")
                           : "Sin pendientes por reconexión",
          tono: "azul", ir: "clientes" },
        { icono: "🧾", etiqueta: "Facturación", valor: String(facturas.length),
          pie: porCobrar + " por cobrar · " + pagadas + " pagadas",
          extra: facturas.length ? alDia + "% al día en pago" : "Sin facturas emitidas",
          barra: facturas.length ? alDia : null,
          tono: semaforo(porCobrar), ir: "facturas" },
        { icono: "🎧", etiqueta: "Tickets clientes", valor: String(pendientes.length + enProceso.length),
          pie: pendientes.length + " en cola · " + enProceso.length + " en proceso",
          extra: atendidos.length + " atendidos hasta hoy",
          tono: semaforo(pendientes.length), ir: "soporte" },
        { icono: "📡", etiqueta: "Planes activos", valor: String(planesActivos),
          pie: "En el catálogo comercial",
          extra: plural(planes.length - planesActivos, "inactivo", "inactivos"),
          tono: "violeta", ir: "planes" },
        { icono: "👔", etiqueta: "Empleados empresa", valor: String(empleados.length),
          pie: plural(empleadosActivos, "activo", "activos") + " de " + empleados.length,
          extra: "Personal de tu empresa",
          tono: "azul", ir: "empleados" }
    ];

    return tarjetas;
}

function irAlModulo(seccion) {
    const enlace = document.querySelector('.nav-link[href="#' + seccion + '"]');
    if (typeof mostrarSeccion === "function") {
        mostrarSeccion(seccion, enlace);
    }
}

function pintarTarjeta(t) {
    const tono = t.tono ? " tono-" + t.tono : "";
    const barra = (typeof t.barra === "number" && t.barra > 0)
        ? '<div class="kpi-barra"><span style="width:' + Math.max(0, Math.min(100, t.barra)) + '%"></span></div>'
        : "";
    const extra = t.extra ? '<p class="kpi-extra">' + t.extra + '</p>' : "";

    return '<div class="kpi-card es-enlace' + tono + '" role="button" tabindex="0"' +
           ' onclick="irAlModulo(\'' + t.ir + '\')"' +
           ' onkeydown="if(event.key===\'Enter\'||event.key===\' \'){event.preventDefault();irAlModulo(\'' + t.ir + '\');}">' +
           '<span class="kpi-flecha">→</span>' +
           '<div class="kpi-cabecera">' +
             '<span class="kpi-icono">' + t.icono + '</span>' +
             '<p class="kpi-etiqueta">' + t.etiqueta + '</p>' +
           '</div>' +
           '<div class="kpi-valor' + (t.textoLargo ? ' kpi-valor--texto' : '') + '">' + t.valor + '</div>' +
           '<p class="kpi-pie">' + t.pie + '</p>' +
           extra +
           barra +
           '</div>';
}

function tarjetaCargando(texto) {
    return '<div class="kpi-card kpi-cargando">' +
             '<div class="kpi-cabecera"><span class="kpi-icono">⏳</span>' +
             '<p class="kpi-etiqueta">Cargando</p></div>' +
             '<div class="kpi-valor">—</div>' +
             '<p class="kpi-pie">' + texto + '</p>' +
           '</div>';
}

function adaptarVistasPorRol(usuario) {
    const esSuper = (usuario.rol === "SUPERADMIN");
    const esAdmin = (usuario.rol === "ADMIN");
    const esCliente = (usuario.rol === "CLIENTE");
    const esEmpPlataforma = (usuario.rol === "EMPLEADO" && usuario.empresaId === "PLATAFORMA");

    const enlaceEmpleados = document.getElementById("navLinkEmpleados");
    if (enlaceEmpleados) {
        enlaceEmpleados.style.display = (esAdmin || esSuper) ? "flex" : "none";
        enlaceEmpleados.innerHTML = esSuper
            ? '<span>👔</span> Empleados Plataforma'
            : '<span>👔</span> Empleados Empresa';
    }

    const enlaceEmpresas = document.getElementById("navLinkEmpresas");
    if (enlaceEmpresas) {
        enlaceEmpresas.style.display = esSuper ? "flex" : "none";
    }

    const enlaceClientes = document.getElementById("navLinkClientes");
    if (enlaceClientes) {
        enlaceClientes.style.display = (esCliente || esSuper || esEmpPlataforma) ? "none" : "flex";
    }

    const enlacePerfil = document.getElementById("navLinkPerfil");
    if (enlacePerfil) {
        enlacePerfil.style.display = esCliente ? "flex" : "none";
    }

    const enlaceSoportePlat = document.getElementById("navLinkSoportePlataforma");
    if (enlaceSoportePlat) {
        enlaceSoportePlat.style.display = esAdmin ? "flex" : "none";
    }

    const enlacePlanes = document.getElementById("navLinkPlanes");
    if (enlacePlanes) {
        enlacePlanes.innerHTML = esCliente
            ? '<span>📡</span> Mi plan'
            : '<span>📡</span> Catálogo de Planes';
        enlacePlanes.style.display = esEmpPlataforma ? "none" : "flex";
    }

    const enlaceFacturacion = document.getElementById("navLinkFacturacion");
    if (enlaceFacturacion) {
        enlaceFacturacion.style.display = esEmpPlataforma ? "none" : "flex";
    }

    const enlaceSoporte = document.getElementById("navLinkSoporte");
    if (enlaceSoporte) {
        enlaceSoporte.innerHTML = (esSuper || esEmpPlataforma)
            ? '<span>🎧</span> Tickets de Admins'
            : '<span>🎧</span> Mesa de Ayuda';
    }

    const empTitulo = document.querySelector("#seccion-empleados .cli-titulo h3");
    const empSub = document.querySelector("#seccion-empleados .modulo-subtitulo");
    if (empTitulo && empSub) {
        if (esSuper) {
            empTitulo.textContent = "Gestión de Empleados de Plataforma";
            empSub.textContent = "Tu equipo propio de soporte para atender tickets y ayudas de los administradores.";
        } else {
            empTitulo.textContent = "Gestión de Empleados de Mi Empresa";
            empSub.textContent = "Personal propio de tu empresa: cargos, atención al cliente y técnicos.";
        }
    }
}

function cerrarSesion() {
    localStorage.removeItem("sesionActiva");

    sessionStorage.removeItem("seccionAbierta");

    window.location.href = "login.html";
}

function seccionPermitida(seccion, rol) {
    const sesion = sesionActual();
    const esEmpPlataforma = (rol === "EMPLEADO" && sesion && sesion.empresaId === "PLATAFORMA");
    if (esEmpPlataforma) {
        return seccion === "inicio" || seccion === "soporte";
    }
    const permisos = {
        inicio:               ["SUPERADMIN", "ADMIN", "EMPLEADO", "CLIENTE"],
        clientes:             ["ADMIN", "EMPLEADO"],
        empresas:             ["SUPERADMIN"],
        perfil:               ["CLIENTE"],
        empleados:            ["SUPERADMIN", "ADMIN"],
        planes:               ["SUPERADMIN", "ADMIN", "EMPLEADO", "CLIENTE"],
        facturas:             ["SUPERADMIN", "ADMIN", "EMPLEADO", "CLIENTE"],
        soporte:              ["SUPERADMIN", "ADMIN", "EMPLEADO", "CLIENTE"],
        "soporte-plataforma": ["ADMIN"]
    };

    if (!permisos[seccion]) return false;
    return permisos[seccion].indexOf(rol) !== -1;
}

function mostrarAlerta(elementoAlerta, mensaje, tipo) {
    elementoAlerta.textContent = mensaje;
    elementoAlerta.style.display = "block";
    if (tipo === "error") {
        elementoAlerta.className = "alert alert-error";
    } else {
        elementoAlerta.className = "alert alert-success";
    }
}

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

function puedeEliminarEmpleados() {
    const sesion = sesionActual();
    return !!sesion && (sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN");
}

function puedeAdministrarClientes() {
    const sesion = sesionActual();
    return !!sesion && (sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN");
}

const API_CLIENTES = API_BASE + "/api/clientes";
const API_PLANES_CATALOGO = API_BASE + "/api/planes";

const FILAS_POR_PAGINA = 5;
let paginaActual = 1;
let clientesFiltrados = [];
let clientesTodos = [];
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

function mostrarPaginaSinSaltos() {
    const area = document.querySelector(".content-area");
    const posicion = area ? area.scrollTop : 0;

    mostrarPagina();

    if (area) area.scrollTop = posicion;
}

function mostrarPagina() {

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

    const activos = clientesFiltrados.filter(c => c.activo).length;
    document.getElementById("chipTotal").textContent = clientesFiltrados.length;
    document.getElementById("chipActivos").textContent = activos;
    document.getElementById("chipInactivos").textContent = clientesFiltrados.length - activos;

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

async function cargarClientes(mantenerPagina) {
    try {
        const respuesta = await fetch(API_CLIENTES);
        const json = await respuesta.json();
        clientesTodos = json.datos || [];
        clientesFiltrados = clientesTodos;

        if (mantenerPagina !== true) {
            paginaActual = 1;
        }

        mostrarPagina();

        obtenerPlanesActivos();
    } catch (error) {
        mostrarErrorServidor();
    }
}

function alEscribirBusqueda() {
    clearTimeout(temporizadorBusqueda);
    temporizadorBusqueda = setTimeout(filtrarClientes, 300);
}

async function filtrarClientes() {
    const texto = document.getElementById("buscarCliente").value.trim();
    const estado = document.getElementById("filtroEstado").value;

    try {

        if (texto !== "") {

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

        let url = API_CLIENTES;

        if (estado === "activos" || estado === "inactivos") {

            url = API_CLIENTES + "?estado=" + estado;
        }

        const respuesta = await fetch(url);
        const json = await respuesta.json();

        clientesFiltrados = json.datos || [];

        if (texto === "" && estado === "todos") {
            clientesTodos = json.datos || [];
        }

        paginaActual = 1;
        mostrarPagina();
    } catch (error) {
        mostrarErrorServidor();
    }
}

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

        cargarClientes(true);
    } catch (error) {
        mostrarErrorServidor();
    }
}

let modoFormulario = "nuevo";

let planesActivosCache = null;
let planesActivosCacheHora = 0;
const PLANES_CACHE_MS = 300000;

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

        return planesActivosCache || [];
    }
}

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

    pintarOpcionesDePlan(planesActivosCache || [], planActual);

    const planes = await obtenerPlanesActivos();

    if (planes.length > 0) {
        pintarOpcionesDePlan(planes, planActual);
    }
}

async function abrirModal(identificacion) {
    modoFormulario = "editar";

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
    if (document.getElementById("editDireccion")) {
        document.getElementById("editDireccion").value = cliente.direccion || "";
    }
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
    if (document.getElementById("editDireccion")) {
        document.getElementById("editDireccion").value = "";
    }
    await cargarPlanesEnSelect(null);
    document.getElementById("editEstado").value = "Activo";

    document.getElementById("modalCliente").style.display = "flex";
}

function cerrarModal() {

    if (document.activeElement && document.activeElement.blur) {
        document.activeElement.blur();
    }
    document.getElementById("modalCliente").style.display = "none";
}

document.addEventListener("submit", function (evento) {
    evento.preventDefault();
});

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

    const campoDir = document.getElementById("editDireccion");
    const direccionValor = campoDir ? campoDir.value.trim() : "";
    const direccionFinal = direccionValor !== ""
        ? direccionValor
        : (modoFormulario === "nuevo" ? "Sin dirección" : "");

    const datos = {
        identificacion: identificacion,
        nombre: nombre,
        apellido: apellido,
        tipo: "CC",
        telefono: telefono,
        correo: correo,
        direccion: direccionFinal,
        contrasena: (modoFormulario === "nuevo") ? "Clave123" : "",
        planActual: document.getElementById("editPlan").value,
        activo: document.getElementById("editEstado").value === "Activo" ? "true" : "false"
    };

    enviarCliente(datos);
}

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

        if (!json.exito) {
            notificar(json.mensaje, "error");
            return;
        }

        cerrarModal();
        notificar(esNuevo ? "Cliente registrado correctamente." : "Cliente actualizado correctamente.", "exito");

        cargarClientes(true);

    } catch (error) {
        notificar("No se pudo conectar con el servidor Java. Arranca ApiServer.", "error");
    }
}

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

const EMP_API = API_BASE + "/api/empleados";
const EMP_FILAS_POR_PAGINA = 4;

let empTodos = [];
let empFiltrados = [];
let empPagina = 1;
let empModo = "nuevo";
let empTemporizador = null;

async function cargarEmpleados(mantenerPagina) {
    try {
        const respuesta = await fetch(EMP_API);
        const json = await respuesta.json();
        empTodos = json.datos || [];
        empFiltrados = empTodos;

        if (mantenerPagina !== true) {
            empPagina = 1;
        }

        empAplicarFiltro(mantenerPagina);
    } catch (error) {
        empErrorServidor();
    }
}

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
                ${(puedeEliminarEmpleados() && (esSuperAdminActual() || (e.rol !== "SUPERADMIN" && e.identificacion !== "9999999999"))) ? `<button class="btn-icono peligro" aria-label="Eliminar empleado" onclick="eliminarEmpleado('${e.identificacion}')" title="Eliminar empleado">🗑️</button>` : ""}
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

function prepararSelectoresModalEmpleado(sucursalValor, cargoValor) {
    const selSuc = document.getElementById("empSucursal");
    const selCargo = document.getElementById("empCargo");
    const sesion = sesionActual() || {};

    if (selSuc) {
        if (esSuperAdminActual()) {
            selSuc.innerHTML = '<option value="Plataforma Central">Plataforma Central</option>';
            selSuc.value = "Plataforma Central";
        } else {
            const sedeEmp = sesion.sucursal || "Mi Empresa";
            selSuc.innerHTML = `<option value="${sedeEmp}">${sedeEmp}</option>`;
            selSuc.value = sucursalValor || sedeEmp;
        }
    }

    if (selCargo) {
        const opciones = esSuperAdminActual()
            ? ["Soporte Plataforma", "Especialista Nivel 2", "Atención a Empresas", "Supervisor Plataforma"]
            : ["Técnico de campo", "Técnico de red", "Atención al cliente", "Supervisor", "Administrativo"];
        selCargo.innerHTML = opciones.map(c => `<option value="${c}">${c}</option>`).join("");
        if (cargoValor && opciones.includes(cargoValor)) {
            selCargo.value = cargoValor;
        }
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
    prepararSelectoresModalEmpleado(empleado.sucursal, empleado.cargo);
    document.getElementById("empEstado").value = empleado.activo ? "Activo" : "Inactivo";

    const filaRol = document.getElementById("filaEmpRol");
    if (filaRol) {
        filaRol.style.display = "none";
        document.getElementById("empRol").value = "EMPLEADO";
        alCambiarRolEmpleado();
    }

    document.getElementById("modalEmpleado").style.display = "flex";
}

function abrirModalEmpleadoNuevo() {
    empModo = "nuevo";

    document.getElementById("modalEmpleadoTitulo").textContent = esSuperAdminActual()
        ? "➕ Nuevo Empleado de Plataforma"
        : "➕ Registrar Nuevo Empleado";

    document.getElementById("empIdentificacion").value = "";
    document.getElementById("empIdentificacion").readOnly = false;
    document.getElementById("empNombre").value = "";
    document.getElementById("empApellido").value = "";
    document.getElementById("empCorreo").value = "";
    document.getElementById("empTelefono").value = "";
    prepararSelectoresModalEmpleado(null, null);
    document.getElementById("empEstado").value = "Activo";

    const filaRol = document.getElementById("filaEmpRol");
    if (filaRol) {
        filaRol.style.display = "none";
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

    let rolElegido = "EMPLEADO";
    if (esSuperAdminActual()) {
        rolElegido = document.getElementById("empRol").value;
    } else if (empModo === "editar") {
        const empActual = empTodos.find(function (e) { return e.identificacion === identificacion; });
        rolElegido = (empActual && empActual.rol) ? empActual.rol : "EMPLEADO";
    }
    const cargoElegido = (rolElegido === "ADMIN")
        ? (document.getElementById("empCargo").value || "Administrador de sucursal")
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

async function eliminarEmpleado(identificacion) {
    if (!puedeEliminarEmpleados()) {
        notificar("Solo un administrador puede eliminar empleados.", "error");
        return;
    }

    const empleado = empTodos.find(function (e) { return e.identificacion === identificacion; });
    if (!empleado) return;

    if (!esSuperAdminActual() && (empleado.rol === "SUPERADMIN" || empleado.identificacion === "9999999999")) {
        notificar("Un Administrador no puede eliminar al Super Administrador.", "error");
        return;
    }

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
                contrasena: contrasena
            })
        });

        const json = await respuesta.json();

        if (!json.exito) {
            notificar(json.mensaje, "error");
            return;
        }

        sesion.nombre = nombre;
        sesion.apellido = apellido;
        sesion.correo = correo;
        sesion.telefono = telefono;
        localStorage.setItem("sesionActiva", JSON.stringify(sesion));

        notificar(contrasena !== ""
            ? "Tus datos y tu contraseña se actualizaron correctamente."
            : "Tus datos se actualizaron correctamente.", "exito");

        document.getElementById("perfilContrasena").value = "";
        document.getElementById("perfilContrasena2").value = "";
        cargarDashboard();

    } catch (error) {
        notificar("No se pudo conectar con el servidor Java.", "error");
    }
}
