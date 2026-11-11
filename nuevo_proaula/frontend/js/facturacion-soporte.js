/* ==========================================================================
   LÓGICA JAVASCRIPT - FACTURACIÓN (LIFO) Y SOPORTE TÉCNICO (FIFO)
   Autor: Juan Esteban Navia (Issue #10)
   Proyecto: WiNet Control - ProAula Estructuras de Datos / MVC
   Nivel: 4to Semestre - Código pedagógico, directo y modular
   ========================================================================== */

// 1. DATOS INICIALES DE PRUEBA (PILAS Y COLAS EN LOCALSTORAGE)
const facturasIniciales = [
    {
        idFactura: 3,
        identificacionCliente: "1045678901",
        nombreCliente: "Alejandro Escudero",
        idPlan: 102,
        nombrePlan: "Plan Fibra Gamer 300M",
        fecha: "01/11/2026",
        monto: 95000,
        archivoPdf: "facturas/factura_Alejandro_Escudero-11-2026.pdf",
        estado: "EMITIDA" // Esta es la CIMA (LIFO) - Última emitida
    },
    {
        idFactura: 2,
        identificacionCliente: "1098765432",
        nombreCliente: "María Gómez",
        idPlan: 101,
        nombrePlan: "Plan Básico 50M",
        fecha: "15/10/2026",
        monto: 50000,
        archivoPdf: "facturas/factura_Maria_Gomez-10-2026.pdf",
        estado: "PAGADA"
    },
    {
        idFactura: 1,
        identificacionCliente: "1045678901",
        nombreCliente: "Alejandro Escudero",
        idPlan: 102,
        nombrePlan: "Plan Fibra Gamer 300M",
        fecha: "01/10/2026",
        monto: 95000,
        archivoPdf: "facturas/factura_Alejandro_Escudero-10-2026.pdf",
        estado: "PAGADA" // Fondo de la Pila
    }
];

const ticketsIniciales = [
    {
        idTicket: 1,
        identificacion: "1045678901",
        usuario: "Alejandro Escudero",
        correo: "alejandrocastellar04@gmail.com",
        asunto: "Lentitud en horario nocturno",
        categoria: "Técnico",
        mensaje: "Presento caídas de velocidad entre las 8:00 p.m. y 10:00 p.m. en el sector norte.",
        fecha: "01/11/2026",
        estado: "PENDIENTE" // FRENTE DE LA COLA (FIFO) - 1° en ser atendido
    },
    {
        idTicket: 2,
        identificacion: "1098765432",
        usuario: "María Gómez",
        correo: "maria.gomez@email.com",
        asunto: "Solicitud de cambio de clave WiFi",
        categoria: "Configuración",
        mensaje: "Deseo actualizar la contraseña de mi router de fibra óptica por seguridad.",
        fecha: "01/11/2026",
        estado: "PENDIENTE" // 2° en espera
    }
];

// Inicializar almacenamiento local si no existen
(function inicializarAlmacenamientoFacturasYSoporte() {
    if (!localStorage.getItem("facturasWiNet")) {
        localStorage.setItem("facturasWiNet", JSON.stringify(facturasIniciales));
    }
    if (!localStorage.getItem("ticketsSoporteWiNet")) {
        localStorage.setItem("ticketsSoporteWiNet", JSON.stringify(ticketsIniciales));
    }
    if (!localStorage.getItem("ticketsAtendidosWiNet")) {
        localStorage.setItem("ticketsAtendidosWiNet", JSON.stringify([]));
    }
})();


// ==========================================================================
// 2. MÓDULO DE FACTURACIÓN: ESTRUCTURA PILA (LIFO)
// ==========================================================================

// Apilar una nueva factura (Push) -> Se ubica en la cima (índice 0)
function apilarFactura(nuevaFactura) {
    const pila = JSON.parse(localStorage.getItem("facturasWiNet")) || [];
    pila.unshift(nuevaFactura); // Push: añade al tope de la pila
    localStorage.setItem("facturasWiNet", JSON.stringify(pila));
    return nuevaFactura;
}

// Desapilar la última factura emitida (Pop) -> Retira la de la cima
function desapilarFactura() {
    const pila = JSON.parse(localStorage.getItem("facturasWiNet")) || [];
    if (pila.length === 0) return null;
    const facturaDesapilada = pila.shift(); // Pop: elimina del tope
    localStorage.setItem("facturasWiNet", JSON.stringify(pila));
    return facturaDesapilada;
}

