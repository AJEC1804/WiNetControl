/* ==========================================================================
   LÓGICA JAVASCRIPT - FACTURACIÓN (LIFO) Y SOPORTE TÉCNICO (FIFO)
   Autor: Juan Esteban Navia (Issue #10)
   Conectado a ApiServer (FacturaController / PilaFacturas y SoporteController / ColaSoporte)
   ========================================================================== */

const API_FACTURAS = "http://localhost:8080/api/facturas";
const API_SOPORTE  = "http://localhost:8080/api/soporte";
const API_CLIENTES_FACT = "http://localhost:8080/api/clientes";
const API_PLANES_FACT   = "http://localhost:8080/api/planes?estado=activos";

let facturasEnMemoria = [];
let cimaFacturaEnMemoria = null;
let ticketsPendientesEnMemoria = [];
let ticketsAtendidosEnMemoria = [];
let frenteTicketEnMemoria = null;
let clientesParaSelector = [];
let planesParaSelector = [];

function avisarToast(mensaje, tipo) {
    if (typeof notificar === "function") {
        notificar(mensaje, tipo || "exito");
    }
}


// ==========================================================================
// 1. MÓDULO DE FACTURACIÓN: ESTRUCTURA PILA (LIFO)
// ==========================================================================

async function cargarModuloFacturas() {
    const contenedor = document.getElementById("seccion-facturas");
    if (!contenedor) return;

    const sesion = JSON.parse(localStorage.getItem("sesionActiva")) || {};
    const rol = (sesion.rol || "CLIENTE").toUpperCase();
    const esAdmin = (rol === "ADMIN" || rol === "SUPERADMIN");
    const esEmpleado = (rol === "EMPLEADO");
    const esPersonal = (esAdmin || esEmpleado);

    const url = esPersonal
        ? API_FACTURAS
        : API_FACTURAS + "?usuario=" + encodeURIComponent(sesion.identificacion || "");

    try {
        const respuesta = await fetch(url);
        const json = await respuesta.json();
        facturasEnMemoria = (json && Array.isArray(json.datos)) ? json.datos : [];
        cimaFacturaEnMemoria = esPersonal
            ? (json.cima || facturasEnMemoria[0] || null)
            : (facturasEnMemoria[0] || null);
    } catch (error) {
        contenedor.innerHTML = `
            <div class="facturacion">
                <div class="facturacion__tabla-card" style="text-align:center; padding:32px; color:#b91c1c;">
                    ⚠️ No se pudo conectar con el servidor Java (<code>http://localhost:8080</code>). Verifica que <strong>ApiServer</strong> esté encendido.
                </div>
            </div>
        `;
        return;
    }

    const cima = cimaFacturaEnMemoria;
    const facturas = facturasEnMemoria;

    let html = `
        <div class="facturacion">
            
            <!-- Encabezado del Módulo (Diseño original de Juanes) -->
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
                        <span>${esPersonal ? 'Cima de la Pila LIFO (Última Factura Emitida en el Sistema)' : 'Mi Última Factura Emitida'}</span>
                    </div>
                    <span class="facturacion__badge--cima">${esPersonal ? 'TOPE DE LA PILA (LIFO)' : 'ÚLTIMO PERIODO'}</span>
                </div>
    `;

    if (cima) {
        const montoCima = Number(cima.monto || 0);
        const pagadaCima = cima.estado === "PAGADA";
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
                        <span class="facturacion__dato-valor facturacion__dato-valor--monto">$${montoCima.toLocaleString('es-CO')} COP</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Estado</span>
                        <span class="facturacion__dato-valor" style="color: ${pagadaCima ? '#16a34a' : '#2563eb'};">
                            ${pagadaCima ? 'Al Día ✅' : 'Emitida ⏳'}
                        </span>
                    </div>
                </div>

                <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                    <button class="facturacion__btn facturacion__btn--primario" onclick="verComprobanteFactura(${cima.idFactura})">
                        <span>👁️</span> Ver Comprobante Oficial (PDF)
                    </button>
                    ${!pagadaCima ? `
                        <button class="facturacion__btn facturacion__btn--secundario" onclick="registrarPagoFactura(${cima.idFactura})">
                            <span>💳</span> ${esPersonal ? 'Registrar Pago' : 'Pagar Factura'}
                        </button>
                    ` : ''}
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

    // Barra de herramientas para Administradores y Empleados
    if (esPersonal) {
        html += `
            <div class="facturacion__toolbar">
                <div style="font-weight: 600; font-size: 14px; color: #1e293b;">
                    <span>⚙️</span> Panel de Control de Facturación (${esAdmin ? 'Administrador' : 'Empleado'})
                </div>
                <div class="facturacion__toolbar-acciones">
                    <button class="facturacion__btn facturacion__btn--primario" onclick="abrirModalEmitirFactura()">
                        <span>➕</span> Emitir Nueva Factura (Apilar / Push)
                    </button>
                </div>
            </div>
        `;
    }

    // Tabla de Historial de Facturas + Buscador por Cliente
    html += `
            <div class="facturacion__tabla-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                    <h4 style="font-size: 15px; font-weight: 700; color: #1e293b;">
                        <span>📋</span> ${esPersonal ? 'Historial Global de Facturas en la Pila' : 'Mis Facturas de Servicio'}
                    </h4>
                    <span id="contadorFacturasTabla" style="font-size: 12px; color: #64748b;">Total apiladas: ${facturas.length}</span>
                </div>

                ${esPersonal ? `
                <div class="modulo-toolbar" style="margin-bottom: 12px;">
                    <input type="text" id="buscarFacturaCliente" class="campo-busqueda"
                           placeholder="🔍  Buscar facturas por cédula o nombre del cliente..."
                           oninput="filtrarTablaFacturas()">

                    <select id="filtroEstadoFactura" onchange="filtrarTablaFacturas()">
                        <option value="TODOS">Todos los estados</option>
                        <option value="EMITIDA">Solo emitidas</option>
                        <option value="PAGADA">Solo pagadas</option>
                    </select>

                    <button type="button" class="btn btn-secondary btn-auto" onclick="limpiarFiltroFacturas()">
                        🔄 Ver todas
                    </button>
                </div>
                ` : ''}

                <div class="facturacion__tabla-contenedor">
                    <table class="facturacion__tabla">
                        <thead>
                            <tr>
                                <th>Posición</th>
                                <th>N° Factura</th>
                                <th>Cédula</th>
                                <th>Suscriptor</th>
                                <th>Plan Contratado</th>
                                <th>Fecha Emisión</th>
                                <th>Total</th>
                                <th>Estado</th>
                                <th>Acción</th>
                            </tr>
                        </thead>
                        <tbody id="cuerpoTablaFacturas">
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Modal para Emitir Nueva Factura -->
            <div id="modalEmitirFactura" class="modal-cli-fondo">
                <div class="modal-cli-caja">
                    <h3>➕ Emitir Nueva Factura</h3>
                    <p class="nota-obligatorio">Selecciona el cliente y el plan para generar el comprobante PDF y apilarlo en la cima.</p>

                    <div class="form-row">
                        <div class="form-group" style="grid-column: 1 / -1;">
                            <label for="factClienteSelect">Cliente suscriptor <span class="obligatorio">*</span></label>
                            <select id="factClienteSelect" onchange="alCambiarClienteFactura()">
                                <option value="">Seleccione un cliente...</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group" style="grid-column: 1 / -1;">
                            <label for="factPlanSelect">Plan a facturar <span class="obligatorio">*</span></label>
                            <select id="factPlanSelect">
                                <option value="">Seleccione un plan...</option>
                            </select>
                        </div>
                    </div>

                    <div class="modal-cli-acciones">
                        <button type="button" class="btn btn-secondary btn-auto" onclick="cerrarModalEmitirFactura()">Cancelar</button>
                        <button type="button" class="btn btn-primary btn-auto" onclick="guardarNuevaFactura()">Emitir factura</button>
                    </div>
                </div>
            </div>

        </div>
    `;

    contenedor.innerHTML = html;
    renderizarFilasFacturas(facturas, esPersonal);
}

function renderizarFilasFacturas(lista, esPersonal) {
    const tbody = document.getElementById("cuerpoTablaFacturas");
    if (!tbody) return;

    if (esPersonal === undefined) {
        const sesion = JSON.parse(localStorage.getItem("sesionActiva")) || {};
        const rol = (sesion.rol || "CLIENTE").toUpperCase();
        esPersonal = (rol === "ADMIN" || rol === "SUPERADMIN" || rol === "EMPLEADO");
    }

    const contador = document.getElementById("contadorFacturasTabla");
    if (contador) {
        contador.textContent = "Mostrando " + lista.length + " de " + facturasEnMemoria.length;
    }

    if (lista.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="facturacion__tabla-vacia">No se encontraron facturas registradas.</td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = lista.map((fact) => {
        const esTope = cimaFacturaEnMemoria && Number(fact.idFactura) === Number(cimaFacturaEnMemoria.idFactura);
        const pos = facturasEnMemoria.findIndex(f => Number(f.idFactura) === Number(fact.idFactura)) + 1;
        const monto = Number(fact.monto || 0);
        const pagada = fact.estado === "PAGADA";

        return `
            <tr style="${esTope ? 'background-color: #eff6ff; font-weight: 600;' : ''}">
                <td>
                    ${esTope ? '<span class="facturacion__badge--cima">CIMA</span>' : `#${pos}`}
                </td>
                <td>#00${fact.idFactura}</td>
                <td><span class="cli-cedula">${fact.identificacionCliente || '-'}</span></td>
                <td>${fact.nombreCliente}</td>
                <td>${fact.nombrePlan}</td>
                <td>${fact.fecha}</td>
                <td style="color: #16a34a; font-weight: 700;">$${monto.toLocaleString('es-CO')}</td>
                <td>
                    <span class="badge ${pagada ? 'badge-activo' : 'badge-admin'}">
                        ${fact.estado}
                    </span>
                </td>
                <td style="display:flex; gap:6px; flex-wrap:wrap;">
                    <button class="facturacion__btn facturacion__btn--secundario" style="padding: 4px 10px; font-size: 12px;" onclick="verComprobanteFactura(${fact.idFactura})">
                        <span>📄</span> Ver PDF
                    </button>
                    ${!pagada ? `
                        <button class="facturacion__btn facturacion__btn--primario" style="padding: 4px 10px; font-size: 12px;" onclick="registrarPagoFactura(${fact.idFactura})">
                            <span>💳</span> ${esPersonal ? 'Marcar pagada' : 'Pagar'}
                        </button>
                    ` : ''}
                </td>
            </tr>
        `;
    }).join("");
}

function filtrarTablaFacturas() {
    const input = document.getElementById("buscarFacturaCliente");
    const select = document.getElementById("filtroEstadoFactura");
    const texto = input ? input.value.trim().toLowerCase() : "";
    const estado = select ? select.value : "TODOS";

    const filtradas = facturasEnMemoria.filter(function (f) {
        const coincideCliente = texto === ""
            || (f.identificacionCliente && f.identificacionCliente.toLowerCase().includes(texto))
            || (f.nombreCliente && f.nombreCliente.toLowerCase().includes(texto))
            || String(f.idFactura).includes(texto);

        const coincideEstado = estado === "TODOS"
            || (f.estado && f.estado.toUpperCase() === estado);

        return coincideCliente && coincideEstado;
    });

    renderizarFilasFacturas(filtradas);
}

function limpiarFiltroFacturas() {
    cargarModuloFacturas();
}

async function registrarPagoFactura(idFactura) {
    try {
        const respuesta = await fetch(API_FACTURAS + "/pagar", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ idFactura: String(idFactura) })
        });
        const json = await respuesta.json();
        if (!json.exito) {
            avisarToast(json.mensaje || "No se pudo registrar el pago.", "error");
            return;
        }
        avisarToast("Pago registrado para la factura #00" + idFactura + ".", "exito");
        cargarModuloFacturas();
    } catch (error) {
        avisarToast("No se pudo conectar con el servidor Java.", "error");
    }
}

async function confirmarDesapilarFactura() {
    const cima = cimaFacturaEnMemoria;
    if (!cima) {
        avisarToast("La pila de facturas ya está vacía.", "error");
        return;
    }

    let confirmado = false;
    if (typeof confirmarAccion === "function") {
        confirmado = await confirmarAccion(
            "Desapilar última factura",
            'Se retirará de la cima la factura #00' + cima.idFactura + ' de "' + cima.nombreCliente + '".',
            "Sí, desapilar"
        );
    } else {
        confirmado = confirm(`¿Deseas desapilar (Pop LIFO) la factura #00${cima.idFactura} de ${cima.nombreCliente}?`);
    }

    if (!confirmado) return;

    try {
        const respuesta = await fetch(API_FACTURAS + "/cima", { method: "DELETE" });
        const json = await respuesta.json();
        if (!json.exito) {
            avisarToast(json.mensaje || "No se pudo desapilar la factura.", "error");
            return;
        }
        avisarToast("Factura #00" + cima.idFactura + " desapilada exitosamente.", "exito");
        cargarModuloFacturas();
    } catch (error) {
        avisarToast("Error al conectar con el servidor Java.", "error");
    }
}

async function abrirModalEmitirFactura() {
    try {
        const [respCli, respPla] = await Promise.all([
            fetch(API_CLIENTES_FACT),
            fetch(API_PLANES_FACT)
        ]);
        const jsonCli = await respCli.json();
        const jsonPla = await respPla.json();
        clientesParaSelector = (jsonCli && Array.isArray(jsonCli.datos)) ? jsonCli.datos : [];
        planesParaSelector = (jsonPla && Array.isArray(jsonPla.datos)) ? jsonPla.datos : [];
    } catch (error) {
        avisarToast("No se pudo consultar clientes o planes en el servidor.", "error");
        return;
    }

    const selCliente = document.getElementById("factClienteSelect");
    const selPlan = document.getElementById("factPlanSelect");
    if (!selCliente || !selPlan) return;

    selCliente.innerHTML = '<option value="">Seleccione un cliente...</option>';
    clientesParaSelector.forEach(function (c) {
        const planTexto = c.planActual ? " — " + c.planActual : " — Sin plan";
        const opt = document.createElement("option");
        opt.value = c.identificacion;
        opt.textContent = c.identificacion + " · " + c.nombre + " " + c.apellido + planTexto;
        selCliente.appendChild(opt);
    });

    selPlan.innerHTML = '<option value="">Seleccione un plan...</option>';
    planesParaSelector.forEach(function (p) {
        const precio = Number(p.precio || 0).toLocaleString("es-CO");
        const opt = document.createElement("option");
        opt.value = p.nombrePlan;
        opt.textContent = p.nombrePlan + " (" + p.velocidadPlan + " — $" + precio + ")";
        selPlan.appendChild(opt);
    });

    if (clientesParaSelector.length > 0) {
        selCliente.value = clientesParaSelector[0].identificacion;
        alCambiarClienteFactura();
    }

    const modal = document.getElementById("modalEmitirFactura");
    if (modal) modal.style.display = "flex";
}

function alCambiarClienteFactura() {
    const selCliente = document.getElementById("factClienteSelect");
    const selPlan = document.getElementById("factPlanSelect");
    if (!selCliente || !selPlan) return;

    const cliente = clientesParaSelector.find(c => c.identificacion === selCliente.value);
    if (cliente && cliente.planActual) {
        selPlan.value = cliente.planActual;
    } else if (planesParaSelector.length > 0 && !selPlan.value) {
        selPlan.value = planesParaSelector[0].nombrePlan;
    }
}

function cerrarModalEmitirFactura() {
    const modal = document.getElementById("modalEmitirFactura");
    if (modal) modal.style.display = "none";
}

async function guardarNuevaFactura() {
    const cedula = document.getElementById("factClienteSelect").value;
    const plan = document.getElementById("factPlanSelect").value;

    if (!cedula) {
        avisarToast("Selecciona un cliente para emitir la factura.", "error");
        return;
    }

    try {
        const resp = await fetch(API_FACTURAS, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ identificacion: cedula, plan: plan })
        });
        const json = await resp.json();
        if (!json.exito) {
            avisarToast(json.mensaje || "No se pudo emitir la factura.", "error");
            return;
        }
        cerrarModalEmitirFactura();
        const num = json.datos ? "#00" + json.datos.idFactura : "";
        avisarToast("Factura " + num + " emitida y apilada en la cima.", "exito");
        cargarModuloFacturas();
    } catch (error) {
        avisarToast("Error al conectar con el servidor Java.", "error");
    }
}


