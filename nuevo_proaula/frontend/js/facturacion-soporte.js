const API_FACTURAS = API_BASE + "/api/facturas";
const API_SOPORTE = API_BASE + "/api/soporte";
const API_CLIENTES_FACT = API_BASE + "/api/clientes";
const API_PLANES_FACT = API_BASE + "/api/planes?estado=activos";

let facturasEnMemoria = [];
let cimaFacturaEnMemoria = null;
let ticketsPendientesEnMemoria = [];
let ticketsEnProcesoEnMemoria = [];
let ticketsAtendidosEnMemoria = [];
let frenteTicketEnMemoria = null;
let clientesParaSelector = [];
let planesParaSelector = [];

function avisarToast(mensaje, tipo) {
    if (typeof notificar === "function") {
        notificar(mensaje, tipo || "exito");
    }
}

function escaparHtml(texto) {
    if (texto === null || texto === undefined) return "";
    return String(texto)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

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
                    ⚠️ No se pudo conectar con el servidor Java (<code>${API_BASE}</code>). Verifica que <strong>ApiServer</strong> esté encendido.
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
        const anuladaCima = cima.estado === "ANULADA";
        html += `
                <div class="facturacion__cima-grid">
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Factura N°</span>
                        <span class="facturacion__dato-valor">#00${cima.idFactura}</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Suscriptor</span>
                        <span class="facturacion__dato-valor">${escaparHtml(cima.nombreCliente)}</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Plan Asignado</span>
                        <span class="facturacion__dato-valor">${escaparHtml(cima.nombrePlan)}</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Fecha de Emisión</span>
                        <span class="facturacion__dato-valor">${escaparHtml(cima.fecha)}</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Monto Total</span>
                        <span class="facturacion__dato-valor facturacion__dato-valor--monto">$${montoCima.toLocaleString('es-CO')} COP</span>
                    </div>
                    <div class="facturacion__dato">
                        <span class="facturacion__dato-etiqueta">Estado</span>
                        <span class="facturacion__dato-valor" style="color: ${pagadaCima ? '#16a34a' : (anuladaCima ? '#dc2626' : '#d97706')};">
                            ${pagadaCima ? 'Pagada ✅' : (anuladaCima ? 'Anulada ⛔' : 'Pendiente ⏳')}
                        </span>
                    </div>
                </div>

                <div style="display: flex; gap: 10px; flex-wrap: wrap;">
                    <button class="facturacion__btn facturacion__btn--primario" onclick="verComprobanteFactura(${cima.idFactura})">
                        <span>👁️</span> Ver Comprobante Oficial (PDF)
                    </button>
                    ${!pagadaCima && !anuladaCima ? `
                        <button class="facturacion__btn facturacion__btn--secundario" onclick="registrarPagoFactura(${cima.idFactura})">
                            <span>💳</span> ${esPersonal ? 'Registrar Pago' : 'Pagar Factura'}
                        </button>
                    ` : ''}
                    ${pagadaCima && esPersonal ? `
                        <button class="facturacion__btn facturacion__btn--secundario" onclick="editarEstadoFactura(${cima.idFactura}, 'PENDIENTE')">
                            <span>↩️</span> Volver a Pendiente
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
                        <option value="PENDIENTE">Pendientes de pago</option>
                        <option value="PAGADA">Solo pagadas</option>
                        <option value="ANULADA">Solo anuladas</option>
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
                    <p class="nota-obligatorio">Se factura únicamente el plan contratado por el cliente para el mes y año seleccionados (máximo 1 factura por mes).</p>

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
                            <label for="factPlanTexto">Plan contratado del cliente</label>
                            <input type="text" id="factPlanTexto" readonly style="background-color: #f8fafc; color: #334155; font-weight: 600;" value="Seleccione un cliente...">
                            <small id="factInfoPlan" style="display: block; margin-top: 6px; color: #64748b; font-size: 12px;"></small>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="factMesSelect">Mes <span class="obligatorio">*</span></label>
                            <select id="factMesSelect">
                                <option value="1">Enero</option>
                                <option value="2">Febrero</option>
                                <option value="3">Marzo</option>
                                <option value="4">Abril</option>
                                <option value="5">Mayo</option>
                                <option value="6">Junio</option>
                                <option value="7">Julio</option>
                                <option value="8">Agosto</option>
                                <option value="9">Septiembre</option>
                                <option value="10">Octubre</option>
                                <option value="11">Noviembre</option>
                                <option value="12">Diciembre</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="factAnoInput">Año <span class="obligatorio">*</span></label>
                            <input type="number" id="factAnoInput" min="2020" max="2100" required>
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
        const anulada = fact.estado === "ANULADA";
        const estadoFila = String(fact.estado || "EMITIDA").toUpperCase();
        const badgeClase = pagada ? "badge-activo" : (anulada ? "badge-inactivo" : "badge-alerta");

        return `
            <tr class="fila-${estadoFila.toLowerCase()}" style="${esTope ? 'background-color: #eff6ff; font-weight: 600;' : ''}">
                <td>
                    ${esTope ? '<span class="facturacion__badge--cima">CIMA</span>' : `#${pos}`}
                </td>
                <td>#00${Number(fact.idFactura)}</td>
                <td><span class="cli-cedula">${escaparHtml(fact.identificacionCliente || '-')}</span></td>
                <td>${escaparHtml(fact.nombreCliente)}</td>
                <td>${escaparHtml(fact.nombrePlan)}</td>
                <td>${escaparHtml(fact.fecha)}</td>
                <td style="color: #16a34a; font-weight: 700;">$${monto.toLocaleString('es-CO')}</td>
                <td>
                    <span class="badge ${badgeClase}">
                        ${escaparHtml(fact.estado)}
                    </span>
                </td>
                <td style="display:flex; gap:6px; flex-wrap:wrap;">
                    <button class="facturacion__btn facturacion__btn--secundario" style="padding: 4px 10px; font-size: 12px;" onclick="verComprobanteFactura(${fact.idFactura})">
                        <span>📄</span> Ver PDF
                    </button>
                    ${!pagada && !anulada ? `
                        <button class="facturacion__btn facturacion__btn--primario" style="padding: 4px 10px; font-size: 12px;" onclick="registrarPagoFactura(${fact.idFactura})">
                            <span>💳</span> ${esPersonal ? 'Marcar pagada' : 'Pagar'}
                        </button>
                    ` : ''}
                    ${pagada && esPersonal ? `
                        <button class="facturacion__btn facturacion__btn--secundario" style="padding: 4px 10px; font-size: 12px;" onclick="editarEstadoFactura(${fact.idFactura}, 'PENDIENTE')" title="Cambiar estado a pendiente de pago">
                            <span>↩️</span> Volver a pendiente
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

        const est = f.estado ? f.estado.toUpperCase() : "";
        const coincideEstado = estado === "TODOS"
            || (estado === "PENDIENTE" && (est === "PENDIENTE" || est === "EMITIDA"))
            || (est === estado);

        return coincideCliente && coincideEstado;
    });

    renderizarFilasFacturas(filtradas);
}

function limpiarFiltroFacturas() {
    cargarModuloFacturas();
}

async function editarEstadoFactura(idFactura, nuevoEstado) {
    let confirmado = true;
    if (typeof confirmarAccion === "function") {
        confirmado = await confirmarAccion(
            "Editar estado de factura",
            "¿Deseas cambiar la factura #00" + idFactura + " al estado " + nuevoEstado + "?",
            "Sí, cambiar estado"
        );
    }
    if (!confirmado) return;

    try {
        const respuesta = await fetch(API_FACTURAS + "/estado", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ idFactura: String(idFactura), estado: nuevoEstado })
        });
        const json = await respuesta.json();
        if (!json.exito) {
            avisarToast(json.mensaje || "No se pudo actualizar el estado de la factura.", "error");
            return;
        }
        avisarToast(json.mensaje || "Factura #00" + idFactura + " actualizada a " + nuevoEstado + ".", "exito");
        cargarModuloFacturas();
    } catch (error) {
        avisarToast("No se pudo conectar con el servidor Java.", "error");
    }
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
    const selMes = document.getElementById("factMesSelect");
    const inpAno = document.getElementById("factAnoInput");
    if (!selCliente) return;

    selCliente.innerHTML = '<option value="">Seleccione un cliente...</option>';
    clientesParaSelector.forEach(function (c) {
        const planTexto = c.planActual ? " — " + c.planActual : " — Sin plan";
        const opt = document.createElement("option");
        opt.value = c.identificacion;
        opt.textContent = c.identificacion + " · " + c.nombre + " " + c.apellido + planTexto;
        selCliente.appendChild(opt);
    });

    const hoy = new Date();
    if (selMes) selMes.value = String(hoy.getMonth() + 1);
    if (inpAno) inpAno.value = String(hoy.getFullYear());

    if (clientesParaSelector.length > 0) {
        selCliente.value = clientesParaSelector[0].identificacion;
        alCambiarClienteFactura();
    }

    const modal = document.getElementById("modalEmitirFactura");
    if (modal) modal.style.display = "flex";
}

function alCambiarClienteFactura() {
    const selCliente = document.getElementById("factClienteSelect");
    const txtPlan = document.getElementById("factPlanTexto");
    const infoPlan = document.getElementById("factInfoPlan");
    if (!selCliente || !txtPlan) return;

    const cliente = clientesParaSelector.find(c => c.identificacion === selCliente.value);
    if (!cliente) {
        txtPlan.value = "Seleccione un cliente...";
        if (infoPlan) infoPlan.textContent = "";
        return;
    }

    const nombrePlan = (cliente.planActual || "").trim();
    if (!nombrePlan || nombrePlan.toLowerCase() === "sin plan") {
        txtPlan.value = "Sin plan contratado (no se puede facturar)";
        if (infoPlan) infoPlan.textContent = "";
        return;
    }

    const planObj = planesParaSelector.find(p =>
        (p.nombre || "").trim().toLowerCase() === nombrePlan.toLowerCase()
    );
    if (planObj) {
        const precio = Number(planObj.precio || 0).toLocaleString("es-CO");
        txtPlan.value = planObj.nombre + " (" + planObj.velocidad + " — $" + precio + ")";
    } else {
        txtPlan.value = nombrePlan;
    }

    if (infoPlan) {
        const activas = facturasEnMemoria.filter(f => {
            const est = String(f.estado || "").toUpperCase();
            return f.identificacionCliente === cliente.identificacion && (est === "EMITIDA" || est === "PENDIENTE");
        }).length;
        const fechaInicio = cliente.fechaInicioPlan || cliente.fechaRegistro || "Sin fecha";
        infoPlan.textContent = "Adquirido el: " + fechaInicio + " · Facturas activas (pendientes): " + activas;
    }
}

function cerrarModalEmitirFactura() {
    const modal = document.getElementById("modalEmitirFactura");
    if (modal) modal.style.display = "none";
}

async function guardarNuevaFactura() {
    const cedula = document.getElementById("factClienteSelect").value;
    const mes = Number(document.getElementById("factMesSelect").value);
    const ano = Number(document.getElementById("factAnoInput").value);

    if (!cedula) {
        avisarToast("Selecciona un cliente para emitir la factura.", "error");
        return;
    }

    const cliente = clientesParaSelector.find(c => c.identificacion === cedula);
    const nombrePlan = cliente && cliente.planActual ? cliente.planActual.trim() : "";
    if (!nombrePlan || nombrePlan.toLowerCase() === "sin plan") {
        avisarToast("El cliente seleccionado no tiene un plan contratado.", "error");
        return;
    }

    if (!mes || mes < 1 || mes > 12 || !ano || ano < 2020 || ano > 2100) {
        avisarToast("Selecciona un mes y año válidos.", "error");
        return;
    }

    try {
        const resp = await fetch(API_FACTURAS, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ identificacion: cedula, mes: mes, ano: ano })
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

async function cargarModuloSoporte() {
    const contenedor = document.getElementById("seccion-soporte");
    if (!contenedor) return;

    const sesion = JSON.parse(localStorage.getItem("sesionActiva")) || {};
    const rol = (sesion.rol || "CLIENTE").toUpperCase();
    const esEmpPlataforma = (rol === "EMPLEADO" && sesion.empresaId === "PLATAFORMA");
    const puedeAtender = (rol === "ADMIN" || rol === "SUPERADMIN" || rol === "EMPLEADO");
    const esModoPlataformaAtencion = (rol === "SUPERADMIN" || esEmpPlataforma);

    try {
        const respuesta = await fetch(API_SOPORTE);
        const json = await respuesta.json();
        ticketsPendientesEnMemoria = (json && Array.isArray(json.pendientes)) ? json.pendientes : [];
        ticketsEnProcesoEnMemoria = (json && Array.isArray(json.enProceso)) ? json.enProceso : [];
        ticketsAtendidosEnMemoria = (json && Array.isArray(json.atendidos)) ? json.atendidos : [];
        frenteTicketEnMemoria = (json && json.frente) ? json.frente : (ticketsPendientesEnMemoria[0] || null);
    } catch (error) {
        contenedor.innerHTML = `
            <div class="soporte">
                <div class="facturacion__tabla-card" style="text-align:center; padding:32px; color:#b91c1c;">
                    ⚠️ No se pudo conectar con el servidor Java (<code>${API_BASE}</code>). Verifica que <strong>ApiServer</strong> esté encendido.
                </div>
            </div>
        `;
        return;
    }

    const nombreSesion = ((sesion.nombre || "Usuario") + " " + (sesion.apellido || "")).trim();

    if (puedeAtender) {
        const cola = ticketsPendientesEnMemoria;
        const enProceso = ticketsEnProcesoEnMemoria;
        const atendidos = ticketsAtendidosEnMemoria;
        const frente = frenteTicketEnMemoria;

        const tituloModulo = esModoPlataformaAtencion
            ? "Tickets de Administradores — Soporte Plataforma (Cola FIFO)"
            : "Mesa de Ayuda y Soporte Técnico (Cola FIFO)";
        const subtituloModulo = esModoPlataformaAtencion
            ? "Inicia el siguiente ticket reportado por un administrador, revisa el problema y escribe la solución para notificarle por correo y web al cerrarlo."
            : "Inicia el siguiente ticket en orden de llegada (FIFO), revisa el problema y registra la observación/solución antes de cerrarlo.";

        let html = `
            <div class="soporte">
                <div class="soporte__header">
                    <div class="soporte__header-titulo">
                        <span>🎧</span>
                        <span>${tituloModulo}</span>
                    </div>
                    <div class="soporte__header-subtitulo">${subtituloModulo}</div>
                </div>

                <!-- PANEL DE TICKETS INICIADOS (EN PROCESO) PARA DAR SOLUCIÓN Y CERRAR -->
                <div class="soporte__columna">
                    <div class="soporte__seccion-titulo">
                        <span>🛠️ Tickets Iniciados (Esperando Observación / Solución)</span>
                        <span class="badge ${enProceso.length > 0 ? 'badge-alerta' : 'badge-activo'}" style="font-size: 12px;">
                            ${enProceso.length} en proceso
                        </span>
                    </div>
        `;

        if (enProceso.length === 0) {
            html += `
                <div class="facturacion__tabla-vacia" style="padding: 20px;">
                    No hay tickets iniciados ahora mismo. Presiona <strong>"▶️ Iniciar Siguiente Ticket (FIFO)"</strong> en la cola de abajo para ver el problema y responderlo.
                </div>
            `;
        } else {
            html += `<div style="display:flex; flex-direction:column; gap:14px;">`;
            enProceso.forEach(t => {
                html += `
                    <div class="soporte__en-proceso-card">
                        <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
                            <div style="font-weight:800; font-size:15px; color:#1e293b;">
                                🎫 Ticket #${Number(t.idTicket)} — ${escaparHtml(t.asunto)}
                            </div>
                            <span class="badge badge-alerta">EN PROCESO</span>
                        </div>
                        <div style="font-size:12.5px; color:#64748b; margin-top:6px;">
                            👤 <strong>Solicitante:</strong> ${escaparHtml(t.usuario)} (${escaparHtml(t.correo || 'Sin correo')}) ·
                            🏢 <strong>Empresa:</strong> ${escaparHtml(t.empresaNombre || t.empresaId)} ·
                            📅 <strong>Iniciado:</strong> ${escaparHtml(t.fechaInicio || t.fecha)}
                        </div>
                        <div class="soporte__detalle-problema">
                            <h5>📌 Problema Reportado por el Usuario:</h5>
                            <p>${escaparHtml(t.mensaje)}</p>
                        </div>
                        <div class="soporte__observacion-box">
                            <label for="obsTicket_${t.idTicket}" style="font-size:13px; font-weight:700; color:#1e293b;">
                                ✍️ Observación / Solución para el usuario <span class="obligatorio">*</span>
                            </label>
                            <textarea id="obsTicket_${t.idTicket}" rows="3" placeholder="Describe detalladamente la solución aplicada o respuesta para el usuario..."></textarea>
                        </div>
                        <div style="display:flex; justify-content:flex-end; margin-top:12px;">
                            <button type="button" class="btn btn-primary btn-auto" onclick="cerrarTicketConObservacion(${t.idTicket}, false)">
                                ✅ Cerrar Ticket y Notificar (Correo + Web)
                            </button>
                        </div>
                    </div>
                `;
            });
            html += `</div>`;
        }

        html += `
                </div>

                <!-- Cola FIFO en Tiempo Real -->
                <div class="soporte__columna">
                    <div class="soporte__seccion-titulo">
                        <span>👥 Cola de Espera en Vivo (FIFO)</span>
                        <span class="badge ${cola.length > 0 ? 'badge-alerta' : 'badge-activo'}" style="font-size: 12px;">
                            ${cola.length} en espera
                        </span>
                    </div>

                    <div class="soporte__cola-info">
                        <span><strong>Orden de atención:</strong> Desde el frente hacia atrás</span>
                        <span><strong>Próximo a iniciar:</strong> ${frente ? '#' + Number(frente.idTicket) + ' (' + escaparHtml(frente.usuario) + ')' : 'Ninguno'}</span>
                    </div>

                    <div class="soporte__cola-lista">
        `;

        if (cola.length === 0) {
            html += `
                <div class="facturacion__tabla-vacia" style="padding: 34px 15px;">
                    🎉 No hay solicitudes pendientes en la cola. ¡Todo el soporte está al día!
                </div>
            `;
        } else {
            cola.forEach((ticket, index) => {
                const esFrente = index === 0;
                html += `
                    <div class="soporte__nodo ${esFrente ? 'soporte__nodo--frente' : 'soporte__nodo--espera'}">
                        <div class="soporte__nodo-header">
                            <span class="soporte__nodo-id">
                                ${esFrente ? '🥇 FRENTE (1° en iniciar)' : `Posición #${index + 1} en cola`}
                            </span>
                            <span class="${esFrente ? 'soporte__badge--frente' : 'soporte__badge--espera'}">
                                Ticket #${ticket.idTicket}
                            </span>
                        </div>
                        <div class="soporte__nodo-asunto">${escaparHtml(ticket.asunto)}</div>
                        <div class="soporte__nodo-mensaje">${escaparHtml(ticket.mensaje)}</div>
                        <div class="soporte__nodo-footer">
                            <span>👤 ${escaparHtml(ticket.usuario)} (${escaparHtml(ticket.correo || 'Sin correo')})</span>
                            <span>📅 ${escaparHtml(ticket.fecha)}</span>
                        </div>
                    </div>
                `;
            });
        }

        html += `
                    </div>

                    <button class="soporte__btn-atender" onclick="iniciarSiguienteTicketEnCola()" ${cola.length === 0 ? 'disabled' : ''}>
                        <span>▶️</span> Iniciar Siguiente Ticket (Desencolar FIFO para Responder)
                    </button>
                </div>

                <!-- Historial de Tickets Cerrados -->
                <div class="facturacion__tabla-card" style="margin-top: 10px;">
                    <h4 style="font-size: 15px; font-weight: 700; color: #1e293b; margin-bottom: 12px;">
                        <span>✅</span> Historial de Tickets Cerrados (${atendidos.length})
                    </h4>

                    <div class="facturacion__tabla-contenedor">
                        <table class="facturacion__tabla">
                            <thead>
                                <tr>
                                    <th>N° Ticket</th>
                                    <th>Solicitante</th>
                                    <th>Asunto</th>
                                    <th>Observación / Solución</th>
                                    <th>Atendido por</th>
                                    <th>Fecha Cierre</th>
                                    <th>Estado</th>
                                    <th>Detalle</th>
                                </tr>
                            </thead>
                            <tbody>
        `;

        if (atendidos.length === 0) {
            html += `
                <tr>
                    <td colspan="8" class="facturacion__tabla-vacia">Aún no hay tickets cerrados.</td>
                </tr>
            `;
        } else {
            atendidos.forEach(t => {
                html += `
                    <tr class="fila-atendido">
                        <td>#${Number(t.idTicket)}</td>
                        <td>${escaparHtml(t.usuario)}</td>
                        <td>${escaparHtml(t.asunto)}</td>
                        <td style="max-width:260px;">${escaparHtml(t.observacion || 'Sin observación')}</td>
                        <td>${escaparHtml(t.atendidoPor || '-')}</td>
                        <td>${escaparHtml(t.fechaCierre || t.fecha || 'Hoy')}</td>
                        <td><span class="badge badge-activo">${escaparHtml(t.estado || 'CERRADO')} ✅</span></td>
                        <td>
                            <button type="button" class="facturacion__btn facturacion__btn--secundario" style="padding:4px 10px; font-size:12px;" onclick="verDetalleTicket(${Number(t.idTicket)})">
                                👁️ Ver
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
        return;
    }

    renderizarVistaRemitenteTickets(contenedor, {
        titulo: "Mesa de Ayuda y Soporte Técnico (Cola FIFO)",
        subtitulo: "Radica tu requerimiento. Al iniciar y cerrar tu ticket, recibirás la solución por correo y notificaciones web.",
        formId: "formRadicarTicket",
        onSubmitFn: "procesarRadicacionTicket(event)",
        nombreSesion: nombreSesion,
        correoSesion: sesion.correo || "",
        esPlataforma: false,
        pendientes: ticketsPendientesEnMemoria,
        enProceso: ticketsEnProcesoEnMemoria,
        atendidos: ticketsAtendidosEnMemoria
    });
}

