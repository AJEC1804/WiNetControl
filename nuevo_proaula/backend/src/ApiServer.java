import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import controladores.EmpleadoController;
import controladores.FacturaController;
import controladores.PlanController;
import controladores.SoporteController;
import controladores.UsuarioController;
import entidades.Administrador;
import entidades.Cliente;
import entidades.Empleado;
import entidades.Factura;
import entidades.Soporte;
import entidades.Usuario;
import entidades.Plan;
import utils.PersistenciaUtil;
import utils.Respuesta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PUENTE HTTP ENTRE LA PÁGINA WEB Y LOS MÉTODOS DE JAVA
 * ------------------------------------------------------------------
 * Este servidor NO tiene lógica de negocio: solo recibe peticiones de la
 * página web (fetch) y llama a los métodos que ya existen en
 * UsuarioController. Toda la lógica sigue estando en:
 *
 *   controladores.UsuarioController -> modelos.ClienteModel -> metodos.ListaClientes
 *
 * Usa com.sun.net.httpserver, que viene incluido en el JDK: no hace falta
 * instalar ni descargar ninguna librería.
 *
 * Cómo arrancarlo:
 *   javac -encoding UTF-8 -cp "nuevo_proaula/backend/lib/*" -d bin $(find nuevo_proaula/backend/src -name "*.java")
 *   java -cp "bin;nuevo_proaula/backend/lib/*" ApiServer
 *
 * Rutas que expone:
 *   GET    /api/clientes                 -> usuarioCtrl.listarClientes()
 *   GET    /api/clientes?estado=activos  -> usuarioCtrl.listarClientesPorEstado(true)
 *   GET    /api/clientes?estado=inactivos-> usuarioCtrl.listarClientesPorEstado(false)
 *   GET    /api/clientes/{cedula}        -> usuarioCtrl.buscarCliente(cedula)
 *   POST   /api/clientes                 -> usuarioCtrl.registrarCliente(...)
 *   PUT    /api/clientes/{cedula}        -> usuarioCtrl.actualizarCliente(...)
 *   PUT    /api/clientes/{cedula}/estado -> usuarioCtrl.cambiarEstadoCliente(...)
 *   DELETE /api/clientes/{cedula}        -> usuarioCtrl.eliminarCliente(cedula)
 */
public class ApiServer {

    private static final int PUERTO = 8080;
    private static final String RUTA_BASE = "/api/clientes";
    private static final String RUTA_PLANES = "/api/planes";
    private static final String RUTA_EMPLEADOS = "/api/empleados";
    private static final String RUTA_LOGIN = "/api/login";
    private static final String RUTA_FACTURAS = "/api/facturas";
    private static final String RUTA_SOPORTE = "/api/soporte";
    private static final String ARCHIVO_ATENDIDOS = "atendidos.dat";

    // La persistencia en archivos .dat se activa antes de crear los controladores,
    // porque cada modelo intenta cargar su estado en el constructor.
    static {
        PersistenciaUtil.activar();
    }

    // Los controladores del equipo. Aquí NO hay lógica nueva: solo se les llama.
    private static final UsuarioController usuarioCtrl = new UsuarioController();
    private static final PlanController planCtrl = new PlanController();
    private static final EmpleadoController empleadoCtrl = new EmpleadoController();
    private static final FacturaController facturaCtrl = new FacturaController();
    private static final SoporteController soporteCtrl = new SoporteController();
    private static final List<Soporte> ticketsAtendidos = new ArrayList<>();
    private static final Map<String, SesionToken> sesionesActivas = new ConcurrentHashMap<>();

    private static final class SesionToken {
        final String identificacion;
        final String rol;
        final String nombreCompleto;
        final String correo;

        SesionToken(String identificacion, String rol, String nombreCompleto, String correo) {
            this.identificacion = identificacion != null ? identificacion : "";
            this.rol = rol != null ? rol.toUpperCase() : "CLIENTE";
            this.nombreCompleto = nombreCompleto != null ? nombreCompleto : "";
            this.correo = correo != null ? correo : "";
        }
    }

    public static void main(String[] args) throws IOException {
        // Primero se recupera el historial de atendidos, para que la siembra
        // (si hace falta) no lo sobrescriba con una lista vacía.
        cargarTicketsAtendidos();

        // La siembra solo ocurre en un arranque realmente vacío (primera ejecución).
        // Si se recuperó algo de disco, no se siembra: de lo contrario se duplicarían
        // los clientes y los tickets que sí se habían cargado.
        List<Plan> planesEnDisco = planCtrl.listarPlanes().getDatos();
        List<Cliente> clientesEnDisco = usuarioCtrl.listarClientes().getDatos();
        List<Empleado> empleadosEnDisco = empleadoCtrl.listarEmpleados().getDatos();
        boolean todoVacio = (planesEnDisco == null || planesEnDisco.isEmpty())
                && (clientesEnDisco == null || clientesEnDisco.isEmpty())
                && (empleadosEnDisco == null || empleadosEnDisco.isEmpty());

        if (todoVacio) {
            sembrarClientesDeEjemplo();
            persistirTodo();
        } else {
            System.out.println("[persistencia] Estado recuperado de la carpeta datos/");
        }

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        servidor.createContext(RUTA_BASE, ApiServer::manejarClientes);
        servidor.createContext(RUTA_PLANES, ApiServer::manejarPlanes);
        servidor.createContext(RUTA_EMPLEADOS, ApiServer::manejarEmpleados);
        servidor.createContext(RUTA_LOGIN, ApiServer::manejarLogin);
        servidor.createContext(RUTA_FACTURAS, ApiServer::manejarFacturas);
        servidor.createContext(RUTA_SOPORTE, ApiServer::manejarSoporte);
        // Cualquier otra ruta: 404 en JSON y con CORS. Sin este contexto, el 404 por
        // defecto del JDK no trae cabeceras CORS y el navegador lo reporta como
        // "no se pudo conectar" en lugar de "ruta no encontrada".
        servidor.createContext("/", ApiServer::manejarRutaDesconocida);
        servidor.setExecutor(null);
        servidor.start();

        System.out.println("=========================================================");
        System.out.println("  API WiNet Control escuchando en http://localhost:" + PUERTO);
        System.out.println("  Modulo: Gestion de Clientes (Issue #8 - Gyanela Meza)");
        System.out.println("  Ctrl+C para detener.");
        System.out.println("=========================================================");
    }

    // ==================================================================
    // 0. PERSISTENCIA
    // ==================================================================
    /**
     * Guarda en disco el estado de los cinco modelos. Se invoca al final de cada
     * petición que modifica datos (POST, PUT y DELETE). Los GET no escriben nada.
     */
    private static void persistirTodo() {
        if (!PersistenciaUtil.estaActiva()) {
            return;
        }
        UsuarioController.getSharedClienteModel().guardarEstado();
        UsuarioController.getSharedPlanModel().guardarEstado();
        UsuarioController.getSharedFacturaModel().guardarEstado();
        EmpleadoController.getSharedEmpleadoModel().guardarEstado();
        SoporteController.getSharedSoporteModel().guardarEstado();
        PersistenciaUtil.guardar(new ArrayList<>(ticketsAtendidos), ARCHIVO_ATENDIDOS);
    }

    /**
     * Recupera el historial de tickets ya atendidos (el que consume GET /api/soporte).
     * La cola de pendientes se recupera aparte, desde SoporteModel.
     */
    @SuppressWarnings("unchecked")
    private static void cargarTicketsAtendidos() {
        Object leido = PersistenciaUtil.cargar(ARCHIVO_ATENDIDOS, Object.class);
        if (leido instanceof List) {
            ticketsAtendidos.clear();
            ticketsAtendidos.addAll((List<Soporte>) leido);
            System.out.println("[persistencia] Tickets atendidos recuperados: " + ticketsAtendidos.size());
        }
    }