// ==========================================================================
// 2. MÓDULO DE MESA DE AYUDA: ESTRUCTURA COLA (FIFO)
// ==========================================================================

async function cargarModuloSoporte() {
    const contenedor = document.getElementById("seccion-soporte");
    if (!contenedor) return;

    const sesion = JSON.parse(localStorage.getItem("sesionActiva")) || {};
    const rol = (sesion.rol || "CLIENTE").toUpperCase();
    const puedeAtender = (rol === "ADMIN" || rol === "SUPERADMIN" || rol === "EMPLEADO");

    try {
        const respuesta = await fetch(API_SOPORTE);
        const json = await respuesta.json();
        ticketsPendientesEnMemoria = (json && Array.isArray(json.pendientes)) ? json.pendientes : [];
        ticketsAtendidosEnMemoria  = (json && Array.isArray(json.atendidos))  ? json.atendidos  : [];
        frenteTicketEnMemoria      = (json && json.frente) ? json.frente : (ticketsPendientesEnMemoria[0] || null);
    } catch (error) {
        contenedor.innerHTML = `
            <div class="soporte">
                <div class="facturacion__tabla-card" style="text-align:center; padding:32px; color:#b91c1c;">
                    ⚠️ No se pudo conectar con el servidor Java (<code>http://localhost:8080</code>). Verifica que <strong>ApiServer</strong> esté encendido.
                </div>
            </div>
        `;
        return;
    }

    const nombreSesion = ((sesion.nombre || "Usuario") + " " + (sesion.apellido || "")).trim();

    // Si es SUPERADMIN, ADMIN o EMPLEADO: ve la Cola FIFO en vivo (sin formulario de enviar soporte) + Historial de atendidos
    if (puedeAtender) {
        const cola = ticketsPendientesEnMemoria;
        const atendidos = ticketsAtendidosEnMemoria;
        const frente = frenteTicketEnMemoria;

        let html = `
            <div class="soporte">
                
                <div class="soporte__header">
                    <div class="soporte__header-titulo">
                        <span>🎧</span>
                        <span>Mesa de Ayuda y Soporte Técnico (Cola FIFO)</span>
                    </div>
                    <div class="soporte__header-subtitulo">
                        Atención de requerimientos técnicos en orden de llegada (First-In, First-Out). El primer ticket que entra es el primero que se atiende.
                    </div>
                </div>

                <!-- Cola FIFO en Tiempo Real (Diseño original de Juanes) -->
                <div class="soporte__columna">
                    <div class="soporte__seccion-titulo">
                        <span>👥 Cola de Espera en Vivo (FIFO)</span>
                        <span class="badge ${cola.length > 0 ? 'badge-alerta' : 'badge-activo'}" style="font-size: 12px;">
                            ${cola.length} en espera
                        </span>
                    </div>

                    <div class="soporte__cola-info">
                        <span><strong>Orden de atención:</strong> Desde el frente hacia atrás</span>
                        <span><strong>Próximo a atender:</strong> ${frente ? '#' + frente.idTicket + ' (' + frente.usuario + ')' : 'Ninguno'}</span>
                    </div>

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
                            <span>👤 ${ticket.usuario} (${ticket.correo || 'Sin correo'})</span>
                            <span>📅 ${ticket.fecha}</span>
                        </div>
                    </div>
                `;
            });
        }

        html += `
                    </div>

                    <button class="soporte__btn-atender" onclick="atenderSiguienteTicketEnCola()" ${cola.length === 0 ? 'disabled' : ''}>
                        <span>⚡</span> Atender Siguiente Ticket (Desencolar FIFO)
                    </button>
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
                                    <th>Correo</th>
                                    <th>Fecha</th>
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
                        <td>${t.correo || '-'}</td>
                        <td>${t.fecha || 'Hoy'}</td>
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
        return;
    }

    // Si es CLIENTE: ve el layout de 2 columnas de Juanes (Formulario a la izquierda + Cola a la derecha)
    const cola = ticketsPendientesEnMemoria.filter(function (t) {
        return (t.usuario && t.usuario.toLowerCase() === nombreSesion.toLowerCase())
            || (t.correo && sesion.correo && t.correo.toLowerCase() === sesion.correo.toLowerCase());
    });
    const atendidos = ticketsAtendidosEnMemoria.filter(function (t) {
        return (t.usuario && t.usuario.toLowerCase() === nombreSesion.toLowerCase())
            || (t.correo && sesion.correo && t.correo.toLowerCase() === sesion.correo.toLowerCase());
    });
    const frente = cola[0] || null;

    let html = `
        <div class="soporte">
            
            <div class="soporte__header">
                <div class="soporte__header-titulo">
                    <span>🎧</span>
                    <span>Mesa de Ayuda y Soporte Técnico (Cola FIFO)</span>
                </div>
                <div class="soporte__header-subtitulo">
                    Atención de requerimientos técnicos en orden de llegada (First-In, First-Out). El primer ticket que entra es el primero que se atiende.
                </div>
            </div>

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
                            <input type="text" id="soporteUsuario" value="${nombreSesion}" readonly style="background-color: #f8fafc;">
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

                <!-- Columna Derecha: Mis Solicitudes en Cola -->
                <div class="soporte__columna">
                    <div class="soporte__seccion-titulo">
                        <span>👥 Mis Solicitudes en Espera (FIFO)</span>
                        <span class="badge ${cola.length > 0 ? 'badge-alerta' : 'badge-activo'}" style="font-size: 12px;">
                            ${cola.length} en espera
                        </span>
                    </div>

                    <div class="soporte__cola-info">
                        <span><strong>Orden de atención:</strong> Desde el frente hacia atrás</span>
                        <span><strong>Mi ticket actual:</strong> ${frente ? '#' + frente.idTicket : 'Ninguno'}</span>
                    </div>

                    <div class="soporte__cola-lista">
    `;

    if (cola.length === 0) {
        html += `
            <div class="facturacion__tabla-vacia" style="padding: 40px 15px;">
                🎉 No tienes solicitudes pendientes en la cola.
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
                        <span>👤 ${ticket.usuario}</span>
                        <span>📅 ${ticket.fecha}</span>
                    </div>
                </div>
            `;
        });
    }

    html += `
                    </div>

                    <div style="margin-top: 15px; font-size: 12px; color: #64748b; text-align: center; background: #f8fafc; padding: 10px; border-radius: 6px;">
                        ℹ️ Los asesores atienden los requerimientos estrictamente en el orden que ingresan a la cola.
                    </div>
                </div>

            </div>

            <!-- Historial de Tickets Atendidos -->
            <div class="facturacion__tabla-card" style="margin-top: 10px;">
                <h4 style="font-size: 15px; font-weight: 700; color: #1e293b; margin-bottom: 12px;">
                    <span>✅</span> Mis Tickets Atendidos (${atendidos.length})
                </h4>

                <div class="facturacion__tabla-contenedor">
                    <table class="facturacion__tabla">
                        <thead>
                            <tr>
                                <th>N° Ticket</th>
                                <th>Solicitante</th>
                                <th>Asunto</th>
                                <th>Correo</th>
                                <th>Fecha</th>
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
                    <td>${t.correo || '-'}</td>
                    <td>${t.fecha || 'Hoy'}</td>
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

