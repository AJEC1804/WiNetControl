/* ==========================================================================
   CONFIGURACIÓN DE LA API  (WiNet Control)
   Archivo: js/config.js  ·  Debe cargarse ANTES que app.js, planes.js y
   facturacion-soporte.js.
   --------------------------------------------------------------------------
   ¿Para qué sirve?
   Antes cada archivo tenía escrito "http://localhost:8080" a mano. Eso obliga
   a que el backend y el navegador estén en el mismo equipo: nadie más podía
   ver los datos.

   Ahora la dirección se calcula sola:
     1. Si la página se abrió desde otro equipo o desde un túnel, se usa el
        MISMO host desde el que se cargó la página, en el puerto 8080.
        Ejemplo: abrir http://192.168.1.10:8000/login.html
                 -> la API será http://192.168.1.10:8080
     2. Se puede forzar otra dirección agregando ?api= en la URL:
        http://localhost:8000/login.html?api=https://mi-servidor.com
     3. Si no hay nada de lo anterior, se usa localhost:8080 (uso normal).
   ========================================================================== */

var API_BASE = (function () {
    var PUERTO = "8080";

    // 1. Dirección forzada por parámetro (?api=...)
    var forzada = null;
    try {
        forzada = new URLSearchParams(window.location.search).get("api");
    } catch (e) {
        forzada = null;
    }

    if (forzada) {
        return forzada.replace(/\/+$/, "");   // sin barra final
    }

    var host = window.location.hostname || "localhost";

    // Si la página se sirve por https, la API también debe ser https
    var protocolo = window.location.protocol === "https:" ? "https:" : "http:";

    // Si la página NO trae puerto (sirve en 80/443), igual apuntamos a 8080
    return protocolo + "//" + host + ":" + PUERTO;
})();

// Ayuda para depurar: queda visible en la consola del navegador
console.log("[WiNet] API en uso:", API_BASE);
