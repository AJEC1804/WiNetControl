/* ==========================================================================
   LÓGICA JAVASCRIPT - WINET CONTROL (NIVEL PRINCIPIANTE)
   Autor: Alejandro Escudero
   Explicación: Este archivo maneja las acciones del usuario (hacer clic en
   iniciar sesión, registrarse, verificar datos y cerrar sesión).
   ========================================================================== */

// 1. USUARIOS INICIALES DE PRUEBA (Para que puedas probar el Login de inmediato)
const usuariosIniciales = [
    {
        identificacion: "1045678901",
        contrasena: "Clave123",
        nombre: "Alejandro",
        apellido: "Escudero",
        tipo: "CC",
        telefono: "3001234567",
        correo: "alejandrocastellar04@gmail.com",
        direccion: "Calle 10 # 20-30",
        planActual: "Plan Fibra Gamer 300M",
        activo: true // Estado: ACTIVO
    },
    {
        identificacion: "1098765432",
        contrasena: "Clave456",
        nombre: "María",
        apellido: "Gómez",
        tipo: "CC",
        telefono: "3109876543",
        correo: "maria.gomez@email.com",
        direccion: "Carrera 15 # 45-10",
        planActual: "Plan Básico 50M",
        activo: false // Estado: INACTIVO (Para probar que el login la rechace)
    }
];

// Si el navegador no tiene usuarios guardados aún, guardamos los iniciales
if (!localStorage.getItem("usuariosWiNet")) {
    localStorage.setItem("usuariosWiNet", JSON.stringify(usuariosIniciales));
}

// ==========================================================================
// 2. FUNCIÓN DE INICIO DE SESIÓN (LOGIN)
// ==========================================================================
function procesarLogin(evento) {
    evento.preventDefault(); // Evita que la página se recargue

    // Obtenemos los valores que el usuario escribió en las cajas de texto
    const idInput = document.getElementById("loginIdentificacion").value.trim();
    const claveInput = document.getElementById("loginContrasena").value.trim();
    const alertBox = document.getElementById("loginAlert");

    // Limpiamos alertas anteriores
    alertBox.style.display = "none";
    alertBox.className = "alert";

    // Validación básica: campos vacíos
    if (idInput === "" || claveInput === "") {
        mostrarAlerta(alertBox, "Por favor complete todos los campos.", "error");
        return;
    }

    // Validación: identificación numérica
    if (!/^\d{8,10}$/.test(idInput)) {
        mostrarAlerta(alertBox, "La identificación debe tener entre 8 y 10 dígitos numéricos.", "error");
        return;
    }

    // Buscamos los usuarios guardados
    const listaUsuarios = JSON.parse(localStorage.getItem("usuariosWiNet")) || [];

    // Buscamos si existe un usuario con esa cédula y esa contraseña
    const usuarioEncontrado = listaUsuarios.find(u => u.identificacion === idInput && u.contrasena === claveInput);

    if (!usuarioEncontrado) {
        mostrarAlerta(alertBox, "Identificación o contraseña incorrectas.", "error");
        return;
    }

    // REGLA DE NEGOCIO: Validar si el usuario está ACTIVO o INACTIVO
    if (!usuarioEncontrado.activo) {
        mostrarAlerta(alertBox, "El cliente se encuentra INACTIVO. Comuníquese con la administración.", "error");
        return;
    }

    // Si todo está bien: Guardamos la sesión en el navegador
    localStorage.setItem("sesionActiva", JSON.stringify(usuarioEncontrado));

    mostrarAlerta(alertBox, "¡Bienvenido, " + usuarioEncontrado.nombre + "! Redirigiendo...", "success");

    // Esperamos 1 segundo y enviamos al usuario al Dashboard
    setTimeout(() => {
        window.location.href = "dashboard.html";
    }, 1000);
}

// ==========================================================================
// 3. FUNCIÓN DE REGISTRO DE NUEVO CLIENTE
// ==========================================================================
function procesarRegistro(evento) {
    evento.preventDefault();

    const alertBox = document.getElementById("registroAlert");
    alertBox.style.display = "none";

    // Captura de valores del formulario
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

    // 2. Validar formato de cédula (8-10 dígitos)
    if (!/^\d{8,10}$/.test(identificacion)) {
        mostrarAlerta(alertBox, "La identificación debe tener entre 8 y 10 dígitos numéricos.", "error");
        return;
    }

    // 3. Validar teléfono (10 dígitos)
    if (!/^\d{10}$/.test(telefono)) {
        mostrarAlerta(alertBox, "El teléfono debe contener exactamente 10 dígitos.", "error");
        return;
    }

    // 4. Validar contraseña mínima
    if (contrasena.length < 6) {
        mostrarAlerta(alertBox, "La contraseña debe tener mínimo 6 caracteres.", "error");
        return;
    }

    // 5. Validar que coincidan las contraseñas
    if (contrasena !== confContrasena) {
        mostrarAlerta(alertBox, "Las contraseñas no coinciden.", "error");
        return;
    }

    // 6. Verificar si la cédula o correo ya existen
    const listaUsuarios = JSON.parse(localStorage.getItem("usuariosWiNet")) || [];

    const yaExiste = listaUsuarios.some(u => u.identificacion === identificacion || u.correo === correo);
    if (yaExiste) {
        mostrarAlerta(alertBox, "Ya existe un cliente registrado con esa identificación o correo.", "error");
        return;
    }

    // Creamos el nuevo cliente (inicia en estado ACTIVO y con Plan Básico)
    const nuevoCliente = {
        nombres,
        apellidos,
        tipo,
        identificacion,
        telefono,
        correo,
        direccion,
        contrasena,
        planActual: "Plan Básico 50M",
        activo: true
    };

    listaUsuarios.push(nuevoCliente);
    localStorage.setItem("usuariosWiNet", JSON.stringify(listaUsuarios));

    mostrarAlerta(alertBox, "¡Cuenta creada exitosamente! Redirigiendo al login...", "success");

    setTimeout(() => {
        window.location.href = "login.html";
    }, 1500);
}

// ==========================================================================
// 4. FUNCIÓN PARA CARGAR LOS DATOS EN EL DASHBOARD
// ==========================================================================
function cargarDashboard() {
    // Verificamos si hay una sesión iniciada
    const sesion = localStorage.getItem("sesionActiva");

    if (!sesion) {
        // Si no ha iniciado sesión, lo devolvemos al login
        window.location.href = "login.html";
        return;
    }

    const usuario = JSON.parse(sesion);

    // Pintamos los datos en la pantalla
    const nombreCompleto = (usuario.nombre || usuario.nombres) + " " + (usuario.apellido || usuario.apellidos);
    
    // Elementos del Topbar y Bienvenida
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

    // Tarjetas resumen
    if (document.getElementById("dashPlan")) {
        document.getElementById("dashPlan").textContent = usuario.planActual || "Sin plan asignado";
    }
    if (document.getElementById("dashEstadoBadge")) {
        const badge = document.getElementById("dashEstadoBadge");
        if (usuario.activo) {
            badge.textContent = "ACTIVO";
            badge.className = "badge badge-activo";
        } else {
            badge.textContent = "INACTIVO";
            badge.className = "badge badge-inactivo";
        }
    }
}

// ==========================================================================
// 5. FUNCIÓN PARA CERRAR SESIÓN
// ==========================================================================
function cerrarSesion() {
    localStorage.removeItem("sesionActiva");
    window.location.href = "login.html";
}

// ==========================================================================
// 6. FUNCIÓN AUXILIAR PARA MOSTRAR ALERTAS EN PANTALLA
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