// Consultar la cima de la pila sin retirarla (Peek)
function consultarCimaFactura() {
    const pila = JSON.parse(localStorage.getItem("facturasWiNet")) || [];
    return pila.length > 0 ? pila[0] : null; // Peek: mira el tope
}

// Obtener facturas filtradas por cliente o todas si es administrador
function obtenerFacturasPorUsuario(criterioId) {
    const pila = JSON.parse(localStorage.getItem("facturasWiNet")) || [];
    if (!criterioId) return pila;
    return pila.filter(f => f.identificacionCliente === criterioId);
}

// Renderizar el contenido completo de la sección Facturación
function cargarModuloFacturas() {
    const contenedor = document.getElementById("seccion-facturas");
    if (!contenedor) return;

    const sesion = JSON.parse(localStorage.getItem("sesionActiva")) || {};
    const esAdmin = sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN";
    const cima = consultarCimaFactura();
    const facturas = esAdmin ? obtenerFacturasPorUsuario(null) : obtenerFacturasPorUsuario(sesion.identificacion);

    let html = `
        <div class="facturacion">
            
            <!-- Encabezado del Módulo -->
            <div class="facturacion__header">
                <div class="facturacion__header-titulo">
                    <span>📄</span>
                    <span>Facturación de Servicios y Comprobantes (Pila LIFO)</span>
                </div>
                <div class="facturacion__header-subtitulo">
                    Gestión de facturación por estructuras dinámicas. La última factura emitida se ubica siempre en la cima de la pila.
                </div>
            </div>

            <!-- Cima de la Pila (Última Factura Emitida) -->
            <div class="facturacion__cima">
                <div class="facturacion__cima-cabecera">
                    <div class="facturacion__cima-titulo">
                        <span>📌</span>
                        <span>Cima de la Pila LIFO (Última Factura Emitida en el Sistema)</span>
                    </div>
                    <span class="facturacion__badge--cima">TOPE DE LA PILA (LIFO)</span>
                </div>
    `;

    if (cima) {
        html += `
                <div class="facturacion__cima-grid">
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Factura N°</span>
                        <span class="facturacion__dato-valor">#00${cima.idFactura}</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Suscriptor</span>
                        <span class="facturacion__dato-valor">${cima.nombreCliente}</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Plan Asignado</span>
                        <span class="facturacion__dato-valor">${cima.nombrePlan}</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Fecha de Emisión</span>
                        <span class="facturacion__dato-valor">${cima.fecha}</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Monto Total</span>
                        <span class="facturacion__dato-valor facturacion__dato-valor--monto">$${cima.monto.toLocaleString('es-CO')} COP</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Estado</span>
                        <span class="facturacion__dato-valor" style="color: ${cima.estado === 'PAGADA' ? '#16a34a' : '#2563eb'};">
                            ${cima.estado === 'PAGADA' ? 'Al Día ✅' : 'Emitida ⏳'}
                        </span>
                    </div>
                </div>

                <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                    <button class="facturacion__btn facturacion__btn--primario" onclick="verComprobanteFactura(${cima.idFactura})">
                        <span>👁️</span> Ver Comprobante Oficial (PDF)
                    </button>
                    ${esAdmin ? `
                        <button class="facturacion__btn facturacion__btn--peligro" onclick="confirmarDesapilarFactura()">
                            <span>⬇️</span> Desapilar Última Factura (Pop LIFO)
                        </button>
                    ` : ''}
                </div>
        `;
    } else {
        html += `
                <div class="facturacion__tabla-vacia">
                    La pila de facturas se encuentra vacía actualmente.
                </div>
        `;
    }

    html += `
            </div>
    `;

    // Barra de herramientas para Administradores
    if (esAdmin) {
        html += `
            <div class="facturacion__toolbar">
                <div style="font-weight: 600; font-size: 14px; color: #1e293b;">
                    <span>⚙️</span> Panel de Control de Facturación (Administrador)
                </div>
                <div class="facturacion__toolbar-acciones">
                    <button class="facturacion__btn facturacion__btn--primario" onclick="abrirModalEmitirFactura()">
                        <span>➕</span> Emitir Nueva Factura (Apilar / Push)
                    </button>
                </div>
            </div>
        `;
    }

    // Tabla de Historial de Facturas
    html += `
            <div class="facturacion__tabla-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                    <h4 style="font-size: 15px; font-weight: 700; color: #1e293b;">
                        <span>📋</span> ${esAdmin ? 'Historial Global de Facturas en la Pila' : 'Mis Facturas de Servicio'}
                    </h4>
                    <span style="font-size: 12px; color: #64748b;">Total apiladas: ${facturas.length}</span>
                </div>

                <div class="facturacion__tabla-contenedor">
                    <table class="facturacion__tabla">
                        <thead>
                            <tr>
                                <th>Posición</th>
                                <th>N° Factura</th>
                                <th>Suscriptor</th>
                                <th>Plan Contratado</th>
                                <th>Fecha Emisión</th>
                                <th>Total</th>
                                <th>Estado</th>
                                <th>Acción</th>
                            </tr>
                        </thead>
                        <tbody>
    `;

    if (facturas.length === 0) {
        html += `
            <tr>
                <td colspan="8" class="facturacion__tabla-vacia">No se encontraron facturas registradas.</td>
            </tr>
        `;
    } else {
        facturas.forEach((fact, index) => {
            const esTope = index === 0;
            html += `
                <tr style="${esTope ? 'background-color: #eff6ff; font-weight: 600;' : ''}">
                    <td>
                        ${esTope ? '<span class="facturacion__badge--cima">CIMA</span>' : `#${index + 1}`}
                    </td>
                    <td>#00${fact.idFactura}</td>
                    <td>${fact.nombreCliente}</td>
                    <td>${fact.nombrePlan}</td>
                    <td>${fact.fecha}</td>
                    <td style="color: #16a34a; font-weight: 700;">$${fact.monto.toLocaleString('es-CO')}</td>
                    <td>
                        <span class="badge ${fact.estado === 'PAGADA' ? 'badge-activo' : 'badge-admin'}">
                            ${fact.estado}
                        </span>
                    </td>
                    <td>
                        <button class="facturacion__btn facturacion__btn--secundario" style="padding: 4px 10px; font-size: 12px;" onclick="verComprobanteFactura(${fact.idFactura})">
                            <span>📄</span> Ver PDF
                        </button>
                    </td>
                </tr>
            `;
        });
    }

    html += `
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    `;

    contenedor.innerHTML = html;
}

// Desapilar la última factura con confirmación
function confirmarDesapilarFactura() {
    const cima = consultarCimaFactura();
    if (!cima) {
        alert("La pila de facturas ya está vacía.");
        return;
    }

    if (confirm(`¿Deseas desapilar (Pop LIFO) la factura #00${cima.idFactura} de ${cima.nombreCliente}? Esta acción retira la factura de la cima.`)) {
        const retirada = desapilarFactura();
        if (retirada) {
            alert(`Factura #00${retirada.idFactura} desapilada exitosamente. La pila retrocedió a la factura anterior.`);
            cargarModuloFacturas();
        }
    }
}

// Abrir diálogo simple para emitir factura
function abrirModalEmitirFactura() {
    const usuarios = JSON.parse(localStorage.getItem("usuariosWiNet")) || [];
    const clientes = usuarios.filter(u => u.rol === "CLIENTE");

    if (clientes.length === 0) {
        alert("No hay clientes registrados en el sistema para facturar.\n\nRecuerde iniciar el flujo desde el login o registrar suscriptores en la plataforma.");
        return;
    }

    const opcionesClientes = clientes.map(c => `${c.identificacion} - ${c.nombre} ${c.apellido}`).join("\n");
    const cedula = prompt("Ingrese el número de cédula del cliente a facturar:\n\nClientes disponibles:\n" + opcionesClientes);

    if (!cedula) return;

    const clienteSeleccionado = clientes.find(c => c.identificacion === cedula.trim());
    if (!clienteSeleccionado) {
        alert("No se encontró ningún cliente con la cédula ingresada.");
        return;
    }

    const pila = JSON.parse(localStorage.getItem("facturasWiNet")) || [];
    const nuevoId = pila.length > 0 ? Math.max(...pila.map(f => f.idFactura)) + 1 : 1;
    const fechaHoy = new Date().toLocaleDateString('es-CO');

    const nuevaFactura = {
        idFactura: nuevoId,
        identificacionCliente: clienteSeleccionado.identificacion,
        nombreCliente: `${clienteSeleccionado.nombre} ${clienteSeleccionado.apellido}`,
        idPlan: 101,
        nombrePlan: clienteSeleccionado.planActual || "Plan Básico 50M",
        fecha: fechaHoy,
        monto: 65000,
        archivoPdf: `facturas/factura_${clienteSeleccionado.identificacion}-${nuevoId}.pdf`,
        estado: "EMITIDA"
    };

    apilarFactura(nuevaFactura);
    alert(`¡Factura #00${nuevaFactura.idFactura} emitida y apilada en la cima de la pila LIFO exitosamente!`);
    cargarModuloFacturas();
}


// ==========================================================================
// 3. MÓDULO DE MESA DE AYUDA: ESTRUCTURA COLA (FIFO)
// ==========================================================================

// Encolar un nuevo ticket (Enqueue) -> Se agrega al final de la fila
function encolarTicket(nuevoTicket) {
    const cola = JSON.parse(localStorage.getItem("ticketsSoporteWiNet")) || [];
    cola.push(nuevoTicket); // Enqueue: añade al final
    localStorage.setItem("ticketsSoporteWiNet", JSON.stringify(cola));
    return nuevoTicket;
}

// Desencolar el siguiente ticket en espera (Dequeue) -> Se toma del frente
function desencolarTicket() {
    const cola = JSON.parse(localStorage.getItem("ticketsSoporteWiNet")) || [];
    if (cola.length === 0) return null;
    const ticketAtendido = cola.shift(); // Dequeue: retira del frente
    ticketAtendido.estado = "ATENDIDO";
    ticketAtendido.fechaAtencion = new Date().toLocaleDateString('es-CO');

    localStorage.setItem("ticketsSoporteWiNet", JSON.stringify(cola));

    // Guardar en el histórico de atendidos
    const atendidos = JSON.parse(localStorage.getItem("ticketsAtendidosWiNet")) || [];
    atendidos.unshift(ticketAtendido);
    localStorage.setItem("ticketsAtendidosWiNet", JSON.stringify(atendidos));

    return ticketAtendido;
}

// Consultar el ticket que está al frente de la cola (Peek)
function consultarFrenteTicket() {
    const cola = JSON.parse(localStorage.getItem("ticketsSoporteWiNet")) || [];
    return cola.length > 0 ? cola[0] : null; // Peek: mira el frente
}

// Renderizar el contenido completo de la sección Mesa de Ayuda
function cargarModuloSoporte() {
    const contenedor = document.getElementById("seccion-soporte");
    if (!contenedor) return;

    const sesion = JSON.parse(localStorage.getItem("sesionActiva")) || {};
    const esAdmin = sesion.rol === "ADMIN" || sesion.rol === "SUPERADMIN";
    const cola = JSON.parse(localStorage.getItem("ticketsSoporteWiNet")) || [];
    const atendidos = JSON.parse(localStorage.getItem("ticketsAtendidosWiNet")) || [];
    const frente = consultarFrenteTicket();

    let html = `
        <div class="soporte">
            
            <!-- Encabezado del Módulo -->
            <div class="soporte__header">
                <div class="soporte__header-titulo">
                    <span>🎧</span>
                    <span>Mesa de Ayuda y Soporte Técnico (Cola FIFO)</span>
                </div>
                <div class="soporte__header-subtitulo">
                    Atención de requerimientos técnicos en orden de llegada (First-In, First-Out). El primer ticket que entra es el primero que se atiende.
                </div>
            </div>

            <!-- Distribución Formulario / Cola -->
            <div class="soporte__layout">
                
                <!-- Columna Izquierda: Formulario de Radicación -->
                <div class="soporte__columna">
                    <div class="soporte__seccion-titulo">
                        <span>📝 Radicar Solicitud de Soporte</span>
                        <span class="badge badge-admin" style="font-size: 11px;">ENCOLAR (Enqueue)</span>
                    </div>

                    <form id="formRadicarTicket" onsubmit="procesarRadicacionTicket(event)">
                        <div class="form-group">
                            <label for="soporteUsuario">Nombre del Solicitante:</label>
                            <input type="text" id="soporteUsuario" value="${(sesion.nombre || 'Usuario') + ' ' + (sesion.apellido || '')}" readonly style="background-color: #f8fafc;">
                        </div>

                        <div class="form-group">
                            <label for="soporteCorreo">Correo Electrónico de Contacto:</label>
                            <input type="email" id="soporteCorreo" value="${sesion.correo || 'soporte@cliente.com'}" required>
                        </div>

                        <div class="form-group">
                            <label for="soporteCategoria">Categoría del Incidente:</label>
                            <select id="soporteCategoria" required>
                                <option value="Falla de Conexión / Sin Señal">🔴 Falla de Conexión / Sin Señal</option>
                                <option value="Lentitud o Intermitencia">🟡 Lentitud o Intermitencia</option>
                                <option value="Configuración de Router / WiFi">🔵 Configuración de Router / WiFi</option>
                                <option value="Consulta de Facturación">🟢 Consulta de Facturación</option>
                                <option value="Traslado de Servicio">🟣 Traslado de Servicio</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="soporteAsunto">Asunto Breve:</label>
                            <input type="text" id="soporteAsunto" placeholder="Ej. Pérdida de señal intermitente en la tarde" required>
                        </div>

                        <div class="form-group">
                            <label for="soporteMensaje">Descripción Detallada del Problema:</label>
                            <textarea id="soporteMensaje" rows="3" placeholder="Explique con detalle qué sucede..." required></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary" style="margin-top: 5px;">
                            <span>📩</span> Radicar Ticket (Encolar al Final)
                        </button>
                    </form>
                </div>

                <!-- Columna Derecha: Cola FIFO en Tiempo Real -->
                <div class="soporte__columna">
                    <div class="soporte__seccion-titulo">
                        <span>👥 Cola de Espera en Vivo (FIFO)</span>
                        <span class="badge ${cola.length > 0 ? 'badge-alerta' : 'badge-activo'}" style="font-size: 12px;">
                            ${cola.length} en espera
                        </span>
                    </div>

                    <div class="soporte__cola-info">
                        <span><strong>Orden de atención:</strong> Desde el frente hacia atrás</span>
                        <span><strong>Próximo a atender:</strong> ${frente ? '#' + frente.idTicket : 'Ninguno'}</span>
                    </div>

                    <!-- Lista Visual de Nodos de la Cola -->
                    <div class="soporte__cola-lista">
    `;

    if (cola.length === 0) {
        html += `
            <div class="facturacion__tabla-vacia" style="padding: 40px 15px;">
                🎉 No hay solicitudes pendientes en la cola. ¡Todo el soporte está al día!
            </div>
        `;
    } else {
        cola.forEach((ticket, index) => {
            const esFrente = index === 0;
            html += `
                <div class="soporte__nodo ${esFrente ? 'soporte__nodo--frente' : ''}">
                    <div class="soporte__nodo-header">
                        <span class="soporte__nodo-id">
                            ${esFrente ? '🥇 FRENTE (1° en ser atendido)' : `Posición #${index + 1} en cola`}
                        </span>
                        <span class="${esFrente ? 'soporte__badge--frente' : 'soporte__badge--espera'}">
                            Ticket #${ticket.idTicket}
                        </span>
                    </div>
                    <div class="soporte__nodo-asunto">${ticket.asunto}</div>
                    <div class="soporte__nodo-mensaje">${ticket.mensaje}</div>
                    <div class="soporte__nodo-footer">
                        <span>👤 ${ticket.usuario} (${ticket.categoria || 'Técnico'})</span>
                        <span>📅 ${ticket.fecha}</span>
                    </div>
                </div>
            `;
        });
    }

    html += `
                    </div>

                    <!-- Botón para Atender Siguiente Ticket (Desencolar) -->
                    ${esAdmin ? `
                        <button class="soporte__btn-atender" onclick="atenderSiguienteTicketEnCola()" ${cola.length === 0 ? 'disabled' : ''}>
                            <span>⚡</span> Atender Siguiente Ticket (Desencolar FIFO)
                        </button>
                    ` : `
                        <div style="margin-top: 15px; font-size: 12px; color: #64748b; text-align: center; background: #f8fafc; padding: 10px; border-radius: 6px;">
                            ℹ️ Los asesores atienden los requerimientos estrictamente en el orden que ingresan a la cola.
                        </div>
                    `}

                </div>

            </div>

            <!-- Historial de Tickets Atendidos -->
            <div class="facturacion__tabla-card" style="margin-top: 10px;">
                <h4 style="font-size: 15px; font-weight: 700; color: #1e293b; margin-bottom: 12px;">
                    <span>✅</span> Historial de Tickets Atendidos (${atendidos.length})
                </h4>

                <div class="facturacion__tabla-contenedor">
                    <table class="facturacion__tabla">
                        <thead>
                            <tr>
                                <th>N° Ticket</th>
                                <th>Solicitante</th>
                                <th>Asunto</th>
                                <th>Categoría</th>
                                <th>Fecha Atención</th>
                                <th>Estado</th>
                            </tr>
                        </thead>
                        <tbody>
    `;

    if (atendidos.length === 0) {
        html += `
            <tr>
                <td colspan="6" class="facturacion__tabla-vacia">Aún no hay tickets atendidos en esta sesión.</td>
            </tr>
        `;
    } else {
        atendidos.forEach(t => {
            html += `
                <tr>
                    <td>#${t.idTicket}</td>
                    <td>${t.usuario}</td>
                    <td>${t.asunto}</td>
                    <td>${t.categoria || 'Soporte'}</td>
                    <td>${t.fechaAtencion || 'Hoy'}</td>
                    <td><span class="badge badge-activo">RESUELTO ✅</span></td>
                </tr>
            `;
        });
    }

    html += `
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    `;

    contenedor.innerHTML = html;
}

// Procesar formulario de radicación y encolar al final
function procesarRadicacionTicket(evento) {
    evento.preventDefault();

    const sesion = JSON.parse(localStorage.getItem("sesionActiva")) || {};
    const asunto = document.getElementById("soporteAsunto").value.trim();
    const categoria = document.getElementById("soporteCategoria").value;
    const mensaje = document.getElementById("soporteMensaje").value.trim();
    const correo = document.getElementById("soporteCorreo").value.trim();

    if (!asunto || !mensaje) {
        alert("Por favor complete el asunto y el mensaje.");
        return;
    }

    const cola = JSON.parse(localStorage.getItem("ticketsSoporteWiNet")) || [];
    const nuevoId = cola.length > 0 ? Math.max(...cola.map(t => t.idTicket)) + 1 : 1;
    const nombreUsuario = (sesion.nombre || 'Usuario') + ' ' + (sesion.apellido || '');

    const nuevoTicket = {
        idTicket: nuevoId,
        identificacion: sesion.identificacion || "1045678901",
        usuario: nombreUsuario,
        correo: correo,
        asunto: asunto,
        categoria: categoria,
        mensaje: mensaje,
        fecha: new Date().toLocaleDateString('es-CO'),
        estado: "PENDIENTE"
    };

    encolarTicket(nuevoTicket);
    alert(`¡Ticket #${nuevoTicket.idTicket} radicado con éxito y encolado en la posición #${cola.length + 1}!`);

    // Limpiar campos
    document.getElementById("soporteAsunto").value = "";
    document.getElementById("soporteMensaje").value = "";

    cargarModuloSoporte();
}

// Desencolar y atender el siguiente ticket en espera
function atenderSiguienteTicketEnCola() {
    const frente = consultarFrenteTicket();
    if (!frente) {
        alert("No hay tickets pendientes en la cola.");
        return;
    }

    const confirmacion = confirm(`¿Deseas atender el ticket #${frente.idTicket} del frente de la cola?\n\nSolicitante: ${frente.usuario}\nAsunto: ${frente.asunto}`);
    if (confirmacion) {
        const atendido = desencolarTicket();
        alert(`¡Ticket #${atendido.idTicket} de ${atendido.usuario} fue ATENDIDO con éxito! Se desencoló del frente de la cola (FIFO).`);
        cargarModuloSoporte();
    }
}


// ==========================================================================
// 4. COMPROBANTE OFICIAL DE FACTURA (MODAL / IMPRESIÓN PDF)
// ==========================================================================

function verComprobanteFactura(idFactura) {
    const pila = JSON.parse(localStorage.getItem("facturasWiNet")) || [];
    const factura = pila.find(f => f.idFactura === idFactura);

    if (!factura) {
        alert("No se encontró la factura solicitada.");
        return;
    }

    // Crear o recuperar el modal en el DOM
    let modal = document.getElementById("comprobanteModal");
    if (!modal) {
        modal = document.createElement("div");
        modal.id = "comprobanteModal";
        modal.className = "comprobante-modal";
        document.body.appendChild(modal);
    }

    modal.innerHTML = `
        <div class="comprobante-modal__caja">
            <div class="comprobante-modal__header">
                <span class="comprobante-modal__titulo">📄 Factura Oficial de Servicios - WiNet Control</span>
                <button class="comprobante-modal__cerrar" onclick="cerrarModalComprobante()">✕</button>
            </div>
            <div class="comprobante-modal__cuerpo" id="imprimibleFactura">
                <div class="comprobante-papel">
                    <div class="comprobante-papel__encabezado">
                        <div class="comprobante-papel__empresa">
                            <h3>🌐 WiNet Control S.A.S.</h3>
                            <p>NIT: 901.845.220-4 | Régimen Común</p>
                            <p>Sede Principal: Centro Empresarial Bocagrande, Cartagena</p>
                            <p>Línea de Atención: (605) 693-0000 | soporte@winetcontrol.com</p>
                        </div>
                        <div class="comprobante-papel__factura-id">
                            <h4>FACTURA DE VENTA</h4>
                            <p><strong>N° 00${factura.idFactura}</strong></p>
                            <p style="margin-top: 4px;">Fecha: ${factura.fecha}</p>
                            <span class="badge ${factura.estado === 'PAGADA' ? 'badge-activo' : 'badge-admin'}" style="margin-top: 6px;">
                                ${factura.estado}
                            </span>
                        </div>
                    </div>

                    <div class="comprobante-papel__datos-cliente">
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                            <div><strong>Suscriptor:</strong> ${factura.nombreCliente}</div>
                            <div><strong>C.C. / NIT:</strong> ${factura.identificacionCliente}</div>
                            <div><strong>Dirección:</strong> Cartagena de Indias, Bolívar</div>
                            <div><strong>Plan:</strong> ${factura.nombrePlan}</div>
                        </div>
                    </div>

                    <table class="comprobante-papel__detalle-tabla">
                        <thead>
                            <tr>
                                <th>Concepto</th>
                                <th>Periodo</th>
                                <th style="text-align: right;">Tarifa Mensual</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>Servicio de Internet Fibra Óptica - ${factura.nombrePlan}</td>
                                <td>Mes de Facturación</td>
                                <td style="text-align: right;">$${factura.monto.toLocaleString('es-CO')} COP</td>
                            </tr>
                            <tr>
                                <td>Subtotal</td>
                                <td></td>
                                <td style="text-align: right;">$${factura.monto.toLocaleString('es-CO')} COP</td>
                            </tr>
                            <tr>
                                <td>IVA (0% Exento Telecomunicaciones Educación)</td>
                                <td></td>
                                <td style="text-align: right;">$0 COP</td>
                            </tr>
                        </tbody>
                    </table>

                    <div class="comprobante-papel__total">
                        <div class="comprobante-papel__total-caja">
                            <span style="font-size: 12px; color: #64748b; font-weight: 600;">TOTAL A PAGAR:</span>
                            <h3>$${factura.monto.toLocaleString('es-CO')} COP</h3>
                        </div>
                    </div>

                    <div style="margin-top: 20px; font-size: 11px; color: #64748b; text-align: center; border-top: 1px dashed #cbd5e1; padding-top: 10px;">
                        ¡Gracias por confiar en WiNet Control! Recuerda mantener tus pagos al día para gozar de internet sin interrupciones.
                    </div>
                </div>
            </div>
            <div class="comprobante-modal__footer">
                <button class="facturacion__btn facturacion__btn--secundario" onclick="cerrarModalComprobante()">
                    Cerrar
                </button>
                <button class="facturacion__btn facturacion__btn--primario" onclick="window.print()">
                    <span>🖨️</span> Imprimir / Guardar PDF
                </button>
            </div>
        </div>
    `;

    modal.style.display = "flex";
}

function cerrarModalComprobante() {
    const modal = document.getElementById("comprobanteModal");
    if (modal) {
        modal.style.display = "none";
    }
}