async function procesarRadicacionTicket(evento) {
    evento.preventDefault();

    const usuario = document.getElementById("soporteUsuario").value.trim();
    const asunto = document.getElementById("soporteAsunto").value.trim();
    const categoria = document.getElementById("soporteCategoria").value;
    const mensaje = document.getElementById("soporteMensaje").value.trim();
    const correo = document.getElementById("soporteCorreo").value.trim();

    if (!asunto || !mensaje) {
        avisarToast("Completa el asunto y la descripción del problema.", "error");
        return;
    }

    try {
        const respuesta = await fetch(API_SOPORTE, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                usuario: usuario,
                correo: correo,
                categoria: categoria,
                asunto: asunto,
                mensaje: mensaje
            })
        });
        const json = await respuesta.json();
        if (!json.exito) {
            avisarToast(json.mensaje || "No se pudo radicar el ticket.", "error");
            return;
        }
        const idNuevo = json.datos ? "#" + json.datos.idTicket : "";
        avisarToast("Ticket " + idNuevo + " radicado y encolado exitosamente.", "exito");
        cargarModuloSoporte();
    } catch (error) {
        avisarToast("No se pudo conectar con el servidor Java.", "error");
    }
}

async function atenderSiguienteTicketEnCola() {
    const frente = frenteTicketEnMemoria;
    if (!frente) {
        avisarToast("No hay tickets pendientes en la cola.", "error");
        return;
    }

    let confirmado = false;
    if (typeof confirmarAccion === "function") {
        confirmado = await confirmarAccion(
            "Atender siguiente ticket",
            'Se atenderá el Ticket #' + frente.idTicket + ' de "' + frente.usuario + '" (' + frente.asunto + ').',
            "Sí, atender"
        );
    } else {
        confirmado = confirm(`¿Deseas atender el ticket #${frente.idTicket} del frente de la cola?`);
    }

    if (!confirmado) return;

    try {
        const respuesta = await fetch(API_SOPORTE + "/atender", { method: "POST" });
        const json = await respuesta.json();
        if (!json.exito) {
            avisarToast(json.mensaje || "No se pudo atender el ticket.", "error");
            return;
        }
        avisarToast("Ticket #" + frente.idTicket + " de " + frente.usuario + " atendido con éxito.", "exito");
        cargarModuloSoporte();
    } catch (error) {
        avisarToast("No se pudo conectar con el servidor Java.", "error");
    }
}


