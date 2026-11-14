/* ==========================================================================
   ALERTAS DE ERROR  (WiNet Control)
   Archivo: js/errores.js  ·  Se carga antes que app.js y planes.js.
   --------------------------------------------------------------------------
   Red de seguridad global: avisa al usuario de cualquier error posible sin
   tener que repetir el manejo en cada llamada.

   Cubre los tres tipos de fallo:
     1. La API no responde (servidor apagado, cable, IP equivocada).
     2. La API responde con error (400, 401, 403, 404, 500...): se muestra el
        mensaje que devuelve el backend, que es el que explica la regla rota.
     3. Excepción de JavaScript no capturada (un fallo de programación).

   Además expone window.alertarError(titulo, detalle) para usarlo a mano desde
   cualquier módulo.
   ========================================================================== */

(function () {
    "use strict";

    var CONTENEDOR_ID = "alertasGlobales";
    var DURACION_MS = 9000;      // tiempo que queda visible una alerta
    var REPETICION_MS = 4000;    // ventana para no repetir la misma alerta
    var ultimas = {};            // clave -> instante del último aviso

    // ------------------------------------------------------------------
    // 1. CONTENEDOR (creado por JS con estilos propios, para que funcione
    //    también en el login, que no carga planes.css)
    // ------------------------------------------------------------------
    function obtenerContenedor() {
        var caja = document.getElementById(CONTENEDOR_ID);
        if (caja) {
            return caja;
        }
        caja = document.createElement("div");
        caja.id = CONTENEDOR_ID;
        caja.setAttribute("role", "alert");
        caja.setAttribute("aria-live", "assertive");
        caja.style.cssText = [
            "position:fixed", "right:18px", "bottom:18px", "z-index:99999",
            "display:flex", "flex-direction:column", "gap:10px",
            "max-width:390px", "pointer-events:none"
        ].join(";");
        document.body.appendChild(caja);
        return caja;
    }

    // ------------------------------------------------------------------
    // 2. ALERTA VISIBLE
    // ------------------------------------------------------------------
    function alertarError(titulo, detalle) {
        var clave = titulo + "|" + detalle;
        var ahora = Date.now();
        if (ultimas[clave] && (ahora - ultimas[clave]) < REPETICION_MS) {
            return;   // se evita repetir el mismo aviso en ráfaga
        }
        ultimas[clave] = ahora;

        var caja = obtenerContenedor();

        var alerta = document.createElement("div");
        alerta.style.cssText = [
            "pointer-events:auto", "background:#ffffff", "border:1px solid #fecaca",
            "border-left:5px solid #dc2626", "border-radius:10px",
            "box-shadow:0 12px 28px -10px rgba(15,23,42,.35)",
            "padding:12px 14px", "font-family:inherit", "color:#1e293b",
            "opacity:0", "transform:translateY(8px)",
            "transition:opacity .18s ease, transform .18s ease"
        ].join(";");

        var cabecera = document.createElement("div");
        cabecera.style.cssText = "display:flex;align-items:flex-start;gap:8px;";
        cabecera.innerHTML =
            '<span style="font-size:16px;line-height:1.2">⛔</span>' +
            '<strong style="font-size:13.5px;color:#b91c1c;flex:1">' + escapar(titulo) + "</strong>";

        var cerrar = document.createElement("button");
        cerrar.type = "button";
        cerrar.textContent = "×";
        cerrar.setAttribute("aria-label", "Cerrar alerta");
        cerrar.style.cssText = [
            "background:none", "border:none", "cursor:pointer", "font-size:18px",
            "line-height:1", "color:#94a3b8", "padding:0 2px"
        ].join(";");
        cabecera.appendChild(cerrar);

        var texto = document.createElement("p");
        texto.style.cssText = "margin:6px 0 0 24px;font-size:12.5px;line-height:1.45;color:#475569;word-break:break-word;";
        texto.textContent = detalle;

        alerta.appendChild(cabecera);
        alerta.appendChild(texto);
        caja.appendChild(alerta);

        var salir = function () {
            alerta.style.opacity = "0";
            alerta.style.transform = "translateY(8px)";
            setTimeout(function () {
                if (alerta.parentNode) {
                    alerta.parentNode.removeChild(alerta);
                }
            }, 200);
        };

        cerrar.addEventListener("click", salir);
        requestAnimationFrame(function () {
            alerta.style.opacity = "1";
            alerta.style.transform = "translateY(0)";
        });
        setTimeout(salir, DURACION_MS);

        // Queda también en consola para poder revisarlo después
        console.error("[alerta] " + titulo + " — " + detalle);
    }

    function escapar(t) {
        var d = document.createElement("div");
        d.textContent = t == null ? "" : String(t);
        return d.innerHTML;
    }

    // Texto corto de la ruta, para no llenar la alerta con la URL completa
    function rutaDe(url) {
        if (!url) {
            return "petición";
        }
        try {
            var limpia = String(url).replace(/^https?:\/\/[^/]+/, "");
            return limpia.split("?")[0] || "/";
        } catch (e) {
            return "petición";
        }
    }

    // ------------------------------------------------------------------
    // 3. INTERCEPTOR DE fetch
    //    No hay que modificar las llamadas existentes: todas pasan por aquí.
    // ------------------------------------------------------------------
    var fetchOriginal = window.fetch ? window.fetch.bind(window) : null;

    if (fetchOriginal) {
        window.fetch = function (recurso, opciones) {
            var url = (typeof recurso === "string") ? recurso : (recurso && recurso.url) || "";

            return fetchOriginal(recurso, opciones).then(function (respuesta) {
                if (!respuesta.ok) {
                    // Se lee una copia para no consumir el cuerpo que espera el llamador
                    respuesta.clone().json().then(function (json) {
                        var mensaje = (json && (json.mensaje || json.message)) || "";
                        alertarError(
                            "Error " + respuesta.status + " en el servidor",
                            (mensaje ? mensaje + " · " : "") + rutaDe(url)
                        );
                    }).catch(function () {
                        alertarError(
                            "Error " + respuesta.status + " en el servidor",
                            "La operación no se pudo completar · " + rutaDe(url)
                        );
                    });
                }
                return respuesta;
            }, function (error) {
                alertarError(
                    "No se pudo conectar con la API",
                    "No hubo respuesta de " + (window.API_BASE || "el servidor") +
                    ". Verifica que la API esté en ejecución (INICIAR_API.bat) · " + rutaDe(url)
                );
                error.__yaAlertado = true;   // evita el aviso duplicado de unhandledrejection
                throw error;
            });
        };
    }

    // ------------------------------------------------------------------
    // 4. ERRORES NO CAPTURADOS
    // ------------------------------------------------------------------
    window.addEventListener("error", function (evento) {
        var origen = evento.filename ? " (" + String(evento.filename).split("/").pop() + ":" + evento.lineno + ")" : "";
        alertarError("Error de JavaScript", (evento.message || "Fallo inesperado") + origen);
    });

    window.addEventListener("unhandledrejection", function (evento) {
        var motivo = evento.reason;
        if (motivo && motivo.__yaAlertado) {
            return;   // ya se avisó desde el interceptor de fetch
        }
        var detalle = (motivo && (motivo.message || motivo)) || "Promesa rechazada sin detalle";
        alertarError("Operación interrumpida", String(detalle));
    });

    // Disponible para los módulos
    window.alertarError = alertarError;
})();