async function cargarModuloSoportePlataforma() {
    const contenedor = document.getElementById("seccion-soporte-plataforma");
    if (!contenedor) return;

    const sesion = JSON.parse(localStorage.getItem("sesionActiva")) || {};
    const nombreSesion = ((sesion.nombre || "Administrador") + " " + (sesion.apellido || "")).trim();

    try {
        const respuesta = await fetch(API_SOPORTE + "?ambito=plataforma");
        const json = await respuesta.json();
        const pendientes = (json && Array.isArray(json.pendientes)) ? json.pendientes : [];
        const enProceso = (json && Array.isArray(json.enProceso)) ? json.enProceso : [];
        const atendidos = (json && Array.isArray(json.atendidos)) ? json.atendidos : [];

        ticketsAtendidosEnMemoria = atendidos;
        ticketsEnProcesoEnMemoria = enProceso;
        ticketsPendientesEnMemoria = pendientes;

        renderizarVistaRemitenteTickets(contenedor, {
            titulo: "Reportar Problema con la Plataforma (Al Superusuario)",
            subtitulo: "Envía reportes de fallos, solicitudes o dudas al Superusuario y su equipo de soporte de plataforma.",
            formId: "formRadicarTicketPlataforma",
            onSubmitFn: "procesarRadicacionTicketPlataforma(event)",
            nombreSesion: nombreSesion,
            correoSesion: sesion.correo || "",
            esPlataforma: true,
            pendientes: pendientes,
            enProceso: enProceso,
            atendidos: atendidos
        });
    } catch (error) {
        contenedor.innerHTML = `
            <div class="soporte">
                <div class="facturacion__tabla-card" style="text-align:center; padding:32px; color:#b91c1c;">
                    ⚠️ No se pudo conectar con ApiServer para cargar tus reportes de plataforma.
                </div>
            </div>
        `;
    }
}