// ==========================================================================
// 3. COMPROBANTE OFICIAL DE FACTURA (MODAL / IMPRESIÓN PDF)
// ==========================================================================

function verComprobanteFactura(idFactura) {
    const factura = facturasEnMemoria.find(f => Number(f.idFactura) === Number(idFactura))
        || (cimaFacturaEnMemoria && Number(cimaFacturaEnMemoria.idFactura) === Number(idFactura) ? cimaFacturaEnMemoria : null);

    if (!factura) {
        avisarToast("No se encontró la factura solicitada.", "error");
        return;
    }

    const monto = Number(factura.monto || 0);
    const urlPdfReal = `${API_FACTURAS}/pdf?id=${factura.idFactura}`;

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
                            <div><strong>Archivo PDF:</strong> <code>${factura.archivoPdf || '-'}</code></div>
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
                                <td style="text-align: right;">$${monto.toLocaleString('es-CO')} COP</td>
                            </tr>
                            <tr>
                                <td>Subtotal</td>
                                <td></td>
                                <td style="text-align: right;">$${monto.toLocaleString('es-CO')} COP</td>
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
                            <h3>$${monto.toLocaleString('es-CO')} COP</h3>
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
                <a class="facturacion__btn facturacion__btn--secundario" href="${urlPdfReal}" target="_blank" style="text-decoration:none;">
                    <span>📥</span> Abrir PDF Original
                </a>
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
