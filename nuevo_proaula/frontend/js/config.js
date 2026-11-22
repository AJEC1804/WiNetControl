var API_BASE = (function () {
    var PUERTO = "8080";

    var forzada = null;
    try {
        forzada = new URLSearchParams(window.location.search).get("api");
    } catch (e) {
        forzada = null;
    }

    if (forzada) {
        return forzada.replace(/\/+$/, "");
    }

    var host = window.location.hostname || "localhost";

    var protocolo = window.location.protocol === "https:" ? "https:" : "http:";

    return protocolo + "//" + host + ":" + PUERTO;
})();

console.log("[WiNet] API en uso:", API_BASE);

(function () {
    if (typeof window === "undefined" || typeof window.fetch !== "function") return;
    var fetchOriginal = window.fetch.bind(window);

    window.obtenerTokenSesion = function () {
        try {
            var sesion = JSON.parse(localStorage.getItem("sesionActiva"));
            return (sesion && sesion.token) ? sesion.token : "";
        } catch (e) {
            return "";
        }
    };

    window.fetch = function (recurso, opciones) {
        var url = typeof recurso === "string" ? recurso : (recurso && recurso.url) || "";
        var esApi = url.indexOf(API_BASE) === 0;

        if (!esApi) {
            return fetchOriginal(recurso, opciones);
        }

        var opts = opciones ? Object.assign({}, opciones) : {};
        var headers = new Headers(opts.headers || {});
        var token = window.obtenerTokenSesion();

        if (token && !headers.has("Authorization")) {
            headers.set("Authorization", "Bearer " + token);
        }
        opts.headers = headers;

        return fetchOriginal(recurso, opts).then(function (respuesta) {
            var esLogin = url.indexOf("/api/login") !== -1;
            var enAuthPage = /\/(login|registro|empresa-suspendida)\.html$/i.test(window.location.pathname);
            if (respuesta.status === 401 && !esLogin && !enAuthPage && token) {
                localStorage.removeItem("sesionActiva");
                window.location.href = "login.html";
            }
            if (respuesta.status === 403 && !esLogin && !enAuthPage) {
                respuesta.clone().json().then(function (data) {
                    if (data && data.empresaSuspendida) {
                        sessionStorage.setItem("empresaSuspendidaInfo", JSON.stringify(data));
                        localStorage.removeItem("sesionActiva");
                        window.location.href = "empresa-suspendida.html";
                    }
                }).catch(function () {});
            }
            return respuesta;
        });
    };
})();