function renderizarVistaRemitenteTickets(contenedor, cfg) {
    const cola = cfg.pendientes || [];
    const enProceso = cfg.enProceso || [];
    const atendidos = cfg.atendidos || [];
    const frente = cola[0] || null;
    const sufijo = cfg.esPlataforma ? "Plat" : "";

    const opcionesCategoria = cfg.esPlataforma
        ? `
            <option value="Error del Sistema / Bug">🔴 Error del Sistema / Bug</option>
            <option value="Facturación o Pago SaaS">💳 Facturación o Pago SaaS</option>
            <option value="Permisos o Cuentas">🔐 Permisos o Cuentas</option>
            <option value="Solicitud de Mejora">✨ Solicitud de Mejora</option>
            <option value="Otro Inconveniente">🟣 Otro Inconveniente</option>
          `
        : `
            <option value="Falla de Conexión / Sin Señal">🔴 Falla de Conexión / Sin Señal</option>
            <option value="Lentitud o Intermitencia">🟡 Lentitud o Intermitencia</option>
            <option value="Configuración de Router / WiFi">🔵 Configuración de Router / WiFi</option>
            <option value="Consulta de Facturación">🟢 Consulta de Facturación</option>
            <option value="Traslado de Servicio">🟣 Traslado de Servicio</option>
          `;

    let html = `
        <div class="soporte">
            <div class="soporte__header">
                <div class="soporte__header-titulo">
                    <span>${cfg.esPlataforma ? '🛠️' : '🎧'}</span>
                    <span>${cfg.titulo}</span>
                </div>
                <div class="soporte__header-subtitulo">${cfg.subtitulo}</div>
            </div>

            <div class="soporte__layout">
                <div class="soporte__columna">
                    <div class="soporte__seccion-titulo">
                        <span>📝 ${cfg.esPlataforma ? 'Reportar Problema al Superusuario' : 'Radicar Solicitud de Soporte'}</span>
                        <span class="badge badge-admin" style="font-size: 11px;">ENCOLAR (FIFO)</span>
                    </div>

                    <form id="${cfg.formId}" onsubmit="${cfg.onSubmitFn}">
                        <div class="form-group">
                            <label for="soporteUsuario${sufijo}">Solicitante:</label>
                            <input type="text" id="soporteUsuario${sufijo}" value="${escaparHtml(cfg.nombreSesion)}" readonly style="background-color: #f8fafc;">
                        </div>

                        <div class="form-group">
                            <label for="soporteCorreo${sufijo}">Correo donde recibirás la respuesta:</label>
                            <input type="email" id="soporteCorreo${sufijo}" value="${escaparHtml(cfg.correoSesion)}" required>
                        </div>

                        <div class="form-group">
                            <label for="soporteCategoria${sufijo}">Categoría:</label>
                            <select id="soporteCategoria${sufijo}" required>${opcionesCategoria}</select>
                        </div>

                        <div class="form-group">
                            <label for="soporteAsunto${sufijo}">Asunto Breve:</label>
                            <input type="text" id="soporteAsunto${sufijo}" placeholder="Describe brevemente el problema..." required>
                        </div>

                        <div class="form-group">
                            <label for="soporteMensaje${sufijo}">Descripción Detallada del Problema:</label>
                            <textarea id="soporteMensaje${sufijo}" rows="3" placeholder="Explica paso a paso lo que sucede..." required></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary" style="margin-top: 5px;">
                            <span>📩</span> Enviar Reporte / Ticket
                        </button>
                    </form>
                </div>

                <div class="soporte__columna">
                    <div class="soporte__seccion-titulo">
                        <span>👥 Mis Tickets Activos (${cola.length + enProceso.length})</span>
                        <span class="badge ${(cola.length + enProceso.length) > 0 ? 'badge-alerta' : 'badge-activo'}" style="font-size: 12px;">
                            ${enProceso.length} en atención · ${cola.length} en cola
                        </span>
                    </div>

                    <div class="soporte__cola-info">
                        <span><strong>Orden:</strong> FIFO (por llegada)</span>
                        <span><strong>En cola:</strong> ${frente ? '#' + frente.idTicket : 'Ninguno'}</span>
                    </div>

                    <div class="soporte__cola-lista">
    `;

    if (enProceso.length === 0 && cola.length === 0) {
        html += `
            <div class="facturacion__tabla-vacia" style="padding: 36px 15px;">
                🎉 No tienes tickets pendientes o en atención actualmente.
            </div>
        `;
    } else {
        enProceso.forEach(ticket => {
            html += `
                <div class="soporte__nodo soporte__nodo--frente" style="border-left-color:#d97706; background:#fffbeb;">
                    <div class="soporte__nodo-header">
                        <span class="soporte__nodo-id">🛠️ EN PROCESO (siendo atendido por ${escaparHtml(ticket.atendidoPor || 'Soporte')})</span>
                        <span class="soporte__badge--frente">Ticket #${ticket.idTicket}</span>
                    </div>
                    <div class="soporte__nodo-asunto">${escaparHtml(ticket.asunto)}</div>
                    <div class="soporte__nodo-mensaje">${escaparHtml(ticket.mensaje)}</div>
                    <div class="soporte__nodo-footer">
                        <span>📅 Iniciado: ${escaparHtml(ticket.fechaInicio || ticket.fecha)}</span>
                    </div>
                </div>
            `;
        });
        cola.forEach((ticket, index) => {
            const esFrente = index === 0;
            html += `
                <div class="soporte__nodo ${esFrente ? 'soporte__nodo--frente' : ''}">
                    <div class="soporte__nodo-header">
                        <span class="soporte__nodo-id">${esFrente ? '🥇 FRENTE (1° en cola)' : `Posición #${index + 1} en cola`}</span>
                        <span class="${esFrente ? 'soporte__badge--frente' : 'soporte__badge--espera'}">Ticket #${ticket.idTicket}</span>
                    </div>
                    <div class="soporte__nodo-asunto">${escaparHtml(ticket.asunto)}</div>
                    <div class="soporte__nodo-mensaje">${escaparHtml(ticket.mensaje)}</div>
                    <div class="soporte__nodo-footer">
                        <span>👤 ${escaparHtml(ticket.usuario)}</span>
                        <span>📅 ${escaparHtml(ticket.fecha)}</span>
                    </div>
                </div>
            `;
        });
    }

    html += `
                    </div>
                </div>
            </div>

            <div class="facturacion__tabla-card" style="margin-top: 10px;">
                <h4 style="font-size: 15px; font-weight: 700; color: #1e293b; margin-bottom: 12px;">
                    <span>✅</span> Mis Tickets Resueltos / Cerrados (${atendidos.length})
                </h4>

                <div class="facturacion__tabla-contenedor">
                    <table class="facturacion__tabla">
                        <thead>
                            <tr>
                                <th>N° Ticket</th>
                                <th>Asunto</th>
                                <th>Observación / Solución</th>
                                <th>Atendido por</th>
                                <th>Fecha Cierre</th>
                                <th>Estado</th>
                                <th>Detalle</th>
                            </tr>
                        </thead>
                        <tbody>
    `;

    if (atendidos.length === 0) {
        html += '<tr><td colspan="7" class="facturacion__tabla-vacia">Aún no tienes tickets cerrados.</td></tr>';
    } else {
        atendidos.forEach(t => {
            html += `
                <tr class="fila-atendido">
                    <td>#${Number(t.idTicket)}</td>
                    <td>${escaparHtml(t.asunto)}</td>
                    <td style="max-width:280px;">${escaparHtml(t.observacion || 'Sin observación')}</td>
                    <td>${escaparHtml(t.atendidoPor || '-')}</td>
                    <td>${escaparHtml(t.fechaCierre || t.fecha || 'Hoy')}</td>
                    <td><span class="badge badge-activo">${escaparHtml(t.estado || 'CERRADO')} ✅</span></td>
                    <td>
                        <button type="button" class="facturacion__btn facturacion__btn--secundario" style="padding:4px 10px; font-size:12px;" onclick="verDetalleTicket(${Number(t.idTicket)})">
                            👁️ Ver
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

async function procesarRadicacionTicket(evento) {
    evento.preventDefault();
    await enviarTicketAlServidor("", "CLIENTE", cargarModuloSoporte);
}

async function procesarRadicacionTicketPlataforma(evento) {
    evento.preventDefault();
    await enviarTicketAlServidor("Plat", "PLATAFORMA", cargarModuloSoportePlataforma);
}

async function enviarTicketAlServidor(sufijo, tipoTicket, recargarFn) {
    const usuario = document.getElementById("soporteUsuario" + sufijo).value.trim();
    const asunto = document.getElementById("soporteAsunto" + sufijo).value.trim();
    const categoria = document.getElementById("soporteCategoria" + sufijo).value;
    const mensaje = document.getElementById("soporteMensaje" + sufijo).value.trim();
    const correo = document.getElementById("soporteCorreo" + sufijo).value.trim();

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
                mensaje: mensaje,
                tipoTicket: tipoTicket
            })
        });
        const json = await respuesta.json();
        if (!json.exito) {
            avisarToast(json.mensaje || "No se pudo radicar el ticket.", "error");
            return;
        }
        const idNuevo = json.datos ? "#" + json.datos.idTicket : "";
        avisarToast("Ticket " + idNuevo + " radicado y encolado exitosamente.", "exito");
        recargarFn();
    } catch (error) {
        avisarToast("No se pudo conectar con el servidor Java.", "error");
    }
}