    /** Responde 404 a cualquier ruta que no exista, en JSON y con CORS. */
    private static void manejarRutaDesconocida(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }
        responderError(ex, 404, "Ruta no encontrada: " + ex.getRequestMethod()
                + " " + ex.getRequestURI().getPath());
    }

    // ==================================================================
    // 1. ENRUTADOR
    // ==================================================================
    private static void manejarClientes(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();

        // El navegador pregunta primero si puede llamar (CORS)
        if (metodo.equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }

        try {
            SesionToken sesion = autenticarPeticion(ex);

            // GET /api/clientes  y  GET /api/clientes?estado=activos
            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_BASE)) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN", "EMPLEADO")) return;
                listarClientes(ex, query);
                return;
            }

            // GET /api/clientes/{cedula}
            if (metodo.equalsIgnoreCase("GET") && ruta.startsWith(RUTA_BASE + "/")) {
                String cedulaBuscada = ultimoSegmento(ruta);
                if (!requierePersonalOPropio(ex, sesion, cedulaBuscada)) return;
                buscarCliente(ex, cedulaBuscada);
                return;
            }

            // POST /api/clientes (público en registro.html, o desde panel por personal)
            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_BASE)) {
                if (sesion != null && "CLIENTE".equals(sesion.rol)) {
                    responderError(ex, 403, "No tienes permisos para registrar clientes.");
                    return;
                }
                registrarCliente(ex, sesion != null);
                return;
            }

            // PUT /api/clientes/{cedula}/plan  ->  asignarPlan()
            if (metodo.equalsIgnoreCase("PUT") && ruta.endsWith("/plan")) {
                String cedulaPlan = segmentoAnterior(ruta);
                if (!requierePersonalOPropio(ex, sesion, cedulaPlan)) return;
                asignarPlan(ex, cedulaPlan);
                return;
            }

            // PUT /api/clientes/{cedula}/estado
            if (metodo.equalsIgnoreCase("PUT") && ruta.endsWith("/estado")) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN")) return;
                String cedula = segmentoAnterior(ruta);
                cambiarEstado(ex, cedula);
                return;
            }

            // PUT /api/clientes/{cedula}
            if (metodo.equalsIgnoreCase("PUT") && ruta.startsWith(RUTA_BASE + "/")) {
                String cedulaEdit = ultimoSegmento(ruta);
                if (!requierePersonalOPropio(ex, sesion, cedulaEdit)) return;
                actualizarCliente(ex, cedulaEdit, sesion);
                return;
            }

            // DELETE /api/clientes/{cedula}
            if (metodo.equalsIgnoreCase("DELETE") && ruta.startsWith(RUTA_BASE + "/")) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN")) return;
                eliminarCliente(ex, ultimoSegmento(ruta));
                return;
            }

            responderError(ex, 404, "Ruta no encontrada: " + metodo + " " + ruta);

        } catch (Exception e) {
            responderError(ex, 500, "Error interno: " + e.getMessage());
        } finally {
            if (!metodo.equalsIgnoreCase("GET") && !metodo.equalsIgnoreCase("OPTIONS")) {
                persistirTodo();
            }
        }
    }
    // ==================================================================
    // 2. CADA MÉTODO LLAMA AL CONTROLADOR DEL EQUIPO
    // ==================================================================

    /** GET /api/clientes  ->  listarClientes() o listarClientesPorEstado() */
    private static void listarClientes(HttpExchange ex, String query) throws IOException {
        String estado = valorDeQuery(query, "estado");

        Respuesta<List<Cliente>> respuesta;

        if ("activos".equalsIgnoreCase(estado)) {
            respuesta = usuarioCtrl.listarClientesPorEstado(true);
        } else if ("inactivos".equalsIgnoreCase(estado)) {
            respuesta = usuarioCtrl.listarClientesPorEstado(false);
        } else {
            respuesta = usuarioCtrl.listarClientes();
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"exito\":").append(respuesta.isExito());
        json.append(",\"mensaje\":\"").append(escapar(respuesta.getMensaje())).append("\"");
        json.append(",\"datos\":[");
        List<Cliente> lista = respuesta.getDatos();
        if (lista != null) {
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) json.append(",");
                json.append(jsonDeCliente(lista.get(i)));
            }
        }
        json.append("]}");

        responder(ex, 200, json.toString());
    }

    /** GET /api/clientes/{cedula}  ->  buscarCliente(cedula) */
    private static void buscarCliente(HttpExchange ex, String cedula) throws IOException {
        Respuesta<Cliente> respuesta = usuarioCtrl.buscarCliente(cedula);

        if (!respuesta.isExito()) {
            responderError(ex, 404, respuesta.getMensaje());
            return;
        }

        responder(ex, 200, jsonDeRespuestaCliente(respuesta));
    }

    /** PUT /api/clientes/{cedula}  ->  actualizarCliente(...) */
    private static void actualizarCliente(HttpExchange ex, String cedula, SesionToken sesion) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));

        Respuesta<Cliente> respuesta = usuarioCtrl.actualizarCliente(
                cedula,
                datos.getOrDefault("nombre", ""),
                datos.getOrDefault("apellido", ""),
                datos.getOrDefault("tipo", "CC"),
                datos.getOrDefault("telefono", ""),
                datos.getOrDefault("correo", ""),
                datos.getOrDefault("direccion", "Sin dirección"),
                datos.getOrDefault("contrasena", ""),
                datos.get("planActual")
        );

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        // Solo ADMIN / SUPERADMIN pueden cambiar el estado activo/inactivo
        String activo = datos.get("activo");
        if (activo != null && sesion != null && ("SUPERADMIN".equals(sesion.rol) || "ADMIN".equals(sesion.rol))) {
            usuarioCtrl.cambiarEstadoCliente(cedula, Boolean.parseBoolean(activo));
        }

        responder(ex, 200, jsonDeRespuestaCliente(usuarioCtrl.buscarCliente(cedula)));
    }

    /** POST /api/clientes  ->  registrarCliente(...) */
    private static void registrarCliente(HttpExchange ex, boolean esPersonalAutenticado) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));

        String nombre = datos.getOrDefault("nombre", "");
        String apellido = datos.getOrDefault("apellido", "");
        String tipo = datos.getOrDefault("tipo", "CC");
        String cedula = datos.getOrDefault("identificacion", "");
        String telefono = datos.getOrDefault("telefono", "");
        String correo = datos.getOrDefault("correo", "");
        String direccion = datos.getOrDefault("direccion", "Sin dirección");
        String contrasena = datos.getOrDefault("contrasena", "Clave123");

        Respuesta<Cliente> respuesta = usuarioCtrl.registrarCliente(
                nombre, apellido, tipo, cedula, telefono, correo,
                direccion, contrasena, contrasena
        );

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        // Plan y estado elegidos en el formulario (solo si los envía personal autenticado)
        if (esPersonalAutenticado) {
            String plan = datos.get("planActual");
            if (plan != null && !plan.isEmpty()) {
                Respuesta<Cliente> respPlan = usuarioCtrl.asignarPlan(cedula, plan);
                if (!respPlan.isExito()) {
                    usuarioCtrl.eliminarCliente(cedula);
                    responderError(ex, 400, respPlan.getMensaje());
                    return;
                }
            }
            String activo = datos.get("activo");
            if (activo != null) {
                usuarioCtrl.cambiarEstadoCliente(cedula, Boolean.parseBoolean(activo));
            }
        }

        responder(ex, 201, jsonDeRespuestaCliente(usuarioCtrl.buscarCliente(cedula)));
    }

    /** PUT /api/clientes/{cedula}/plan  ->  asignarPlan(cedula, nombrePlan) */
    private static void asignarPlan(HttpExchange ex, String cedula) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));
        String nombrePlan = datos.getOrDefault("plan", "");

        Respuesta<Cliente> respuesta = usuarioCtrl.asignarPlan(cedula, nombrePlan);

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        responder(ex, 200, jsonDeRespuestaCliente(respuesta));
    }

    /** PUT /api/clientes/{cedula}/estado  ->  cambiarEstadoCliente(cedula, activo) */
    private static void cambiarEstado(HttpExchange ex, String cedula) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));
        boolean activo = Boolean.parseBoolean(datos.getOrDefault("activo", "false"));

        Respuesta<Cliente> respuesta = usuarioCtrl.cambiarEstadoCliente(cedula, activo);

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        responder(ex, 200, jsonDeRespuestaCliente(respuesta));
    }

    /** DELETE /api/clientes/{cedula}  ->  eliminarCliente(cedula) */
    private static void eliminarCliente(HttpExchange ex, String cedula) throws IOException {
        Respuesta<Boolean> respuesta = usuarioCtrl.eliminarCliente(cedula);

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(respuesta.getMensaje()) + "\"}");
    }

    // ==================================================================
    // 2b. PLANES  ->  PlanController (Issue #9 - Yeini Álvarez)
    // ==================================================================
    private static void manejarPlanes(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();

        if (metodo.equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }

        try {
            SesionToken sesion = autenticarPeticion(ex);
            if (sesion == null) {
                responderError(ex, 401, "Sesión expirada o no iniciada. Inicia sesión nuevamente.");
                return;
            }

            // GET /api/planes/{codigo}      ->  consultarPlanPorId(codigo), o 404
            if (metodo.equalsIgnoreCase("GET") && ruta.startsWith(RUTA_PLANES + "/")) {
                String texto = ultimoSegmento(ruta);
                int codigo = 0;
                try {
                    codigo = Integer.parseInt(texto.trim());
                } catch (NumberFormatException e) {
                    responderError(ex, 400, "El código del plan debe ser numérico: " + texto);
                    return;
                }
                Respuesta<Plan> uno = planCtrl.buscarPlanPorCodigo(String.valueOf(codigo));
                if (!uno.isExito() || uno.getDatos() == null) {
                    responderError(ex, 404, "No existe un plan con código " + codigo);
                    return;
                }
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"Plan encontrado.\",\"datos\":"
                        + jsonDePlan(uno.getDatos()) + "}");
                return;
            }

            // GET /api/planes              ->  listarPlanes()
            // GET /api/planes?estado=activos ->  listarPlanesActivos()
            if (metodo.equalsIgnoreCase("GET")) {
                String estadoPlan = valorDeQuery(ex.getRequestURI().getQuery(), "estado");

                Respuesta<List<Plan>> respuesta = "activos".equalsIgnoreCase(estadoPlan)
                        ? planCtrl.listarPlanesActivos()
                        : planCtrl.listarPlanes();
                StringBuilder json = new StringBuilder();
                json.append("{\"exito\":").append(respuesta.isExito());
                json.append(",\"mensaje\":\"").append(escapar(respuesta.getMensaje())).append("\"");
                json.append(",\"datos\":[");
                List<Plan> lista = respuesta.getDatos();
                if (lista != null) {
                    for (int i = 0; i < lista.size(); i++) {
                        if (i > 0) json.append(",");
                        json.append(jsonDePlan(lista.get(i)));
                    }
                }
                json.append("]}");
                responder(ex, 200, json.toString());
                return;
            }

            // PUT /api/planes/lote  ->  crearPlan / actualizarPlan / cambiarEstadoPlan / eliminarPlan
            if (metodo.equalsIgnoreCase("PUT")) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN", "EMPLEADO")) return;
                sincronizarPlanes(ex);
                return;
            }

            responderError(ex, 404, "Ruta no encontrada: " + metodo + " " + ruta);

        } catch (Exception e) {
            responderError(ex, 500, "Error interno: " + e.getMessage());
        } finally {
            if (!metodo.equalsIgnoreCase("GET") && !metodo.equalsIgnoreCase("OPTIONS")) {
                persistirTodo();
            }
        }
    }
    /**
     * Recibe el catálogo completo desde la página y lo aplica llamando a los
     * métodos del PlanController: no se inventa nada, solo se decide qué
     * método usar para cada plan.
     */
    private static void sincronizarPlanes(HttpExchange ex) throws IOException {
        List<Map<String, String>> entrantes = leerArrayDeObjetos(leerCuerpo(ex), "planes");

        List<Plan> actuales = planCtrl.listarPlanes().getDatos();
        int creados = 0, actualizados = 0, eliminados = 0;

        // 1. Crear o actualizar los que llegan
        for (Map<String, String> datos : entrantes) {
            String codigo = datos.getOrDefault("codigo", "");
            String nombre = datos.getOrDefault("nombre", "");
            String velocidad = datos.getOrDefault("velocidad", "");
            String precio = datos.getOrDefault("precio", "0");
            String descripcion = datos.getOrDefault("descripcion", "");
            boolean activo = Boolean.parseBoolean(datos.getOrDefault("activo", "true"));

            boolean existe = false;
            if (actuales != null) {
                for (Plan p : actuales) {
                    if (String.valueOf(p.getIdPlan()).equals(codigo)) {
                        existe = true;
                        break;
                    }
                }
            }

            if (existe) {
                Respuesta<Plan> r = planCtrl.actualizarPlan(codigo, codigo, nombre, velocidad, precio, descripcion);
                if (!r.isExito()) {
                    responderError(ex, r.getCodigo(), r.getMensaje());
                    return;
                }
                actualizados++;
            } else {
                Respuesta<Plan> r = planCtrl.crearPlan(codigo, nombre, velocidad, precio, descripcion);
                if (!r.isExito()) {
                    responderError(ex, r.getCodigo(), r.getMensaje());
                    return;
                }
                creados++;
            }

            Respuesta<Plan> rEstado = planCtrl.cambiarEstadoPlan(codigo, activo);
            if (!rEstado.isExito()) {
                responderError(ex, rEstado.getCodigo(), rEstado.getMensaje());
                return;
            }
        }

        // 2. Eliminar los que ya no vienen (el modelo reasigna a los clientes)
        if (actuales != null) {
            for (Plan p : actuales) {
                String codigoActual = String.valueOf(p.getIdPlan());
                boolean sigue = false;
                for (Map<String, String> datos : entrantes) {
                    if (codigoActual.equals(datos.get("codigo"))) {
                        sigue = true;
                        break;
                    }
                }
                if (!sigue) {
                    Respuesta<Boolean> rElim = planCtrl.eliminarPlan(codigoActual);
                    if (!rElim.isExito()) {
                        responderError(ex, rElim.getCodigo(), rElim.getMensaje());
                        return;
                    }
                    eliminados++;
                }
            }
        }

        responder(ex, 200, "{\"exito\":true,\"mensaje\":\"Catálogo sincronizado.\","
                + "\"datos\":{\"creados\":" + creados + ",\"actualizados\":" + actualizados
                + ",\"eliminados\":" + eliminados + "}}");
    }

    private static String jsonDePlan(Plan p) {
        if (p == null) {
            return "null";
        }
        return "{"
                + "\"codigo\":" + p.getIdPlan() + ","
                + "\"nombre\":\"" + escapar(p.getNombrePlan()) + "\","
                + "\"velocidad\":\"" + escapar(p.getVelocidadPlan()) + "\","
                + "\"precio\":" + (long) p.getPrecio() + ","
                + "\"descripcion\":\"" + escapar(p.getDescripcionPlan()) + "\","
                + "\"activo\":" + p.isActivo() + ","
                + "\"estado\":\"" + escapar(p.getEstado()) + "\""
                + "}";
    }

    // ==================================================================
    // 2c. EMPLEADOS  ->  EmpleadoController (Issue #13)
    // ==================================================================
    private static void manejarEmpleados(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();

        if (metodo.equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }

        try {
            SesionToken sesion = autenticarPeticion(ex);
            if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN")) return;

            // GET /api/empleados  (con ?estado=activos|inactivos  o  ?buscar=texto)
            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_EMPLEADOS)) {
                listarEmpleados(ex, query);
                return;
            }

            // GET /api/empleados/{cedula}
            if (metodo.equalsIgnoreCase("GET") && ruta.startsWith(RUTA_EMPLEADOS + "/")) {
                Respuesta<Empleado> respuesta = empleadoCtrl.buscarEmpleado(ultimoSegmento(ruta));
                if (!respuesta.isExito()) {
                    responderError(ex, 404, respuesta.getMensaje());
                    return;
                }
                responder(ex, 200, jsonDeRespuestaEmpleado(respuesta));
                return;
            }

            // POST /api/empleados
            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_EMPLEADOS)) {
                registrarEmpleado(ex);
                return;
            }

            // DELETE /api/empleados/{cedula}
            if (metodo.equalsIgnoreCase("DELETE") && ruta.startsWith(RUTA_EMPLEADOS + "/")) {
                Respuesta<Boolean> respuesta = empleadoCtrl.eliminarEmpleado(ultimoSegmento(ruta));
                if (!respuesta.isExito()) {
                    responderError(ex, 400, respuesta.getMensaje());
                    return;
                }
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\""
                        + escapar(respuesta.getMensaje()) + "\"}");
                return;
            }

            // PUT /api/empleados/{cedula}/estado
            if (metodo.equalsIgnoreCase("PUT") && ruta.endsWith("/estado")) {
                cambiarEstadoEmpleado(ex, segmentoAnterior(ruta));
                return;
            }

            // PUT /api/empleados/{cedula}
            if (metodo.equalsIgnoreCase("PUT") && ruta.startsWith(RUTA_EMPLEADOS + "/")) {
                actualizarEmpleado(ex, ultimoSegmento(ruta));
                return;
            }

            responderError(ex, 404, "Ruta no encontrada: " + metodo + " " + ruta);

        } catch (Exception e) {
            responderError(ex, 500, "Error interno: " + e.getMessage());
        } finally {
            if (!metodo.equalsIgnoreCase("GET") && !metodo.equalsIgnoreCase("OPTIONS")) {
                persistirTodo();
            }
        }
    }
    private static void listarEmpleados(HttpExchange ex, String query) throws IOException {
        String estado = valorDeQuery(query, "estado");
        String buscar = valorDeQuery(query, "buscar");

        StringBuilder json = new StringBuilder();

        // --- Búsqueda parcial: el controlador devuelve una LISTA ENLAZADA ---
        if (buscar != null && !buscar.isEmpty()) {
            Respuesta<metodos.ListaEmpleados> respuesta = empleadoCtrl.buscarEmpleados(buscar);

            json.append("{\"exito\":").append(respuesta.isExito());
            json.append(",\"mensaje\":\"").append(escapar(respuesta.getMensaje())).append("\"");
            json.append(",\"datos\":[");

            // Se recorre la lista enlazada nodo por nodo
            metodos.ListaEmpleados.NodoEmpleado sapo = respuesta.getDatos().getPrimero();
            boolean primeroItem = true;
            while (sapo != null) {
                if (!primeroItem) json.append(",");
                json.append(jsonDeEmpleado(sapo.getDato()));
                primeroItem = false;
                sapo = sapo.getSiguiente();
            }

            json.append("]}");
            responder(ex, 200, json.toString());
            return;
        }

        // --- Listado normal ---
        Respuesta<List<Empleado>> respuesta;

        if ("activos".equalsIgnoreCase(estado)) {
            respuesta = empleadoCtrl.listarEmpleadosPorEstado(true);
        } else if ("inactivos".equalsIgnoreCase(estado)) {
            respuesta = empleadoCtrl.listarEmpleadosPorEstado(false);
        } else {
            respuesta = empleadoCtrl.listarEmpleados();
        }

        json.append("{\"exito\":").append(respuesta.isExito());
        json.append(",\"mensaje\":\"").append(escapar(respuesta.getMensaje())).append("\"");
        json.append(",\"datos\":[");
        List<Empleado> lista = respuesta.getDatos();
        if (lista != null) {
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) json.append(",");
                json.append(jsonDeEmpleado(lista.get(i)));
            }
        }
        json.append("]}");

        responder(ex, 200, json.toString());
    }

    private static void registrarEmpleado(HttpExchange ex) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));

        Respuesta<Empleado> respuesta = empleadoCtrl.registrarEmpleado(
                datos.getOrDefault("nombre", ""),
                datos.getOrDefault("apellido", ""),
                datos.getOrDefault("tipo", "CC"),
                datos.getOrDefault("identificacion", ""),
                datos.getOrDefault("telefono", ""),
                datos.getOrDefault("correo", ""),
                datos.getOrDefault("direccion", "Sin dirección"),
                datos.getOrDefault("cargo", "Atención al cliente"),
                datos.getOrDefault("sucursal", "Sucursal Local Norte"),
                datos.get("contrasena")
        );

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        String activo = datos.get("activo");
        if (activo != null) {
            empleadoCtrl.cambiarEstadoEmpleado(
                    respuesta.getDatos().getIdentificacion(), Boolean.parseBoolean(activo));
        }

        String rol = datos.get("rol");
        if (rol != null && !rol.isEmpty()) {
            empleadoCtrl.cambiarRolEmpleado(respuesta.getDatos().getIdentificacion(), rol);
        }

        responder(ex, 201, jsonDeRespuestaEmpleado(
                empleadoCtrl.buscarEmpleado(respuesta.getDatos().getIdentificacion())));
    }

    private static void actualizarEmpleado(HttpExchange ex, String cedula) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));

        Respuesta<Empleado> respuesta = empleadoCtrl.actualizarEmpleado(
                cedula,
                datos.getOrDefault("nombre", ""),
                datos.getOrDefault("apellido", ""),
                datos.getOrDefault("tipo", "CC"),
                datos.getOrDefault("telefono", ""),
                datos.getOrDefault("correo", ""),
                datos.getOrDefault("direccion", "Sin dirección"),
                datos.getOrDefault("cargo", ""),
                datos.getOrDefault("sucursal", "")
        );

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        String activo = datos.get("activo");
        if (activo != null) {
            empleadoCtrl.cambiarEstadoEmpleado(cedula, Boolean.parseBoolean(activo));
        }

        String rol = datos.get("rol");
        if (rol != null && !rol.isEmpty()) {
            empleadoCtrl.cambiarRolEmpleado(cedula, rol);
        }

        responder(ex, 200, jsonDeRespuestaEmpleado(empleadoCtrl.buscarEmpleado(cedula)));
    }

    private static void cambiarEstadoEmpleado(HttpExchange ex, String cedula) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));
        boolean activo = Boolean.parseBoolean(datos.getOrDefault("activo", "false"));

        Respuesta<Empleado> respuesta = empleadoCtrl.cambiarEstadoEmpleado(cedula, activo);

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        responder(ex, 200, jsonDeRespuestaEmpleado(respuesta));
    }

    private static String jsonDeRespuestaEmpleado(Respuesta<Empleado> respuesta) {
        return "{\"exito\":" + respuesta.isExito()
                + ",\"mensaje\":\"" + escapar(respuesta.getMensaje()) + "\""
                + ",\"datos\":" + jsonDeEmpleado(respuesta.getDatos()) + "}";
    }

    private static String jsonDeEmpleado(Empleado e) {
        if (e == null) {
            return "null";
        }
        return "{"
                + "\"identificacion\":\"" + escapar(e.getIdentificacion()) + "\","
                + "\"nombre\":\"" + escapar(e.getNombre()) + "\","
                + "\"apellido\":\"" + escapar(e.getApellido()) + "\","
                + "\"tipo\":\"" + escapar(e.getTipo()) + "\","
                + "\"telefono\":\"" + escapar(e.getTelefono()) + "\","
                + "\"correo\":\"" + escapar(e.getCorreo()) + "\","
                + "\"direccion\":\"" + escapar(e.getDireccion()) + "\","
                + "\"cargo\":\"" + escapar(e.getCargo()) + "\","
                + "\"sucursal\":\"" + escapar(e.getSucursal()) + "\","
                + "\"fechaIngreso\":\"" + escapar(e.getFechaIngreso()) + "\","
                + "\"rol\":\"" + escapar(e.getRol()) + "\","
                + "\"activo\":" + e.isActivo()
                + "}";
    }

    // ==================================================================
    // 2d. LOGIN  ->  un solo punto de autenticación para los 4 roles
    // ==================================================================
    private static void manejarLogin(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();

        if (metodo.equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }

        if (!metodo.equalsIgnoreCase("POST")) {
            responderError(ex, 405, "Método no permitido. Usa POST.");
            return;
        }

        try {
            Map<String, String> datos = leerJson(leerCuerpo(ex));

            // La página manda "usuario" y "contrasena"
            String identificador = datos.getOrDefault("usuario", datos.getOrDefault("identificador", ""));
            String contrasena = datos.getOrDefault("contrasena", "");

            // 1. Administradores (superadmin / admin) y clientes
            Respuesta<Usuario> respuesta = usuarioCtrl.autenticarUsuario(identificador, contrasena);

            if (respuesta.isExito()) {
                String token = emitirToken(respuesta.getDatos());
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(respuesta.getMensaje())
                        + "\",\"datos\":" + jsonDeSesion(respuesta.getDatos(), token) + "}");
                return;
            }

            // 2. Empleados
            Respuesta<Empleado> respuestaEmpleado = empleadoCtrl.autenticar(identificador, contrasena);

            if (respuestaEmpleado.isExito()) {
                String token = emitirToken(respuestaEmpleado.getDatos());
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(respuestaEmpleado.getMensaje())
                        + "\",\"datos\":" + jsonDeSesion(respuestaEmpleado.getDatos(), token) + "}");
                return;
            }

            // Ninguno de los dos: se devuelve el mensaje del intento de empleado
            // (que es "Usuario o contraseña incorrectos" si no existe en ninguno)
            Respuesta<Usuario> errorAdmin = respuesta;
            String mensajeFinal = errorAdmin.getMensaje();
            int codigoFinal = errorAdmin.getCodigo();

            // Si el intento como cliente fue 401 (no existe), se informa el del empleado
            if (codigoFinal == 401 && respuestaEmpleado.getCodigo() != 401) {
                mensajeFinal = respuestaEmpleado.getMensaje();
                codigoFinal = respuestaEmpleado.getCodigo();
            }

            responderError(ex, codigoFinal, mensajeFinal);

        } catch (Exception e) {
            responderError(ex, 500, "Error interno: " + e.getMessage());
        }
    }
    /** Devuelve los datos que la página guarda como "sesión". */
    private static String jsonDeSesion(Usuario u, String token) {
        if (u == null) {
            return "null";
        }

        String planActual = "";
        String cargo = "";
        String sucursal = "";

        if (u instanceof Cliente) {
            planActual = ((Cliente) u).getPlanActual();
        }
        if (u instanceof Empleado) {
            cargo = ((Empleado) u).getCargo();
            sucursal = ((Empleado) u).getSucursal();
        }
        if (u instanceof Administrador) {
            sucursal = ((Administrador) u).getSucursal();
        }

        return "{"
                + "\"identificacion\":\"" + escapar(u.getIdentificacion()) + "\","
                + "\"nombre\":\"" + escapar(u.getNombre()) + "\","
                + "\"apellido\":\"" + escapar(u.getApellido()) + "\","
                + "\"correo\":\"" + escapar(u.getCorreo()) + "\","
                + "\"telefono\":\"" + escapar(u.getTelefono()) + "\","
                + "\"direccion\":\"" + escapar(u.getDireccion()) + "\","
                + "\"rol\":\"" + escapar(u.getRol()) + "\","
                + "\"planActual\":\"" + escapar(planActual.isEmpty() ? cargo : planActual) + "\","
                + "\"sucursal\":\"" + escapar(sucursal) + "\","
                + "\"token\":\"" + escapar(token) + "\","
                + "\"activo\":" + u.isActivo()
                + "}";
    }

    // ==================================================================
    // 2E. FACTURACIÓN (PilaFacturas - Issue #10)
    // ==================================================================
    private static void manejarFacturas(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();

        if (metodo.equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }

        try {
            SesionToken sesion = autenticarPeticion(ex);
            if (sesion == null) {
                responderError(ex, 401, "Sesión expirada o no iniciada. Inicia sesión nuevamente.");
                return;
            }

            // GET /api/facturas/pdf?id=1  -> devuelve el archivo PDF real en disco
            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_FACTURAS + "/pdf")) {
                servirPdfFactura(ex, valorDeQuery(query, "id"), sesion);
                return;
            }

            // GET /api/facturas  o  GET /api/facturas?usuario=1045678901
            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_FACTURAS)) {
                String usuario = valorDeQuery(query, "usuario");
                Respuesta<List<Factura>> resp;
                if ("CLIENTE".equals(sesion.rol)) {
                    resp = facturaCtrl.obtenerFacturasUsuario(sesion.identificacion);
                } else if (usuario != null && !usuario.trim().isEmpty()) {
                    resp = facturaCtrl.obtenerFacturasUsuario(usuario.trim());
                } else {
                    resp = facturaCtrl.listarTodasFacturas();
                }
                List<Factura> lista = resp.getDatos() != null ? resp.getDatos() : new ArrayList<>();
                Factura cima;
                if ("CLIENTE".equals(sesion.rol)) {
                    cima = lista.isEmpty() ? null : lista.get(0);
                } else {
                    Respuesta<Factura> respCima = facturaCtrl.consultarCima();
                    cima = respCima.isExito() ? respCima.getDatos() : null;
                }

                StringBuilder json = new StringBuilder();
                json.append("{\"exito\":true,\"mensaje\":\"").append(escapar(resp.getMensaje())).append("\",");
                json.append("\"cima\":").append(jsonDeFactura(cima)).append(",");
                json.append("\"datos\":[");
                for (int i = 0; i < lista.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(jsonDeFactura(lista.get(i)));
                }
                json.append("]}");
                responder(ex, 200, json.toString());
                return;
            }

            // POST /api/facturas  -> emitir factura a un cliente (solo su plan contratado, para mes/año)
            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_FACTURAS)) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN", "EMPLEADO")) return;
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                String cedula = datos.getOrDefault("identificacion", "");
                int mes = utils.FormateadorFecha.getMesActual();
                int ano = utils.FormateadorFecha.getAnoActual();
                try {
                    if (datos.containsKey("mes")) mes = Integer.parseInt(datos.get("mes").trim());
                    if (datos.containsKey("ano")) ano = Integer.parseInt(datos.get("ano").trim());
                } catch (NumberFormatException ignored) {
                }
                Respuesta<Factura> resp = facturaCtrl.generarFactura(cedula, mes, ano);
                int codigo = resp.isExito() ? 201 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeFactura(resp.getDatos()) + "}");
                return;
            }

            // PUT /api/facturas/pagar -> marcar una factura como PAGADA en la pila
            if (metodo.equalsIgnoreCase("PUT") && ruta.equals(RUTA_FACTURAS + "/pagar")) {
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                int idBuscado = 0;
                try {
                    idBuscado = Integer.parseInt(datos.getOrDefault("idFactura", "0").trim());
                } catch (NumberFormatException ignored) {
                }
                if ("CLIENTE".equals(sesion.rol) && !facturaPerteneceACliente(idBuscado, sesion.identificacion)) {
                    responderError(ex, 403, "No tienes permisos para pagar una factura de otro cliente.");
                    return;
                }
                Respuesta<Factura> resp = facturaCtrl.pagarFactura(idBuscado);
                int codigo = resp.isExito() ? 200 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeFactura(resp.getDatos()) + "}");
                return;
            }

            // DELETE /api/facturas/cima  -> desapilar última factura (Pop LIFO)
            if (metodo.equalsIgnoreCase("DELETE") && ruta.equals(RUTA_FACTURAS + "/cima")) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN")) return;
                Respuesta<Factura> resp = facturaCtrl.desapilarUltimaFactura();
                int codigo = resp.isExito() ? 200 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeFactura(resp.getDatos()) + "}");
                return;
            }

            responderError(ex, 404, "Ruta de facturas no encontrada: " + metodo + " " + ruta);
        } catch (Exception e) {
            e.printStackTrace();
            responderError(ex, 500, "Error interno en facturas: " + e.getMessage());
        } finally {
            if (!metodo.equalsIgnoreCase("GET") && !metodo.equalsIgnoreCase("OPTIONS")) {
                persistirTodo();
            }
        }
    }
    private static void servirPdfFactura(HttpExchange ex, String idTexto, SesionToken sesion) throws IOException {
        int id = 0;
        try {
            id = Integer.parseInt(idTexto != null ? idTexto.trim() : "0");
        } catch (NumberFormatException ignored) {
        }

        List<Factura> todas = facturaCtrl.listarTodasFacturas().getDatos();
        Factura encontrada = null;
        if (todas != null) {
            for (Factura f : todas) {
                if (f != null && f.getIdFactura() == id) {
                    encontrada = f;
                    break;
                }
            }
        }

        if (encontrada == null || encontrada.getArchivoPDF() == null || encontrada.getArchivoPDF().isEmpty()) {
            responderError(ex, 404, "No se encontró el PDF de la factura #" + id);
            return;
        }

        if ("CLIENTE".equals(sesion.rol)
                && !sesion.identificacion.equalsIgnoreCase(encontrada.getIdentificacionCliente())) {
            responderError(ex, 403, "No tienes permisos para ver el comprobante de otro cliente.");
            return;
        }

        File archivo = new File(encontrada.getArchivoPDF());
        if (!archivo.exists()) {
            responderError(ex, 404, "El archivo PDF no existe en disco: " + encontrada.getArchivoPDF());
            return;
        }

        byte[] bytes = Files.readAllBytes(archivo.toPath());
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Content-Type", "application/pdf");
        ex.getResponseHeaders().add("Content-Disposition", "inline; filename=\"" + archivo.getName() + "\"");
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream salida = ex.getResponseBody()) {
            salida.write(bytes);
        }
        ex.close();
    }

    private static String jsonDeFactura(Factura f) {
        if (f == null) {
            return "null";
        }
        return "{"
                + "\"idFactura\":" + f.getIdFactura() + ","
                + "\"identificacionCliente\":\"" + escapar(f.getIdentificacionCliente()) + "\","
                + "\"nombreCliente\":\"" + escapar(f.getUsuario()) + "\","
                + "\"idPlan\":" + f.getIdPlan() + ","
                + "\"nombrePlan\":\"" + escapar(f.getNombrePlan()) + "\","
                + "\"fecha\":\"" + escapar(f.getFecha()) + "\","
                + "\"monto\":" + Math.round(f.getTotal()) + ","
                + "\"archivoPdf\":\"" + escapar(f.getArchivoPDF()) + "\","
                + "\"estado\":\"" + escapar(f.getEstado()) + "\""
                + "}";
    }

    // ==================================================================
    // 2F. SOPORTE TÉCNICO (ColaSoporte - Issue #10)
    // ==================================================================
    private static void manejarSoporte(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();

        if (metodo.equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }

        try {
            SesionToken sesion = autenticarPeticion(ex);
            if (sesion == null) {
                responderError(ex, 401, "Sesión expirada o no iniciada. Inicia sesión nuevamente.");
                return;
            }

            // GET /api/soporte -> devuelve cola de pendientes, frente y atendidos
            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_SOPORTE)) {
                List<Soporte> pendientes = soporteCtrl.listarTicketsPendientes().getDatos();
                if (pendientes == null) pendientes = new ArrayList<>();
                List<Soporte> atendidos = ticketsAtendidos;
                if ("CLIENTE".equals(sesion.rol)) {
                    pendientes = filtrarTicketsCliente(pendientes, sesion);
                    atendidos = filtrarTicketsCliente(ticketsAtendidos, sesion);
                }
                Soporte frente = pendientes.isEmpty() ? null : pendientes.get(0);
                if (!"CLIENTE".equals(sesion.rol)) {
                    Respuesta<Soporte> respFrente = soporteCtrl.consultarSiguienteTicket();
                    frente = respFrente.isExito() ? respFrente.getDatos() : null;
                }

                StringBuilder json = new StringBuilder();
                json.append("{\"exito\":true,");
                json.append("\"frente\":").append(jsonDeSoporte(frente)).append(",");
                json.append("\"pendientes\":[");
                for (int i = 0; i < pendientes.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(jsonDeSoporte(pendientes.get(i)));
                }
                json.append("],\"atendidos\":[");
                for (int i = 0; i < atendidos.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(jsonDeSoporte(atendidos.get(i)));
                }
                json.append("]}");
                responder(ex, 200, json.toString());
                return;
            }

            // POST /api/soporte -> radicar nuevo ticket (encolar al final)
            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_SOPORTE)) {
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                String usuario = "CLIENTE".equals(sesion.rol)
                        ? sesion.identificacion
                        : datos.getOrDefault("usuario", "");
                String correo = datos.getOrDefault("correo", "");
                String categoria = datos.getOrDefault("categoria", "");
                String asunto = datos.getOrDefault("asunto", "");
                String mensaje = datos.getOrDefault("mensaje", "");

                String asuntoFinal = (!categoria.isEmpty() && !asunto.startsWith("["))
                        ? "[" + categoria + "] " + asunto
                        : asunto;

                Respuesta<Soporte> resp = soporteCtrl.radicarTicket(usuario, correo, asuntoFinal, mensaje, false);
                int codigo = resp.isExito() ? 201 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeSoporte(resp.getDatos()) + "}");
                return;
            }

            // POST /api/soporte/atender -> atender y desencolar el frente (FIFO)
            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_SOPORTE + "/atender")) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN", "EMPLEADO")) return;
                Respuesta<Soporte> resp = soporteCtrl.atenderSiguienteTicket();
                if (resp.isExito() && resp.getDatos() != null) {
                    ticketsAtendidos.add(0, resp.getDatos());
                }
                int codigo = resp.isExito() ? 200 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeSoporte(resp.getDatos()) + "}");
                return;
            }

            responderError(ex, 404, "Ruta de soporte no encontrada: " + metodo + " " + ruta);
        } catch (Exception e) {
            e.printStackTrace();
            responderError(ex, 500, "Error interno en soporte: " + e.getMessage());
        } finally {
            if (!metodo.equalsIgnoreCase("GET") && !metodo.equalsIgnoreCase("OPTIONS")) {
                persistirTodo();
            }
        }
    }
    private static String jsonDeSoporte(Soporte s) {
        if (s == null) {
            return "null";
        }
        return "{"
                + "\"idTicket\":" + s.getIdTicket() + ","
                + "\"usuario\":\"" + escapar(s.getUsuario()) + "\","
                + "\"correo\":\"" + escapar(s.getCorreo()) + "\","
                + "\"asunto\":\"" + escapar(s.getAsunto()) + "\","
                + "\"mensaje\":\"" + escapar(s.getMensaje()) + "\","
                + "\"fecha\":\"" + escapar(s.getFecha()) + "\","
                + "\"estado\":\"" + escapar(s.getEstado()) + "\""
                + "}";
    }

    // ==================================================================
    // 3. CONVERSIÓN A JSON
    // ==================================================================
    private static String jsonDeRespuestaCliente(Respuesta<Cliente> respuesta) {
        return "{\"exito\":" + respuesta.isExito()
                + ",\"mensaje\":\"" + escapar(respuesta.getMensaje()) + "\""
                + ",\"datos\":" + jsonDeCliente(respuesta.getDatos()) + "}";
    }

    private static String jsonDeCliente(Cliente c) {
        if (c == null) {
            return "null";
        }
        return "{"
                + "\"identificacion\":\"" + escapar(c.getIdentificacion()) + "\","
                + "\"nombre\":\"" + escapar(c.getNombre()) + "\","
                + "\"apellido\":\"" + escapar(c.getApellido()) + "\","
                + "\"tipo\":\"" + escapar(c.getTipo()) + "\","
                + "\"telefono\":\"" + escapar(c.getTelefono()) + "\","
                + "\"correo\":\"" + escapar(c.getCorreo()) + "\","
                + "\"direccion\":\"" + escapar(c.getDireccion()) + "\","
                + "\"planActual\":\"" + escapar(c.getPlanActual()) + "\","
                + "\"fechaRegistro\":\"" + escapar(c.getFechaRegistro()) + "\","
                + "\"fechaInicioPlan\":\"" + escapar("Sin plan".equalsIgnoreCase(c.getPlanActual()) ? "" : c.getFechaEfectivaPlan()) + "\","
                + "\"estado\":\"" + escapar(c.getEstado()) + "\","
                + "\"activo\":" + c.isActivo()
                + "}";
    }

    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    // ==================================================================
    // 4. UTILIDADES DE HTTP
    // ==================================================================
    private static void responder(HttpExchange ex, int codigo, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);

        // CORS: permite que la página del Live Server (otro puerto) llame aquí
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        ex.sendResponseHeaders(codigo, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream salida = ex.getResponseBody()) {
                salida.write(bytes);
            }
        }
        ex.close();
    }

    private static void responderError(HttpExchange ex, int codigo, String mensaje) throws IOException {
        responder(ex, codigo, "{\"exito\":false,\"mensaje\":\"" + escapar(mensaje) + "\",\"datos\":null}");
    }

    // ==================================================================
    // 4B. AUTENTICACIÓN Y CONTROL DE ROLES EN LA API
    // ==================================================================
    private static String emitirToken(Usuario u) {
        if (u == null) return "";
        String token = UUID.randomUUID().toString();
        String nombreCompleto = ((u.getNombre() != null ? u.getNombre() : "") + " "
                + (u.getApellido() != null ? u.getApellido() : "")).trim();
        sesionesActivas.put(token, new SesionToken(
                u.getIdentificacion(),
                u.getRol(),
                nombreCompleto,
                u.getCorreo()
        ));
        return token;
    }

    private static SesionToken autenticarPeticion(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        String token = null;
        if (auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = auth.substring(7).trim();
        }
        if (token == null || token.isEmpty()) {
            token = valorDeQuery(ex.getRequestURI().getQuery(), "token");
        }
        if (token == null || token.isEmpty()) {
            return null;
        }
        return sesionesActivas.get(token);
    }

    private static boolean requiereRol(HttpExchange ex, SesionToken sesion, String... rolesPermitidos) throws IOException {
        if (sesion == null) {
            responderError(ex, 401, "Sesión expirada o no iniciada. Inicia sesión nuevamente.");
            return false;
        }
        for (String r : rolesPermitidos) {
            if (r.equalsIgnoreCase(sesion.rol)) {
                return true;
            }
        }
        responderError(ex, 403, "No tienes permisos para realizar esta acción.");
        return false;
    }

    private static boolean requierePersonalOPropio(HttpExchange ex, SesionToken sesion, String cedulaObjetivo) throws IOException {
        if (sesion == null) {
            responderError(ex, 401, "Sesión expirada o no iniciada. Inicia sesión nuevamente.");
            return false;
        }
        if ("SUPERADMIN".equals(sesion.rol) || "ADMIN".equals(sesion.rol) || "EMPLEADO".equals(sesion.rol)) {
            return true;
        }
        if ("CLIENTE".equals(sesion.rol) && sesion.identificacion.equalsIgnoreCase(cedulaObjetivo)) {
            return true;
        }
        responderError(ex, 403, "No tienes permisos para consultar o modificar datos de otro cliente.");
        return false;
    }

    private static boolean facturaPerteneceACliente(int idFactura, String cedula) {
        List<Factura> todas = facturaCtrl.listarTodasFacturas().getDatos();
        if (todas == null) return false;
        for (Factura f : todas) {
            if (f != null && f.getIdFactura() == idFactura) {
                return cedula.equalsIgnoreCase(f.getIdentificacionCliente());
            }
        }
        return false;
    }

    private static List<Soporte> filtrarTicketsCliente(List<Soporte> origen, SesionToken sesion) {
        List<Soporte> filtrados = new ArrayList<>();
        if (origen == null) return filtrados;
        for (Soporte t : origen) {
            if (t == null) continue;
            boolean porNombre = t.getUsuario() != null && t.getUsuario().equalsIgnoreCase(sesion.nombreCompleto);
            boolean porCorreo = t.getCorreo() != null && !sesion.correo.isEmpty()
                    && t.getCorreo().equalsIgnoreCase(sesion.correo);
            if (porNombre || porCorreo) {
                filtrados.add(t);
            }
        }
        return filtrados;
    }

    private static String leerCuerpo(HttpExchange ex) throws IOException {
        try (InputStream entrada = ex.getRequestBody()) {
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String ultimoSegmento(String ruta) {
        String[] partes = ruta.split("/");
        return partes[partes.length - 1];
    }

    private static String segmentoAnterior(String ruta) {
        String[] partes = ruta.split("/");
        return partes.length >= 2 ? partes[partes.length - 2] : "";
    }

    private static String valorDeQuery(String query, String clave) {
        if (query == null) {
            return null;
        }
        for (String par : query.split("&")) {
            String[] kv = par.split("=");
            if (kv.length == 2 && kv[0].equalsIgnoreCase(clave)) {
                return kv[1];
            }
        }
        return null;
    }

    /**
     * Lee un JSON con forma {"clave":[ {...}, {...} ]} y devuelve la lista de
     * objetos. Sirve para el catálogo de planes.
     */
    private static List<Map<String, String>> leerArrayDeObjetos(String json, String clave) {
        List<Map<String, String>> lista = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return lista;
        }

        int ini = json.indexOf("\"" + clave + "\"");
        if (ini < 0) {
            return lista;
        }
        int corchete = json.indexOf('[', ini);
        if (corchete < 0) {
            return lista;
        }

        int n = json.length();
        int i = corchete + 1;
        boolean enCadena = false;
        boolean escapado = false;
        int profundidad = 0;
        int inicioObj = -1;

        while (i < n) {
            char c = json.charAt(i);
            if (enCadena) {
                if (escapado) {
                    escapado = false;
                } else if (c == '\\') {
                    escapado = true;
                } else if (c == '"') {
                    enCadena = false;
                }
            } else {
                if (c == '"') {
                    enCadena = true;
                } else if (c == '{') {
                    if (profundidad == 0) {
                        inicioObj = i;
                    }
                    profundidad++;
                } else if (c == '}') {
                    profundidad--;
                    if (profundidad == 0 && inicioObj >= 0) {
                        lista.add(leerJson(json.substring(inicioObj, i + 1)));
                        inicioObj = -1;
                    }
                } else if (c == ']' && profundidad == 0) {
                    break;
                }
            }
            i++;
        }
        return lista;
    }

    /** Lee un JSON plano (cadenas con escapes, números enteros/decimales, booleanos y null). */
    private static Map<String, String> leerJson(String json) {
        Map<String, String> datos = new LinkedHashMap<>();
        if (json == null || json.isEmpty()) {
            return datos;
        }

        int n = json.length();
        int i = 0;
        while (i < n) {
            char c = json.charAt(i);
            if (c == '"') {
                int[] finClave = new int[1];
                String clave = leerCadenaJson(json, i, finClave);
                i = saltarEspacios(json, finClave[0]);
                if (i < n && json.charAt(i) == ':') {
                    i = saltarEspacios(json, i + 1);
                    if (i < n) {
                        char v = json.charAt(i);
                        if (v == '"') {
                            int[] finVal = new int[1];
                            String valor = leerCadenaJson(json, i, finVal);
                            datos.put(clave, valor);
                            i = finVal[0];
                        } else if (v == '{' || v == '[') {
                            i = saltarCompuestoJson(json, i);
                        } else {
                            int start = i;
                            while (i < n && ",}] \t\r\n".indexOf(json.charAt(i)) < 0) {
                                i++;
                            }
                            String token = json.substring(start, i);
                            if (!token.isEmpty() && !"null".equals(token)) {
                                datos.put(clave, token);
                            }
                        }
                        continue;
                    }
                }
            }
            i++;
        }
        return datos;
    }

    private static int saltarEspacios(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static String leerCadenaJson(String s, int inicioComilla, int[] finSalida) {
        StringBuilder sb = new StringBuilder();
        int i = inicioComilla + 1;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < n) {
                char sig = s.charAt(i + 1);
                switch (sig) {
                    case '"': sb.append('"'); i += 2; break;
                    case '\\': sb.append('\\'); i += 2; break;
                    case '/': sb.append('/'); i += 2; break;
                    case 'b': sb.append('\b'); i += 2; break;
                    case 'f': sb.append('\f'); i += 2; break;
                    case 'n': sb.append('\n'); i += 2; break;
                    case 'r': sb.append('\r'); i += 2; break;
                    case 't': sb.append('\t'); i += 2; break;
                    case 'u':
                        if (i + 5 < n) {
                            try {
                                sb.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                                i += 6;
                                break;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        sb.append(sig);
                        i += 2;
                        break;
                    default:
                        sb.append(sig);
                        i += 2;
                        break;
                }
            } else if (c == '"') {
                finSalida[0] = i + 1;
                return sb.toString();
            } else {
                sb.append(c);
                i++;
            }
        }
        finSalida[0] = n;
        return sb.toString();
    }

    private static int saltarCompuestoJson(String s, int inicio) {
        char apertura = s.charAt(inicio);
        char cierre = (apertura == '{') ? '}' : ']';
        int prof = 0;
        boolean enCadena = false;
        boolean escapado = false;
        int i = inicio;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (enCadena) {
                if (escapado) escapado = false;
                else if (c == '\\') escapado = true;
                else if (c == '"') enCadena = false;
            } else {
                if (c == '"') enCadena = true;
                else if (c == apertura) prof++;
                else if (c == cierre) {
                    prof--;
                    if (prof == 0) return i + 1;
                }
            }
            i++;
        }
        return s.length();
    }

    // ==================================================================
    // 5. DATOS DE EJEMPLO (se crean llamando a los métodos del equipo)
    // ==================================================================
    private static void sembrarClientesDeEjemplo() {
        // 1. Primero el catálogo de planes (PlanController usa el mismo PlanModel compartido)
        planCtrl.crearPlan("101", "Plan Básico 50M", "50 Mbps", "45000", "Internet residencial estándar");
        planCtrl.crearPlan("102", "Plan Fibra 100M", "100 Mbps", "65000", "Internet fibra simétrica");
        planCtrl.crearPlan("103", "Plan Fibra Gamer 300M", "300 Mbps", "95000", "Baja latencia para juegos");
        planCtrl.crearPlan("104", "Plan Corporativo 500M", "500 Mbps", "145000", "IP fija incluida");

        // 2. Clientes de ejemplo (todos con los métodos del controlador)
        usuarioCtrl.registrarCliente("Alejandro", "Escudero", "CC", "1045678901",
                "3001234567", "alejandrocastellar04@gmail.com", "Calle 10 # 20-30", "Clave123", "Clave123");

        usuarioCtrl.registrarCliente("Maria", "Gomez", "CC", "1098765432",
                "3109876543", "maria.gomez@email.com", "Carrera 15 # 45-10", "Clave456", "Clave456");

        usuarioCtrl.registrarCliente("Gyanela", "Meza", "CC", "1001900345",
                "3013154001", "gianelameza@gmail.com", "Calle 45 # 12-34", "Clave123", "Clave123");

        usuarioCtrl.registrarCliente("Luna", "Lopez", "CC", "45517179",
                "3023544779", "lunaloopez@gmail.com", "Carrera 7 # 80-15", "Clave123", "Clave123");

        // 3. Planes de cada cliente (asignarPlan)
        usuarioCtrl.asignarPlan("1045678901", "Plan Fibra 100M");
        usuarioCtrl.asignarPlan("1098765432", "Plan Básico 50M");
        usuarioCtrl.asignarPlan("1001900345", "Plan Básico 50M");
        usuarioCtrl.asignarPlan("45517179", "Plan Corporativo 500M");

        // 4. María queda inactiva para poder probar el estado y el filtro
        usuarioCtrl.cambiarEstadoCliente("1098765432", false);

        // 5. Empleados de ejemplo (Issue #13)
        empleadoCtrl.registrarEmpleado("Carlos", "Pineda", "CC", "1020304050",
                "3005556677", "carlos.pineda@winetcontrol.com", "Calle 3 # 4-5",
                "Técnico de campo", "Sucursal Local Norte", "Clave123");

        empleadoCtrl.registrarEmpleado("Lucia", "Ramirez", "CC", "1030405060",
                "3006667788", "lucia.ramirez@winetcontrol.com", "Carrera 9 # 8-7",
                "Atención al cliente", "Sucursal Local Norte", "Clave123");

        empleadoCtrl.registrarEmpleado("Andres", "Torres", "CC", "1040506070",
                "3007778899", "andres.torres@winetcontrol.com", "Avenida 5 # 6-7",
                "Supervisor", "Sucursal Centro", "Clave123");

        empleadoCtrl.cambiarEstadoEmpleado("1040506070", false);

        // 6. Tickets de soporte de ejemplo (Issue #10)
        soporteCtrl.radicarTicket("Alejandro Escudero", "alejandrocastellar04@gmail.com",
                "[Lentitud o Intermitencia] Lentitud en horario nocturno",
                "Presento caídas de velocidad entre las 8:00 p.m. y 10:00 p.m. en el sector norte.", false);

        soporteCtrl.radicarTicket("Maria Gomez", "maria.gomez@email.com",
                "[Configuración de Router / WiFi] Solicitud de cambio de clave WiFi",
                "Deseo actualizar la contraseña de mi router de fibra óptica por seguridad.", false);

        System.out.println("[seed] 4 planes, 4 clientes, 3 empleados y 2 tickets creados con los metodos del controlador.");
    }
}
