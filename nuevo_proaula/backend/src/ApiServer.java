import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import controladores.EmpleadoController;
import controladores.PlanController;
import controladores.UsuarioController;
import entidades.Administrador;
import entidades.Cliente;
import entidades.Empleado;
import entidades.Usuario;
import entidades.Plan;
import utils.Respuesta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    // Los controladores del equipo. Aquí NO hay lógica nueva: solo se les llama.
    private static final UsuarioController usuarioCtrl = new UsuarioController();
    private static final PlanController planCtrl = new PlanController();
    private static final EmpleadoController empleadoCtrl = new EmpleadoController();

    public static void main(String[] args) throws IOException {
        sembrarClientesDeEjemplo();

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        servidor.createContext(RUTA_BASE, ApiServer::manejarClientes);
        servidor.createContext(RUTA_PLANES, ApiServer::manejarPlanes);
        servidor.createContext(RUTA_EMPLEADOS, ApiServer::manejarEmpleados);
        servidor.createContext(RUTA_LOGIN, ApiServer::manejarLogin);
        servidor.setExecutor(null);
        servidor.start();

        System.out.println("=========================================================");
        System.out.println("  API WiNet Control escuchando en http://localhost:" + PUERTO);
        System.out.println("  Modulo: Gestion de Clientes (Issue #8 - Gyanela Meza)");
        System.out.println("  Ctrl+C para detener.");
        System.out.println("=========================================================");
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
            // GET /api/clientes  y  GET /api/clientes?estado=activos
            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_BASE)) {
                listarClientes(ex, query);
                return;
            }

            // GET /api/clientes/{cedula}
            if (metodo.equalsIgnoreCase("GET") && ruta.startsWith(RUTA_BASE + "/")) {
                buscarCliente(ex, ultimoSegmento(ruta));
                return;
            }

            // POST /api/clientes
            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_BASE)) {
                registrarCliente(ex);
                return;
            }

            // PUT /api/clientes/{cedula}/plan  ->  asignarPlan()
            if (metodo.equalsIgnoreCase("PUT") && ruta.endsWith("/plan")) {
                String cedulaPlan = segmentoAnterior(ruta);
                asignarPlan(ex, cedulaPlan);
                return;
            }

            // PUT /api/clientes/{cedula}/estado
            if (metodo.equalsIgnoreCase("PUT") && ruta.endsWith("/estado")) {
                String cedula = segmentoAnterior(ruta);
                cambiarEstado(ex, cedula);
                return;
            }

            // PUT /api/clientes/{cedula}
            if (metodo.equalsIgnoreCase("PUT") && ruta.startsWith(RUTA_BASE + "/")) {
                actualizarCliente(ex, ultimoSegmento(ruta));
                return;
            }

            // DELETE /api/clientes/{cedula}
            if (metodo.equalsIgnoreCase("DELETE") && ruta.startsWith(RUTA_BASE + "/")) {
                eliminarCliente(ex, ultimoSegmento(ruta));
                return;
            }

            responderError(ex, 404, "Ruta no encontrada: " + metodo + " " + ruta);

        } catch (Exception e) {
            responderError(ex, 500, "Error interno: " + e.getMessage());
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
    private static void actualizarCliente(HttpExchange ex, String cedula) throws IOException {
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
                datos.getOrDefault("planActual", "")
        );

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        // Si la petición trae el estado, se cambia con el método del equipo
        String activo = datos.get("activo");
        if (activo != null) {
            usuarioCtrl.cambiarEstadoCliente(cedula, Boolean.parseBoolean(activo));
        }

        responder(ex, 200, jsonDeRespuestaCliente(usuarioCtrl.buscarCliente(cedula)));
    }

    /** POST /api/clientes  ->  registrarCliente(...) */
    private static void registrarCliente(HttpExchange ex) throws IOException {
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

        // Plan y estado elegidos en el formulario (si vienen)
        String plan = datos.get("planActual");
        if (plan != null && !plan.isEmpty()) {
            usuarioCtrl.asignarPlan(cedula, plan);
        }
        String activo = datos.get("activo");
        if (activo != null) {
            usuarioCtrl.cambiarEstadoCliente(cedula, Boolean.parseBoolean(activo));
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
                sincronizarPlanes(ex);
                return;
            }

            responderError(ex, 404, "Ruta no encontrada: " + metodo + " " + ruta);

        } catch (Exception e) {
            responderError(ex, 500, "Error interno: " + e.getMessage());
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
                planCtrl.actualizarPlan(codigo, codigo, nombre, velocidad, precio, descripcion);
                actualizados++;
            } else {
                planCtrl.crearPlan(codigo, nombre, velocidad, precio, descripcion);
                creados++;
            }

            planCtrl.cambiarEstadoPlan(codigo, activo);
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
                    planCtrl.eliminarPlan(codigoActual);
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
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(respuesta.getMensaje())
                        + "\",\"datos\":" + jsonDeSesion(respuesta.getDatos()) + "}");
                return;
            }

            // 2. Empleados
            Respuesta<Empleado> respuestaEmpleado = empleadoCtrl.autenticar(identificador, contrasena);

            if (respuestaEmpleado.isExito()) {
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(respuestaEmpleado.getMensaje())
                        + "\",\"datos\":" + jsonDeSesion(respuestaEmpleado.getDatos()) + "}");
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
    private static String jsonDeSesion(Usuario u) {
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
                + "\"activo\":" + u.isActivo()
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
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
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
        List<Map<String, String>> lista = new java.util.ArrayList<>();
        if (json == null || json.isEmpty()) {
            return lista;
        }

        int ini = json.indexOf("\"" + clave + "\"");
        if (ini < 0) {
            return lista;
        }
        int corchete = json.indexOf('[', ini);
        int fin = json.lastIndexOf(']');
        if (corchete < 0 || fin < corchete) {
            return lista;
        }

        String contenido = json.substring(corchete + 1, fin);
        for (String objeto : contenido.split("\\}\\s*,\\s*\\{")) {
            String limpio = objeto.trim();
            if (limpio.startsWith("{")) limpio = limpio.substring(1);
            if (limpio.endsWith("}")) limpio = limpio.substring(0, limpio.length() - 1);
            if (limpio.isEmpty()) continue;
            lista.add(leerJson(limpio));
        }
        return lista;
    }

    /** Lee un JSON plano (solo texto y booleanos) sin librerías externas. */
    private static Map<String, String> leerJson(String json) {
        Map<String, String> datos = new LinkedHashMap<>();
        if (json == null || json.isEmpty()) {
            return datos;
        }
        Pattern patron = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|(true|false|null)|(-?[0-9]+))");
        Matcher m = patron.matcher(json);
        while (m.find()) {
            String clave = m.group(1);
            String valor;
            if (m.group(2) != null) {
                valor = m.group(2);
            } else if (m.group(3) != null) {
                valor = m.group(3);
            } else {
                valor = m.group(4);
            }
            datos.put(clave, valor);
        }
        return datos;
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

        System.out.println("[seed] 4 planes, 4 clientes y 3 empleados creados con los metodos del controlador.");
    }
}