async function iniciarSiguienteTicketEnCola() {
    const frente = frenteTicketEnMemoria;
    if (!frente) {
        avisarToast("No hay tickets pendientes en la cola.", "error");
        return;
    }

    try {
        const respuesta = await fetch(API_SOPORTE + "/iniciar", { method: "POST" });
        const json = await respuesta.json();
        if (!json.exito) {
            avisarToast(json.mensaje || "No se pudo iniciar el ticket.", "error");
            return;
        }
        avisarToast("Ticket #" + frente.idTicket + " iniciado. Revisa el problema y registra la solución.", "info");
        cargarModuloSoporte();
    } catch (error) {
        avisarToast("No se pudo conectar con el servidor Java.", "error");
    }
}

async function cerrarTicketConObservacion(idTicket) {
    const campo = document.getElementById("obsTicket_" + idTicket);
    const observacion = campo ? campo.value.trim() : "";

    if (!observacion) {
        avisarToast("Debes escribir una observación o solución antes de cerrar el ticket.", "error");
        if (campo) campo.focus();
        return;
    }

    try {
        const respuesta = await fetch(API_SOPORTE + "/cerrar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ idTicket: String(idTicket), observacion: observacion })
        });
        const json = await respuesta.json();
        if (!json.exito) {
            avisarToast(json.mensaje || "No se pudo cerrar el ticket.", "error");
            return;
        }
        avisarToast("Ticket #" + idTicket + " cerrado y notificado al usuario por correo y web.", "exito");
        cargarModuloSoporte();
        if (typeof cargarNotificacionesWeb === "function") cargarNotificacionesWeb();
    } catch (error) {
        avisarToast("No se pudo conectar con el servidor Java.", "error");
    }
}

function verDetalleTicket(idTicket) {
    const t = ticketsAtendidosEnMemoria.find(x => Number(x.idTicket) === Number(idTicket))
        || ticketsEnProcesoEnMemoria.find(x => Number(x.idTicket) === Number(idTicket))
        || ticketsPendientesEnMemoria.find(x => Number(x.idTicket) === Number(idTicket));
    if (!t) return;

    let modal = document.getElementById("comprobanteModal");
    if (!modal) {
        modal = document.createElement("div");
        modal.id = "comprobanteModal";
        modal.className = "comprobante-modal";
        document.body.appendChild(modal);
    }

    modal.innerHTML = `
        <div class="comprobante-modal__caja" style="max-width: 620px;">
            <div class="comprobante-modal__header">
                <span class="comprobante-modal__titulo">🎫 Detalle del Ticket #${Number(t.idTicket)}</span>
                <button class="comprobante-modal__cerrar" onclick="cerrarModalComprobante()">✕</button>
            </div>
            <div class="comprobante-modal__cuerpo" style="padding: 22px;">
                <p style="margin:0 0 8px;"><strong>Asunto:</strong> ${escaparHtml(t.asunto)}</p>
                <p style="margin:0 0 8px;"><strong>Solicitante:</strong> ${escaparHtml(t.usuario)} (${escaparHtml(t.correo || '-')})</p>
                <p style="margin:0 0 12px;"><strong>Estado:</strong> <span class="badge badge-activo">${escaparHtml(t.estado)}</span></p>
                <div class="soporte__detalle-problema">
                    <h5>📌 Problema Reportado:</h5>
                    <p>${escaparHtml(t.mensaje)}</p>
                </div>
                <div class="soporte__detalle-problema" style="border-color:#bbf7d0; background:#f0fdf4;">
                    <h5>✅ Observación / Solución:</h5>
                    <p>${escaparHtml(t.observacion || 'Sin observación registrada.')}</p>
                    <small style="display:block; margin-top:8px; color:#64748b;">
                        Atendido por: ${escaparHtml(t.atendidoPor || '-')} · Fecha cierre: ${escaparHtml(t.fechaCierre || t.fecha || '-')}
                    </small>
                </div>
            </div>
            <div class="comprobante-modal__footer">
                <button class="facturacion__btn facturacion__btn--primario" onclick="cerrarModalComprobante()">Cerrar</button>
            </div>
        </div>
    `;
    modal.style.display = "flex";
}

function verComprobanteFactura(idFactura) {
    const factura = facturasEnMemoria.find(f => Number(f.idFactura) === Number(idFactura))
        || (cimaFacturaEnMemoria && Number(cimaFacturaEnMemoria.idFactura) === Number(idFactura) ? cimaFacturaEnMemoria : null);

    if (!factura) {
        avisarToast("No se encontró la factura solicitada.", "error");
        return;
    }

    const monto = Number(factura.monto || 0);
    const tokenPdf = (typeof window.obtenerTokenSesion === "function") ? window.obtenerTokenSesion() : "";
    const urlPdfReal = `${API_FACTURAS}/pdf?id=${factura.idFactura}`
        + (tokenPdf ? `&token=${encodeURIComponent(tokenPdf)}` : "");

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
                            <p style="margin-top: 4px;">Fecha: ${escaparHtml(factura.fecha)}</p>
                            <span class="badge ${factura.estado === 'PAGADA' ? 'badge-activo' : 'badge-admin'}" style="margin-top: 6px;">
                                ${escaparHtml(factura.estado)}
                            </span>
                        </div>
                    </div>

                    <div class="comprobante-papel__datos-cliente">
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                            <div><strong>Suscriptor:</strong> ${escaparHtml(factura.nombreCliente)}</div>
                            <div><strong>C.C. / NIT:</strong> ${escaparHtml(factura.identificacionCliente)}</div>
                            <div><strong>Archivo PDF:</strong> <code>${escaparHtml(factura.archivoPdf || '-')}</code></div>
                            <div><strong>Plan:</strong> ${escaparHtml(factura.nombrePlan)}</div>
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
                                <td>Servicio de Internet Fibra Óptica - ${escaparHtml(factura.nombrePlan)}</td>
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
