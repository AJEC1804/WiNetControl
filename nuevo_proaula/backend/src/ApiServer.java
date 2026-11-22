import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import controladores.EmpleadoController;
import controladores.EmpresaController;
import controladores.FacturaController;
import controladores.NotificacionController;
import controladores.PlanController;
import controladores.SoporteController;
import controladores.UsuarioController;
import entidades.Administrador;
import entidades.Cliente;
import entidades.Empleado;
import entidades.Empresa;
import entidades.Factura;
import entidades.Notificacion;
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

public class ApiServer {

    private static final int PUERTO = 8080;
    private static final String RUTA_BASE = "/api/clientes";
    private static final String RUTA_PLANES = "/api/planes";
    private static final String RUTA_EMPLEADOS = "/api/empleados";
    private static final String RUTA_LOGIN = "/api/login";
    private static final String RUTA_FACTURAS = "/api/facturas";
    private static final String RUTA_SOPORTE = "/api/soporte";
    private static final String RUTA_EMPRESAS = "/api/empresas";
    private static final String RUTA_NOTIFICACIONES = "/api/notificaciones";
    private static final String ARCHIVO_ATENDIDOS = "atendidos.dat";

    static {
        PersistenciaUtil.activar();
    }

    private static final UsuarioController usuarioCtrl = new UsuarioController();
    private static final PlanController planCtrl = new PlanController();
    private static final EmpleadoController empleadoCtrl = new EmpleadoController();
    private static final FacturaController facturaCtrl = new FacturaController();
    private static final SoporteController soporteCtrl = new SoporteController();
    private static final EmpresaController empresaCtrl = new EmpresaController();
    private static final NotificacionController notificacionCtrl = new NotificacionController();
    private static final List<Soporte> ticketsAtendidos = new ArrayList<>();
    private static final Map<String, SesionToken> sesionesActivas = new ConcurrentHashMap<>();

    private static final class SesionToken {
        final String identificacion;
        final String rol;
        final String nombreCompleto;
        final String correo;
        final String empresaId;

        SesionToken(String identificacion, String rol, String nombreCompleto, String correo, String empresaId) {
            this.identificacion = identificacion != null ? identificacion : "";
            this.rol = rol != null ? rol.toUpperCase() : "CLIENTE";
            this.nombreCompleto = nombreCompleto != null ? nombreCompleto : "";
            this.correo = correo != null ? correo : "";
            this.empresaId = (empresaId != null && !empresaId.trim().isEmpty()) ? empresaId.trim() : "EMP-001";
        }
    }

    public static void main(String[] args) throws IOException {

        cargarTicketsAtendidos();

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
            asegurarDatosBaseMultiTenant();
            System.out.println("[persistencia] Estado recuperado de la carpeta datos/");
        }

        HttpServer servidor = HttpServer.create(new InetSocketAddress(PUERTO), 0);
        servidor.createContext(RUTA_BASE, ApiServer::manejarClientes);
        servidor.createContext(RUTA_PLANES, ApiServer::manejarPlanes);
        servidor.createContext(RUTA_EMPLEADOS, ApiServer::manejarEmpleados);
        servidor.createContext(RUTA_LOGIN, ApiServer::manejarLogin);
        servidor.createContext(RUTA_FACTURAS, ApiServer::manejarFacturas);
        servidor.createContext(RUTA_SOPORTE, ApiServer::manejarSoporte);
        servidor.createContext(RUTA_EMPRESAS, ApiServer::manejarEmpresas);
        servidor.createContext(RUTA_NOTIFICACIONES, ApiServer::manejarNotificaciones);

        servidor.createContext("/", ApiServer::manejarRutaDesconocida);
        servidor.setExecutor(null);
        servidor.start();

        System.out.println("=========================================================");
        System.out.println("  API WiNet Control escuchando en http://localhost:" + PUERTO);
        System.out.println("  Modulo: Gestion de Clientes (Issue #8 - Gyanela Meza)");
        System.out.println("  Ctrl+C para detener.");
        System.out.println("=========================================================");
    }

    private static void persistirTodo() {
        if (!PersistenciaUtil.estaActiva()) {
            return;
        }
        UsuarioController.getSharedClienteModel().guardarEstado();
        UsuarioController.getSharedPlanModel().guardarEstado();
        UsuarioController.getSharedFacturaModel().guardarEstado();
        EmpleadoController.getSharedEmpleadoModel().guardarEstado();
        SoporteController.getSharedSoporteModel().guardarEstado();
        EmpresaController.getSharedEmpresaModel().guardarEstado();
        NotificacionController.getSharedNotificacionModel().guardarEstado();
        usuarioCtrl.guardarAdministradores();
        PersistenciaUtil.guardar(new ArrayList<>(ticketsAtendidos), ARCHIVO_ATENDIDOS);
    }

    @SuppressWarnings("unchecked")
    private static void cargarTicketsAtendidos() {
        Object leido = PersistenciaUtil.cargar(ARCHIVO_ATENDIDOS, Object.class);
        if (leido instanceof List) {
            ticketsAtendidos.clear();
            ticketsAtendidos.addAll((List<Soporte>) leido);
            System.out.println("[persistencia] Tickets atendidos recuperados: " + ticketsAtendidos.size());
        }
    }

    private static void manejarRutaDesconocida(HttpExchange ex) throws IOException {
        if (ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }
        responderError(ex, 404, "Ruta no encontrada: " + ex.getRequestMethod()
                + " " + ex.getRequestURI().getPath());
    }

    private static void manejarClientes(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();

        if (metodo.equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }

        try {
            SesionToken sesion = autenticarPeticion(ex);
            if (!verificarEmpresaActivaSiAplica(ex, sesion)) return;

            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_BASE)) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN", "EMPLEADO")) return;
                listarClientes(ex, query, sesion);
                return;
            }

            if (metodo.equalsIgnoreCase("GET") && ruta.startsWith(RUTA_BASE + "/")) {
                String cedulaBuscada = ultimoSegmento(ruta);
                if (!requierePersonalOPropio(ex, sesion, cedulaBuscada)) return;
                buscarCliente(ex, cedulaBuscada);
                return;
            }

            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_BASE)) {
                if (sesion != null && "CLIENTE".equals(sesion.rol)) {
                    responderError(ex, 403, "No tienes permisos para registrar clientes.");
                    return;
                }
                registrarCliente(ex, sesion);
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.endsWith("/plan")) {
                String cedulaPlan = segmentoAnterior(ruta);
                if (!requierePersonalOPropio(ex, sesion, cedulaPlan)) return;
                asignarPlan(ex, cedulaPlan);
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.endsWith("/estado")) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN", "EMPLEADO")) return;
                String cedula = segmentoAnterior(ruta);
                cambiarEstado(ex, cedula);
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.startsWith(RUTA_BASE + "/")) {
                String cedulaEdit = ultimoSegmento(ruta);
                if (!requierePersonalOPropio(ex, sesion, cedulaEdit)) return;
                actualizarCliente(ex, cedulaEdit, sesion);
                return;
            }

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

    private static void listarClientes(HttpExchange ex, String query, SesionToken sesion) throws IOException {
        String estado = valorDeQuery(query, "estado");

        Respuesta<List<Cliente>> respuesta;

        if ("activos".equalsIgnoreCase(estado)) {
            respuesta = usuarioCtrl.listarClientesPorEstado(true);
        } else if ("inactivos".equalsIgnoreCase(estado)) {
            respuesta = usuarioCtrl.listarClientesPorEstado(false);
        } else {
            respuesta = usuarioCtrl.listarClientes();
        }

        List<Cliente> base = respuesta.getDatos() != null ? respuesta.getDatos() : new ArrayList<>();
        List<Cliente> lista = new ArrayList<>();
        boolean filtrarPorEmpresa = sesion != null && !"SUPERADMIN".equalsIgnoreCase(sesion.rol)
                && !"PLATAFORMA".equalsIgnoreCase(sesion.empresaId);
        for (Cliente c : base) {
            if (c == null) continue;
            if (!filtrarPorEmpresa || sesion.empresaId.equalsIgnoreCase(c.getEmpresaId())) {
                lista.add(c);
            }
        }

        StringBuilder json = new StringBuilder();
        json.append("{\"exito\":").append(respuesta.isExito());
        json.append(",\"mensaje\":\"").append(escapar(respuesta.getMensaje())).append("\"");
        json.append(",\"datos\":[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) json.append(",");
            json.append(jsonDeCliente(lista.get(i)));
        }
        json.append("]}");

        responder(ex, 200, json.toString());
    }

    private static void buscarCliente(HttpExchange ex, String cedula) throws IOException {
        Respuesta<Cliente> respuesta = usuarioCtrl.buscarCliente(cedula);

        if (!respuesta.isExito()) {
            responderError(ex, 404, respuesta.getMensaje());
            return;
        }

        responder(ex, 200, jsonDeRespuestaCliente(respuesta));
    }

    private static void actualizarCliente(HttpExchange ex, String cedula, SesionToken sesion) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));

        Respuesta<Cliente> respuesta = usuarioCtrl.actualizarCliente(
                cedula,
                datos.getOrDefault("nombre", ""),
                datos.getOrDefault("apellido", ""),
                datos.getOrDefault("tipo", "CC"),
                datos.getOrDefault("telefono", ""),
                datos.getOrDefault("correo", ""),
                datos.getOrDefault("direccion", ""),
                datos.getOrDefault("contrasena", ""),
                datos.get("planActual")
        );

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        String activo = datos.get("activo");
        if (activo != null && sesion != null && ("SUPERADMIN".equals(sesion.rol) || "ADMIN".equals(sesion.rol) || "EMPLEADO".equals(sesion.rol))) {
            usuarioCtrl.cambiarEstadoCliente(cedula, Boolean.parseBoolean(activo));
        }

        responder(ex, 200, jsonDeRespuestaCliente(usuarioCtrl.buscarCliente(cedula)));
    }

    private static void registrarCliente(HttpExchange ex, SesionToken sesion) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));
        boolean esPersonalAutenticado = (sesion != null);

        String nombre = datos.getOrDefault("nombre", "");
        String apellido = datos.getOrDefault("apellido", "");
        String tipo = datos.getOrDefault("tipo", "CC");
        String cedula = datos.getOrDefault("identificacion", "");
        String telefono = datos.getOrDefault("telefono", "");
        String correo = datos.getOrDefault("correo", "");
        String direccion = datos.getOrDefault("direccion", "Sin dirección");
        String contrasena = datos.getOrDefault("contrasena", "Clave123");
        String empresaId = (sesion != null && !"SUPERADMIN".equalsIgnoreCase(sesion.rol)
                && !"PLATAFORMA".equalsIgnoreCase(sesion.empresaId))
                ? sesion.empresaId
                : datos.getOrDefault("empresaId", "EMP-001");

        Respuesta<Cliente> respuesta = usuarioCtrl.registrarCliente(
                nombre, apellido, tipo, cedula, telefono, correo,
                direccion, contrasena, contrasena, empresaId
        );

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

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

    private static void eliminarCliente(HttpExchange ex, String cedula) throws IOException {
        Respuesta<Boolean> respuesta = usuarioCtrl.eliminarCliente(cedula);

        if (!respuesta.isExito()) {
            responderError(ex, 400, respuesta.getMensaje());
            return;
        }

        responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(respuesta.getMensaje()) + "\"}");
    }

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
            if (!verificarEmpresaActivaSiAplica(ex, sesion)) return;

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

            if (metodo.equalsIgnoreCase("PUT")) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN", "EMPLEADO")) return;
                sincronizarPlanes(ex, sesion);
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

    private static void sincronizarPlanes(HttpExchange ex, SesionToken sesion) throws IOException {
        List<Map<String, String>> entrantes = leerArrayDeObjetos(leerCuerpo(ex), "planes");

        List<Plan> actuales = planCtrl.listarPlanes().getDatos();
        int creados = 0, actualizados = 0, eliminados = 0;

        if (sesion != null && "EMPLEADO".equalsIgnoreCase(sesion.rol)) {
            for (Map<String, String> datos : entrantes) {
                String codigo = datos.getOrDefault("codigo", "");
                if (datos.containsKey("activo")) {
                    boolean activo = Boolean.parseBoolean(datos.getOrDefault("activo", "true"));
                    planCtrl.cambiarEstadoPlan(codigo, activo);
                    actualizados++;
                }
            }
            responder(ex, 200, "{\"exito\":true,\"mensaje\":\"Estados de planes actualizados por empleado.\","
                    + "\"datos\":{\"creados\":0,\"actualizados\":" + actualizados
                    + ",\"eliminados\":0}}");
            return;
        }

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
            if (!verificarEmpresaActivaSiAplica(ex, sesion)) return;
            if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN")) return;

            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_EMPLEADOS)) {
                listarEmpleados(ex, query, sesion);
                return;
            }

            if (metodo.equalsIgnoreCase("GET") && ruta.startsWith(RUTA_EMPLEADOS + "/")) {
                Respuesta<Empleado> respuesta = empleadoCtrl.buscarEmpleado(ultimoSegmento(ruta));
                if (!respuesta.isExito()) {
                    responderError(ex, 404, respuesta.getMensaje());
                    return;
                }
                if (!empleadoPerteneceASesion(respuesta.getDatos(), sesion)) {
                    responderError(ex, 403, "No tienes permisos sobre empleados de otra organización.");
                    return;
                }
                responder(ex, 200, jsonDeRespuestaEmpleado(respuesta));
                return;
            }

            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_EMPLEADOS)) {
                registrarEmpleado(ex, sesion);
                return;
            }

            if (metodo.equalsIgnoreCase("DELETE") && ruta.startsWith(RUTA_EMPLEADOS + "/")) {
                String cedulaObjetivo = ultimoSegmento(ruta);
                Respuesta<Empleado> empTarget = empleadoCtrl.buscarEmpleado(cedulaObjetivo);
                if (empTarget.isExito() && !empleadoPerteneceASesion(empTarget.getDatos(), sesion)) {
                    responderError(ex, 403, "No tienes permisos para eliminar empleados de otra organización.");
                    return;
                }
                if (!"SUPERADMIN".equalsIgnoreCase(sesion.rol)) {
                    if ("9999999999".equals(cedulaObjetivo) || (empTarget.isExito() && "SUPERADMIN".equalsIgnoreCase(empTarget.getDatos().getRol()))) {
                        responderError(ex, 403, "Un Administrador no puede eliminar al Super Administrador.");
                        return;
                    }
                }
                Respuesta<Boolean> respuesta = empleadoCtrl.eliminarEmpleado(cedulaObjetivo);
                if (!respuesta.isExito()) {
                    responderError(ex, 400, respuesta.getMensaje());
                    return;
                }
                persistirTodo();
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\""
                        + escapar(respuesta.getMensaje()) + "\"}");
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.endsWith("/estado")) {
                cambiarEstadoEmpleado(ex, sesion, segmentoAnterior(ruta));
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.startsWith(RUTA_EMPLEADOS + "/")) {
                actualizarEmpleado(ex, sesion, ultimoSegmento(ruta));
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
    private static boolean empleadoPerteneceASesion(Empleado e, SesionToken sesion) {
        if (e == null || sesion == null) return false;
        String empObjetivo = "SUPERADMIN".equalsIgnoreCase(sesion.rol) ? "PLATAFORMA" : sesion.empresaId;
        return empObjetivo.equalsIgnoreCase(e.getEmpresaId());
    }

    private static void listarEmpleados(HttpExchange ex, String query, SesionToken sesion) throws IOException {
        String estado = valorDeQuery(query, "estado");
        String buscar = valorDeQuery(query, "buscar");
        String empresaFiltro = "SUPERADMIN".equalsIgnoreCase(sesion.rol) ? "PLATAFORMA" : sesion.empresaId;

        StringBuilder json = new StringBuilder();

        if (buscar != null && !buscar.isEmpty()) {
            Respuesta<metodos.ListaEmpleados> respuesta = empleadoCtrl.buscarEmpleados(buscar);

            json.append("{\"exito\":").append(respuesta.isExito());
            json.append(",\"mensaje\":\"").append(escapar(respuesta.getMensaje())).append("\"");
            json.append(",\"datos\":[");

            metodos.ListaEmpleados.NodoEmpleado sapo = respuesta.getDatos().getPrimero();
            boolean primeroItem = true;
            while (sapo != null) {
                Empleado emp = sapo.getDato();
                if (emp != null && empresaFiltro.equalsIgnoreCase(emp.getEmpresaId())) {
                    if (!primeroItem) json.append(",");
                    json.append(jsonDeEmpleado(emp));
                    primeroItem = false;
                }
                sapo = sapo.getSiguiente();
            }

            json.append("]}");
            responder(ex, 200, json.toString());
            return;
        }

        Respuesta<List<Empleado>> respuesta;

        if ("activos".equalsIgnoreCase(estado)) {
            respuesta = empleadoCtrl.listarEmpleadosPorEmpresaYEstado(empresaFiltro, true);
        } else if ("inactivos".equalsIgnoreCase(estado)) {
            respuesta = empleadoCtrl.listarEmpleadosPorEmpresaYEstado(empresaFiltro, false);
        } else {
            respuesta = empleadoCtrl.listarEmpleadosPorEmpresa(empresaFiltro);
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

    private static void registrarEmpleado(HttpExchange ex, SesionToken sesion) throws IOException {
        Map<String, String> datos = leerJson(leerCuerpo(ex));
        boolean esSuper = sesion != null && "SUPERADMIN".equalsIgnoreCase(sesion.rol);
        String empresaDestino = esSuper ? "PLATAFORMA" : sesion.empresaId;
        String sucursalDefault = esSuper ? "Plataforma Central" : "Mi Empresa";
        if (!esSuper) {
            Empresa empObj = empresaCtrl.obtenerEmpresaDirecta(empresaDestino);
            if (empObj != null && !empObj.getNombre().isEmpty()) {
                sucursalDefault = empObj.getNombre();
            }
        }

        Respuesta<Empleado> respuesta = empleadoCtrl.registrarEmpleado(
                datos.getOrDefault("nombre", ""),
                datos.getOrDefault("apellido", ""),
                datos.getOrDefault("tipo", "CC"),
                datos.getOrDefault("identificacion", ""),
                datos.getOrDefault("telefono", ""),
                datos.getOrDefault("correo", ""),
                datos.getOrDefault("direccion", "Sin dirección"),
                datos.getOrDefault("cargo", esSuper ? "Soporte Plataforma" : "Atención al cliente"),
                datos.getOrDefault("sucursal", sucursalDefault),
                datos.get("contrasena"),
                empresaDestino
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

        empleadoCtrl.cambiarRolEmpleado(respuesta.getDatos().getIdentificacion(), "EMPLEADO");

        responder(ex, 201, jsonDeRespuestaEmpleado(
                empleadoCtrl.buscarEmpleado(respuesta.getDatos().getIdentificacion())));
    }

    private static void actualizarEmpleado(HttpExchange ex, SesionToken sesion, String cedula) throws IOException {
        Respuesta<Empleado> empTarget = empleadoCtrl.buscarEmpleado(cedula);
        if (empTarget.isExito() && !empleadoPerteneceASesion(empTarget.getDatos(), sesion)) {
            responderError(ex, 403, "No tienes permisos para modificar empleados de otra organización.");
            return;
        }
        if (sesion != null && !"SUPERADMIN".equalsIgnoreCase(sesion.rol)) {
            if ("9999999999".equals(cedula) || (empTarget.isExito() && "SUPERADMIN".equalsIgnoreCase(empTarget.getDatos().getRol()))) {
                responderError(ex, 403, "Un Administrador no puede modificar al Super Administrador.");
                return;
            }
        }
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

        responder(ex, 200, jsonDeRespuestaEmpleado(empleadoCtrl.buscarEmpleado(cedula)));
    }

    private static void cambiarEstadoEmpleado(HttpExchange ex, SesionToken sesion, String cedula) throws IOException {
        Respuesta<Empleado> empTarget = empleadoCtrl.buscarEmpleado(cedula);
        if (empTarget.isExito() && !empleadoPerteneceASesion(empTarget.getDatos(), sesion)) {
            responderError(ex, 403, "No tienes permisos para cambiar el estado de empleados de otra organización.");
            return;
        }
        if (sesion != null && !"SUPERADMIN".equalsIgnoreCase(sesion.rol)) {
            if ("9999999999".equals(cedula) || (empTarget.isExito() && "SUPERADMIN".equalsIgnoreCase(empTarget.getDatos().getRol()))) {
                responderError(ex, 403, "Un Administrador no puede cambiar el estado del Super Administrador.");
                return;
            }
        }
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
                + "\"empresaId\":\"" + escapar(e.getEmpresaId()) + "\","
                + "\"fechaIngreso\":\"" + escapar(e.getFechaIngreso()) + "\","
                + "\"rol\":\"" + escapar(e.getRol()) + "\","
                + "\"activo\":" + e.isActivo()
                + "}";
    }

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

            String identificador = datos.getOrDefault("usuario", datos.getOrDefault("identificador", ""));
            String contrasena = datos.getOrDefault("contrasena", "");

            Respuesta<Usuario> respuesta = usuarioCtrl.autenticarUsuario(identificador, contrasena);

            if (respuesta.isExito()) {
                Usuario u = respuesta.getDatos();
                String empId = extraerEmpresaId(u);
                if (!"SUPERADMIN".equalsIgnoreCase(u.getRol()) && !"CLIENTE".equalsIgnoreCase(u.getRol())
                        && !empresaCtrl.estaEmpresaActiva(empId)) {
                    responderEmpresaSuspendida(ex, empId);
                    return;
                }
                String token = emitirToken(u);
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(respuesta.getMensaje())
                        + "\",\"datos\":" + jsonDeSesion(u, token) + "}");
                return;
            }

            Respuesta<Empleado> respuestaEmpleado = empleadoCtrl.autenticar(identificador, contrasena);

            if (respuestaEmpleado.isExito()) {
                Empleado emp = respuestaEmpleado.getDatos();
                if (!empresaCtrl.estaEmpresaActiva(emp.getEmpresaId())) {
                    responderEmpresaSuspendida(ex, emp.getEmpresaId());
                    return;
                }
                String token = emitirToken(emp);
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(respuestaEmpleado.getMensaje())
                        + "\",\"datos\":" + jsonDeSesion(emp, token) + "}");
                return;
            }

            Respuesta<Usuario> errorAdmin = respuesta;
            String mensajeFinal = errorAdmin.getMensaje();
            int codigoFinal = errorAdmin.getCodigo();

            if (codigoFinal == 401 && respuestaEmpleado.getCodigo() != 401) {
                mensajeFinal = respuestaEmpleado.getMensaje();
                codigoFinal = respuestaEmpleado.getCodigo();
            }

            responderError(ex, codigoFinal, mensajeFinal);

        } catch (Exception e) {
            responderError(ex, 500, "Error interno: " + e.getMessage());
        }
    }

    private static String jsonDeSesion(Usuario u, String token) {
        if (u == null) {
            return "null";
        }

        String planActual = "";
        String cargo = "";
        String sucursal = "";
        String empresaId = extraerEmpresaId(u);

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
                + "\"empresaId\":\"" + escapar(empresaId) + "\","
                + "\"token\":\"" + escapar(token) + "\","
                + "\"activo\":" + u.isActivo()
                + "}";
    }

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
            if (!verificarEmpresaActivaSiAplica(ex, sesion)) return;

            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_FACTURAS + "/pdf")) {
                servirPdfFactura(ex, valorDeQuery(query, "id"), sesion);
                return;
            }

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
                List<Factura> base = resp.getDatos() != null ? resp.getDatos() : new ArrayList<>();
                List<Factura> lista = new ArrayList<>();
                boolean filtrarEmp = !"CLIENTE".equals(sesion.rol)
                        && !"SUPERADMIN".equalsIgnoreCase(sesion.rol)
                        && !"PLATAFORMA".equalsIgnoreCase(sesion.empresaId);
                for (Factura f : base) {
                    if (f == null) continue;
                    if (!filtrarEmp || sesion.empresaId.equalsIgnoreCase(f.getEmpresaId())) {
                        lista.add(f);
                    }
                }
                Factura cima;
                if ("CLIENTE".equals(sesion.rol) || filtrarEmp) {
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

            if (metodo.equalsIgnoreCase("PUT") && ruta.equals(RUTA_FACTURAS + "/anular")) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN")) return;
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                int idBuscado = 0;
                try {
                    idBuscado = Integer.parseInt(datos.getOrDefault("idFactura", "0").trim());
                } catch (NumberFormatException ignored) {
                }
                Respuesta<Factura> resp = facturaCtrl.anularFactura(idBuscado);
                int codigo = resp.isExito() ? 200 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeFactura(resp.getDatos()) + "}");
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.equals(RUTA_FACTURAS + "/estado")) {
                if (!requiereRol(ex, sesion, "SUPERADMIN", "ADMIN", "EMPLEADO")) return;
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                int idBuscado = 0;
                try {
                    idBuscado = Integer.parseInt(datos.getOrDefault("idFactura", "0").trim());
                } catch (NumberFormatException ignored) {
                }
                String nuevoEstado = datos.getOrDefault("estado", "");
                Respuesta<Factura> resp = facturaCtrl.cambiarEstadoFactura(idBuscado, nuevoEstado);
                int codigo = resp.isExito() ? 200 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeFactura(resp.getDatos()) + "}");
                return;
            }

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

    private static void manejarSoporte(HttpExchange ex) throws IOException {
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
            if (!verificarEmpresaActivaSiAplica(ex, sesion)) return;

            String ambito = valorDeQuery(query, "ambito");
            boolean esEmpleadoPlataforma = "EMPLEADO".equals(sesion.rol) && "PLATAFORMA".equalsIgnoreCase(sesion.empresaId);
            boolean esSuper = "SUPERADMIN".equals(sesion.rol) || esEmpleadoPlataforma;
            boolean modoPlataforma = esSuper || ("ADMIN".equals(sesion.rol) && "plataforma".equalsIgnoreCase(ambito));

            String tipoTicketFiltro = modoPlataforma ? "PLATAFORMA" : "CLIENTE";
            String empresaFiltro = modoPlataforma
                    ? (esSuper ? "" : sesion.empresaId)
                    : sesion.empresaId;

            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_SOPORTE)) {
                List<Soporte> pendientes = soporteCtrl.listarPendientesFiltrados(tipoTicketFiltro, empresaFiltro);
                List<Soporte> enProceso = soporteCtrl.listarEnProcesoFiltrados(tipoTicketFiltro, empresaFiltro);
                List<Soporte> atendidos = soporteCtrl.listarCerradosFiltrados(tipoTicketFiltro, empresaFiltro);

                if ("CLIENTE".equals(sesion.rol)) {
                    pendientes = filtrarTicketsCliente(pendientes, sesion);
                    enProceso = filtrarTicketsCliente(enProceso, sesion);
                    atendidos = filtrarTicketsCliente(atendidos, sesion);
                } else if ("ADMIN".equals(sesion.rol) && modoPlataforma) {
                    pendientes = filtrarTicketsCliente(pendientes, sesion);
                    enProceso = filtrarTicketsCliente(enProceso, sesion);
                    atendidos = filtrarTicketsCliente(atendidos, sesion);
                }

                Soporte frente = pendientes.isEmpty() ? null : pendientes.get(0);

                StringBuilder json = new StringBuilder();
                json.append("{\"exito\":true,");
                json.append("\"ambito\":\"").append(tipoTicketFiltro).append("\",");
                json.append("\"frente\":").append(jsonDeSoporte(frente)).append(",");
                json.append("\"enProceso\":[");
                for (int i = 0; i < enProceso.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(jsonDeSoporte(enProceso.get(i)));
                }
                json.append("],\"pendientes\":[");
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

            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_SOPORTE)) {
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                String tipoSolicitado = datos.getOrDefault("tipoTicket", modoPlataforma ? "PLATAFORMA" : "CLIENTE");
                if (!"ADMIN".equals(sesion.rol)) {
                    tipoSolicitado = "CLIENTE";
                }

                String usuarioNombre = sesion.nombreCompleto.isEmpty() ? sesion.identificacion : sesion.nombreCompleto;
                if (!"CLIENTE".equals(sesion.rol) && !"ADMIN".equals(sesion.rol)
                        && datos.containsKey("usuario") && !datos.get("usuario").trim().isEmpty()) {
                    usuarioNombre = datos.get("usuario").trim();
                }

                String correo = datos.getOrDefault("correo", sesion.correo);
                if (correo == null || correo.trim().isEmpty()) correo = sesion.correo;

                String categoria = datos.getOrDefault("categoria", "");
                String asunto = datos.getOrDefault("asunto", "");
                String mensaje = datos.getOrDefault("mensaje", "");

                String asuntoFinal = (!categoria.isEmpty() && !asunto.startsWith("["))
                        ? "[" + categoria + "] " + asunto
                        : asunto;

                Respuesta<Soporte> resp = soporteCtrl.radicarTicket(
                        usuarioNombre,
                        sesion.identificacion,
                        sesion.rol,
                        correo,
                        asuntoFinal,
                        mensaje,
                        tipoSolicitado,
                        sesion.empresaId,
                        true
                );
                int codigo = resp.isExito() ? 201 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeSoporte(resp.getDatos()) + "}");
                return;
            }

            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_SOPORTE + "/iniciar")) {
                if ("CLIENTE".equals(sesion.rol)) {
                    responderError(ex, 403, "No tienes permisos para iniciar tickets.");
                    return;
                }
                if ("ADMIN".equals(sesion.rol) && modoPlataforma) {
                    responderError(ex, 403, "Solo el Superusuario o su equipo de plataforma pueden atender tickets de plataforma.");
                    return;
                }
                String tipoIniciar = esSuper ? "PLATAFORMA" : "CLIENTE";
                String empIniciar = esSuper ? "" : sesion.empresaId;
                Respuesta<Soporte> resp = soporteCtrl.iniciarSiguienteTicket(tipoIniciar, empIniciar, sesion.nombreCompleto);
                int codigo = resp.isExito() ? 200 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeSoporte(resp.getDatos()) + "}");
                return;
            }

            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_SOPORTE + "/cerrar")) {
                if ("CLIENTE".equals(sesion.rol)) {
                    responderError(ex, 403, "No tienes permisos para cerrar tickets.");
                    return;
                }
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                int idTicket = 0;
                try {
                    idTicket = Integer.parseInt(datos.getOrDefault("idTicket", "0").trim());
                } catch (NumberFormatException ignored) {
                }
                String observacion = datos.getOrDefault("observacion", "");

                Soporte ticketObj = soporteCtrl.buscarTicket(idTicket);
                if (ticketObj != null) {
                    if ("PLATAFORMA".equalsIgnoreCase(ticketObj.getTipoTicket()) && !esSuper) {
                        responderError(ex, 403, "Solo el equipo de plataforma puede cerrar tickets de plataforma.");
                        return;
                    }
                    if (!"PLATAFORMA".equalsIgnoreCase(ticketObj.getTipoTicket()) && esSuper) {
                        responderError(ex, 403, "Los tickets de clientes los atiende su empresa.");
                        return;
                    }
                }

                Respuesta<Soporte> resp = soporteCtrl.cerrarTicket(idTicket, observacion, sesion.nombreCompleto);
                if (resp.isExito() && resp.getDatos() != null) {
                    ticketsAtendidos.add(0, resp.getDatos());
                }
                int codigo = resp.isExito() ? 200 : resp.getCodigo();
                responder(ex, codigo, "{\"exito\":" + resp.isExito()
                        + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\""
                        + ",\"datos\":" + jsonDeSoporte(resp.getDatos()) + "}");
                return;
            }

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
        Empresa emp = empresaCtrl.obtenerEmpresaDirecta(s.getEmpresaId());
        String empresaNombre = emp != null ? emp.getNombre() : s.getEmpresaId();
        return "{"
                + "\"idTicket\":" + s.getIdTicket() + ","
                + "\"usuario\":\"" + escapar(s.getUsuario()) + "\","
                + "\"remitenteId\":\"" + escapar(s.getRemitenteId()) + "\","
                + "\"remitenteRol\":\"" + escapar(s.getRemitenteRol()) + "\","
                + "\"correo\":\"" + escapar(s.getCorreo()) + "\","
                + "\"asunto\":\"" + escapar(s.getAsunto()) + "\","
                + "\"mensaje\":\"" + escapar(s.getMensaje()) + "\","
                + "\"fecha\":\"" + escapar(s.getFecha()) + "\","
                + "\"estado\":\"" + escapar(s.getEstado()) + "\","
                + "\"observacion\":\"" + escapar(s.getObservacion()) + "\","
                + "\"fechaInicio\":\"" + escapar(s.getFechaInicio()) + "\","
                + "\"fechaCierre\":\"" + escapar(s.getFechaCierre()) + "\","
                + "\"atendidoPor\":\"" + escapar(s.getAtendidoPor()) + "\","
                + "\"tipoTicket\":\"" + escapar(s.getTipoTicket()) + "\","
                + "\"empresaId\":\"" + escapar(s.getEmpresaId()) + "\","
                + "\"empresaNombre\":\"" + escapar(empresaNombre) + "\""
                + "}";
    }

    private static void manejarEmpresas(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();
        String query = ex.getRequestURI().getQuery();

        if (metodo.equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }

        try {

            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_EMPRESAS + "/publicas")) {
                List<Empresa> activas = empresaCtrl.listarEmpresasPorEstado(true).getDatos();
                StringBuilder json = new StringBuilder("{\"exito\":true,\"datos\":[");
                if (activas != null) {
                    for (int i = 0; i < activas.size(); i++) {
                        if (i > 0) json.append(",");
                        Empresa e = activas.get(i);
                        json.append("{\"idEmpresa\":\"").append(escapar(e.getIdEmpresa()))
                            .append("\",\"nombre\":\"").append(escapar(e.getNombre())).append("\"}");
                    }
                }
                json.append("]}");
                responder(ex, 200, json.toString());
                return;
            }

            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_EMPRESAS + "/contacto-suspension")) {
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                String empresaId = datos.getOrDefault("empresaId", "EMP-DESCONOCIDA");
                String nombre = datos.getOrDefault("nombre", "Administrador de Empresa");
                String correo = datos.getOrDefault("correo", "");
                String mensaje = datos.getOrDefault("mensaje", "");
                if (mensaje.trim().isEmpty()) {
                    responderError(ex, 400, "Escribe el mensaje para el administrador de la plataforma.");
                    return;
                }
                Respuesta<Soporte> resp = soporteCtrl.radicarTicket(
                        nombre,
                        datos.getOrDefault("identificacion", empresaId),
                        "ADMIN",
                        correo,
                        "[Revisión de Suspensión / Pago] " + datos.getOrDefault("empresaNombre", empresaId),
                        mensaje,
                        "PLATAFORMA",
                        empresaId,
                        true
                );
                persistirTodo();
                responder(ex, resp.isExito() ? 201 : 400,
                        "{\"exito\":" + resp.isExito() + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\"}");
                return;
            }

            SesionToken sesion = autenticarPeticion(ex);
            if (!requiereRol(ex, sesion, "SUPERADMIN")) return;

            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_EMPRESAS)) {
                String estado = valorDeQuery(query, "estado");
                Respuesta<List<Empresa>> resp = "activos".equalsIgnoreCase(estado)
                        ? empresaCtrl.listarEmpresasPorEstado(true)
                        : ("inactivos".equalsIgnoreCase(estado)
                            ? empresaCtrl.listarEmpresasPorEstado(false)
                            : empresaCtrl.listarEmpresas());
                List<Empresa> lista = resp.getDatos() != null ? resp.getDatos() : new ArrayList<>();
                StringBuilder json = new StringBuilder();
                json.append("{\"exito\":true,\"mensaje\":\"").append(escapar(resp.getMensaje())).append("\",\"datos\":[");
                for (int i = 0; i < lista.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append(jsonDeEmpresa(lista.get(i)));
                }
                json.append("]}");
                responder(ex, 200, json.toString());
                return;
            }

            if (metodo.equalsIgnoreCase("POST") && ruta.equals(RUTA_EMPRESAS)) {
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                double precio = 250000;
                try {
                    if (datos.containsKey("precioMensual") && !datos.get("precioMensual").isEmpty()) {
                        precio = Double.parseDouble(datos.get("precioMensual").trim());
                    }
                } catch (NumberFormatException ignored) {
                }
                boolean activa = !"false".equalsIgnoreCase(datos.getOrDefault("activo", "true"));
                Empresa nueva = new Empresa(
                        datos.getOrDefault("idEmpresa", ""),
                        datos.getOrDefault("nombre", ""),
                        datos.getOrDefault("nit", ""),
                        datos.getOrDefault("correo", ""),
                        datos.getOrDefault("telefono", ""),
                        datos.getOrDefault("direccion", "Sin dirección"),
                        datos.getOrDefault("planPlataforma", "Plan SaaS Empresarial"),
                        precio,
                        activa,
                        datos.getOrDefault("adminIdentificacion", "")
                );

                Respuesta<Empresa> respEmp = empresaCtrl.registrarEmpresa(nueva);
                if (!respEmp.isExito()) {
                    responderError(ex, 400, respEmp.getMensaje());
                    return;
                }

                String adminId = datos.getOrDefault("adminIdentificacion", "").trim();
                if (!adminId.isEmpty()) {
                    Administrador nuevoAdmin = new Administrador(
                            datos.getOrDefault("adminNombre", "Administrador"),
                            datos.getOrDefault("adminApellido", nueva.getNombre()),
                            "CC",
                            adminId,
                            datos.getOrDefault("adminTelefono", nueva.getTelefono()),
                            datos.getOrDefault("adminCorreo", nueva.getCorreo()),
                            nueva.getDireccion(),
                            datos.getOrDefault("adminContrasena", "Admin123!"),
                            "ADMIN",
                            nueva.getNombre(),
                            respEmp.getDatos().getIdEmpresa()
                    );
                    Respuesta<Administrador> respAdmin = usuarioCtrl.registrarAdministrador(nuevoAdmin);
                    if (!respAdmin.isExito()) {
                        empresaCtrl.eliminarEmpresa(respEmp.getDatos().getIdEmpresa());
                        responderError(ex, 400, respAdmin.getMensaje());
                        return;
                    }
                    respEmp.getDatos().setAdminIdentificacion(adminId);
                }

                responder(ex, 201, "{\"exito\":true,\"mensaje\":\"Empresa registrada exitosamente.\",\"datos\":"
                        + jsonDeEmpresa(respEmp.getDatos()) + "}");
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.endsWith("/estado")) {
                String idEmp = segmentoAnterior(ruta);
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                boolean activo = Boolean.parseBoolean(datos.getOrDefault("activo", "false"));
                String motivo = datos.getOrDefault("motivoSuspension", "");
                Respuesta<Empresa> resp = empresaCtrl.cambiarEstadoEmpresa(idEmp, activo, motivo);
                if (!resp.isExito()) {
                    responderError(ex, resp.getCodigo(), resp.getMensaje());
                    return;
                }
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(resp.getMensaje())
                        + "\",\"datos\":" + jsonDeEmpresa(resp.getDatos()) + "}");
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.startsWith(RUTA_EMPRESAS + "/")) {
                String idEmp = ultimoSegmento(ruta);
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                Empresa edit = new Empresa();
                edit.setNombre(datos.getOrDefault("nombre", ""));
                edit.setNit(datos.getOrDefault("nit", ""));
                edit.setCorreo(datos.getOrDefault("correo", ""));
                edit.setTelefono(datos.getOrDefault("telefono", ""));
                edit.setDireccion(datos.getOrDefault("direccion", ""));
                edit.setPlanPlataforma(datos.getOrDefault("planPlataforma", ""));
                try {
                    if (datos.containsKey("precioMensual") && !datos.get("precioMensual").isEmpty()) {
                        edit.setPrecioMensual(Double.parseDouble(datos.get("precioMensual").trim()));
                    }
                } catch (NumberFormatException ignored) {
                }
                if (datos.containsKey("motivoSuspension")) {
                    edit.setMotivoSuspension(datos.get("motivoSuspension"));
                }
                edit.setActivo(!"false".equalsIgnoreCase(datos.getOrDefault("activo", "true")));

                Respuesta<Empresa> resp = empresaCtrl.actualizarEmpresa(idEmp, edit);
                if (!resp.isExito()) {
                    responderError(ex, resp.getCodigo(), resp.getMensaje());
                    return;
                }
                usuarioCtrl.actualizarEmpresaEnAdmins(idEmp, resp.getDatos().getNombre());
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(resp.getMensaje())
                        + "\",\"datos\":" + jsonDeEmpresa(resp.getDatos()) + "}");
                return;
            }

            if (metodo.equalsIgnoreCase("DELETE") && ruta.startsWith(RUTA_EMPRESAS + "/")) {
                String idEmp = ultimoSegmento(ruta);
                Respuesta<Boolean> resp = empresaCtrl.eliminarEmpresa(idEmp);
                if (!resp.isExito()) {
                    responderError(ex, resp.getCodigo(), resp.getMensaje());
                    return;
                }
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(resp.getMensaje()) + "\"}");
                return;
            }

            responderError(ex, 404, "Ruta de empresas no encontrada.");
        } catch (Exception e) {
            responderError(ex, 500, "Error interno en empresas: " + e.getMessage());
        } finally {
            if (!metodo.equalsIgnoreCase("GET") && !metodo.equalsIgnoreCase("OPTIONS")) {
                persistirTodo();
            }
        }
    }

    private static String jsonDeEmpresa(Empresa e) {
        if (e == null) return "null";
        Administrador admin = usuarioCtrl.buscarAdministradorPorEmpresa(e.getIdEmpresa());
        String adminNombre = admin != null ? admin.getNombreCompleto() : "Sin administrador";
        String adminId = admin != null ? admin.getIdentificacion() : e.getAdminIdentificacion();
        String adminCorreo = admin != null ? admin.getCorreo() : "";
        List<Cliente> clientesEmp = usuarioCtrl.listarClientesPorEmpresa(e.getIdEmpresa()).getDatos();
        int totalClientes = clientesEmp != null ? clientesEmp.size() : 0;
        List<Empleado> empleadosEmp = empleadoCtrl.listarEmpleadosPorEmpresa(e.getIdEmpresa()).getDatos();
        int totalEmpleados = empleadosEmp != null ? empleadosEmp.size() : 0;

        return "{"
                + "\"idEmpresa\":\"" + escapar(e.getIdEmpresa()) + "\","
                + "\"nombre\":\"" + escapar(e.getNombre()) + "\","
                + "\"nit\":\"" + escapar(e.getNit()) + "\","
                + "\"correo\":\"" + escapar(e.getCorreo()) + "\","
                + "\"telefono\":\"" + escapar(e.getTelefono()) + "\","
                + "\"direccion\":\"" + escapar(e.getDireccion()) + "\","
                + "\"planPlataforma\":\"" + escapar(e.getPlanPlataforma()) + "\","
                + "\"precioMensual\":" + Math.round(e.getPrecioMensual()) + ","
                + "\"fechaRegistro\":\"" + escapar(e.getFechaRegistro()) + "\","
                + "\"activo\":" + e.isActivo() + ","
                + "\"estado\":\"" + escapar(e.getEstado()) + "\","
                + "\"motivoSuspension\":\"" + escapar(e.getMotivoSuspension()) + "\","
                + "\"adminIdentificacion\":\"" + escapar(adminId) + "\","
                + "\"adminNombre\":\"" + escapar(adminNombre) + "\","
                + "\"adminCorreo\":\"" + escapar(adminCorreo) + "\","
                + "\"totalClientes\":" + totalClientes + ","
                + "\"totalEmpleados\":" + totalEmpleados
                + "}";
    }

    private static void manejarNotificaciones(HttpExchange ex) throws IOException {
        String metodo = ex.getRequestMethod();
        String ruta = ex.getRequestURI().getPath();

        if (metodo.equalsIgnoreCase("OPTIONS")) {
            responder(ex, 204, "");
            return;
        }

        try {
            SesionToken sesion = autenticarPeticion(ex);
            if (sesion == null) {
                responderError(ex, 401, "Sesión expirada o no iniciada.");
                return;
            }

            if (metodo.equalsIgnoreCase("GET") && ruta.equals(RUTA_NOTIFICACIONES)) {
                List<Notificacion> lista = notificacionCtrl.listarParaUsuario(
                        sesion.identificacion, sesion.correo, sesion.nombreCompleto).getDatos();
                if (lista == null) lista = new ArrayList<>();
                int noLeidas = 0;
                StringBuilder json = new StringBuilder("{\"exito\":true,\"datos\":[");
                for (int i = 0; i < lista.size(); i++) {
                    Notificacion n = lista.get(i);
                    if (!n.isLeida()) noLeidas++;
                    if (i > 0) json.append(",");
                    json.append("{"
                            + "\"idNotificacion\":" + n.getIdNotificacion() + ","
                            + "\"titulo\":\"" + escapar(n.getTitulo()) + "\","
                            + "\"mensaje\":\"" + escapar(n.getMensaje()) + "\","
                            + "\"tipo\":\"" + escapar(n.getTipo()) + "\","
                            + "\"idReferencia\":" + n.getIdReferencia() + ","
                            + "\"fecha\":\"" + escapar(n.getFecha()) + "\","
                            + "\"leida\":" + n.isLeida()
                            + "}");
                }
                json.append("],\"noLeidas\":").append(noLeidas).append("}");
                responder(ex, 200, json.toString());
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.equals(RUTA_NOTIFICACIONES + "/leer")) {
                Map<String, String> datos = leerJson(leerCuerpo(ex));
                int id = 0;
                try {
                    id = Integer.parseInt(datos.getOrDefault("idNotificacion", "0").trim());
                } catch (NumberFormatException ignored) {
                }
                Respuesta<Notificacion> resp = notificacionCtrl.marcarLeida(
                        id, sesion.identificacion, sesion.correo, sesion.nombreCompleto);
                responder(ex, resp.isExito() ? 200 : resp.getCodigo(),
                        "{\"exito\":" + resp.isExito() + ",\"mensaje\":\"" + escapar(resp.getMensaje()) + "\"}");
                return;
            }

            if (metodo.equalsIgnoreCase("PUT") && ruta.equals(RUTA_NOTIFICACIONES + "/leer-todas")) {
                Respuesta<Boolean> resp = notificacionCtrl.marcarTodasLeidas(
                        sesion.identificacion, sesion.correo, sesion.nombreCompleto);
                responder(ex, 200, "{\"exito\":true,\"mensaje\":\"" + escapar(resp.getMensaje()) + "\"}");
                return;
            }

            responderError(ex, 404, "Ruta de notificaciones no encontrada.");
        } catch (Exception e) {
            responderError(ex, 500, "Error interno en notificaciones: " + e.getMessage());
        } finally {
            if (!metodo.equalsIgnoreCase("GET") && !metodo.equalsIgnoreCase("OPTIONS")) {
                persistirTodo();
            }
        }
    }

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
                + "\"empresaId\":\"" + escapar(c.getEmpresaId()) + "\","
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

    private static void responder(HttpExchange ex, int codigo, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);

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

    private static String emitirToken(Usuario u) {
        if (u == null) return "";
        String token = UUID.randomUUID().toString();
        String nombreCompleto = ((u.getNombre() != null ? u.getNombre() : "") + " "
                + (u.getApellido() != null ? u.getApellido() : "")).trim();
        sesionesActivas.put(token, new SesionToken(
                u.getIdentificacion(),
                u.getRol(),
                nombreCompleto,
                u.getCorreo(),
                extraerEmpresaId(u)
        ));
        return token;
    }

    private static String extraerEmpresaId(Usuario u) {
        if (u instanceof Administrador) return ((Administrador) u).getEmpresaId();
        if (u instanceof Empleado) return ((Empleado) u).getEmpresaId();
        if (u instanceof Cliente) return ((Cliente) u).getEmpresaId();
        return "SUPERADMIN".equalsIgnoreCase(u != null ? u.getRol() : "") ? "PLATAFORMA" : "EMP-001";
    }

    private static boolean verificarEmpresaActivaSiAplica(HttpExchange ex, SesionToken sesion) throws IOException {
        if (sesion == null) return true;
        if ("SUPERADMIN".equalsIgnoreCase(sesion.rol) || "CLIENTE".equalsIgnoreCase(sesion.rol)) return true;
        if (!empresaCtrl.estaEmpresaActiva(sesion.empresaId)) {
            responderEmpresaSuspendida(ex, sesion.empresaId);
            return false;
        }
        return true;
    }

    private static void responderEmpresaSuspendida(HttpExchange ex, String empresaId) throws IOException {
        Empresa emp = empresaCtrl.obtenerEmpresaDirecta(empresaId);
        Administrador superAdmin = usuarioCtrl.obtenerSuperAdmin();
        String empNombre = emp != null ? emp.getNombre() : empresaId;
        String motivo = emp != null ? emp.getMotivoSuspension() : "Pago de suscripción del sistema pendiente.";
        String adminNombre = superAdmin != null ? superAdmin.getNombreCompleto() : "Administrador de la Plataforma";
        String adminCorreo = superAdmin != null ? superAdmin.getCorreo() : "superadmin@winetcontrol.com";
        String adminTelefono = superAdmin != null ? superAdmin.getTelefono() : "3009999999";

        String json = "{"
                + "\"exito\":false,"
                + "\"empresaSuspendida\":true,"
                + "\"empresaId\":\"" + escapar(empresaId) + "\","
                + "\"empresaNombre\":\"" + escapar(empNombre) + "\","
                + "\"motivo\":\"" + escapar(motivo) + "\","
                + "\"adminPlataformaNombre\":\"" + escapar(adminNombre) + "\","
                + "\"adminPlataformaCorreo\":\"" + escapar(adminCorreo) + "\","
                + "\"adminPlataformaTelefono\":\"" + escapar(adminTelefono) + "\","
                + "\"mensaje\":\"El servicio de la empresa " + escapar(empNombre) + " está suspendido. Realice su pago del sistema o contacte al administrador de la plataforma.\""
                + "}";
        responder(ex, 403, json);
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
        if (origen == null || sesion == null) return filtrados;
        for (Soporte t : origen) {
            if (t == null) continue;
            boolean porRemitente = t.getRemitenteId() != null && !t.getRemitenteId().isEmpty()
                    && t.getRemitenteId().equalsIgnoreCase(sesion.identificacion);
            boolean porId = t.getUsuario() != null && t.getUsuario().equalsIgnoreCase(sesion.identificacion);
            boolean porNombre = t.getUsuario() != null && t.getUsuario().equalsIgnoreCase(sesion.nombreCompleto);
            boolean porCorreo = t.getCorreo() != null && !sesion.correo.isEmpty()
                    && t.getCorreo().equalsIgnoreCase(sesion.correo);
            if (porRemitente || porId || porNombre || porCorreo) {
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

    private static void sembrarClientesDeEjemplo() {

        Empresa emp1 = new Empresa(
                "EMP-001", "WiNet Norte Telecom S.A.S.", "901234567-1",
                "contacto@winetnorte.com", "3008888888", "Sucursal Local Norte # 45-20",
                "Plan SaaS Empresarial", 350000, true, "1000000001"
        );
        empresaCtrl.registrarEmpresa(emp1);

        Empresa emp2 = new Empresa(
                "EMP-002", "FibraCaribe ISP S.A.S.", "901876543-2",
                "gerencia@fibracaribe.com", "3017776655", "Av. Pedro de Heredia # 30-12",
                "Plan SaaS Pro", 250000, true, "1000000002"
        );
        empresaCtrl.registrarEmpresa(emp2);

        usuarioCtrl.registrarAdministrador(new Administrador(
                "Carlos", "Caribe", "CC", "1000000002",
                "3017776655", "admin.caribe@fibracaribe.com", "Av. Pedro de Heredia # 30-12",
                "Admin123!", "ADMIN", "FibraCaribe ISP S.A.S.", "EMP-002"
        ));

        planCtrl.crearPlan("101", "Plan Básico 50M", "50 Mbps", "45000", "Internet residencial estándar");
        planCtrl.crearPlan("102", "Plan Fibra 100M", "100 Mbps", "65000", "Internet fibra simétrica");
        planCtrl.crearPlan("103", "Plan Fibra Gamer 300M", "300 Mbps", "95000", "Baja latencia para juegos");
        planCtrl.crearPlan("104", "Plan Corporativo 500M", "500 Mbps", "145000", "IP fija incluida");

        usuarioCtrl.registrarCliente("Alejandro", "Escudero", "CC", "1045678901",
                "3001234567", "alejandrocastellar04@gmail.com", "Calle 10 # 20-30", "Clave123", "Clave123", "EMP-001");

        usuarioCtrl.registrarCliente("Maria", "Gomez", "CC", "1098765432",
                "3109876543", "maria.gomez@email.com", "Carrera 15 # 45-10", "Clave456", "Clave456", "EMP-001");

        usuarioCtrl.registrarCliente("Gyanela", "Meza", "CC", "1001900345",
                "3013154001", "gianelameza@gmail.com", "Calle 45 # 12-34", "Clave123", "Clave123", "EMP-001");

        usuarioCtrl.registrarCliente("Luna", "Lopez", "CC", "45517179",
                "3023544779", "lunaloopez@gmail.com", "Carrera 7 # 80-15", "Clave123", "Clave123", "EMP-001");

        usuarioCtrl.asignarPlan("1045678901", "Plan Fibra 100M");
        usuarioCtrl.asignarPlan("1098765432", "Plan Básico 50M");
        usuarioCtrl.asignarPlan("1001900345", "Plan Básico 50M");
        usuarioCtrl.asignarPlan("45517179", "Plan Corporativo 500M");

        usuarioCtrl.cambiarEstadoCliente("1098765432", false);

        empleadoCtrl.registrarEmpleado("Carlos", "Pineda", "CC", "1020304050",
                "3005556677", "carlos.pineda@winetcontrol.com", "Calle 3 # 4-5",
                "Técnico de campo", "WiNet Norte Telecom S.A.S.", "Clave123", "EMP-001");

        empleadoCtrl.registrarEmpleado("Lucia", "Ramirez", "CC", "1030405060",
                "3006667788", "lucia.ramirez@winetcontrol.com", "Carrera 9 # 8-7",
                "Atención al cliente", "WiNet Norte Telecom S.A.S.", "Clave123", "EMP-001");

        empleadoCtrl.registrarEmpleado("Andres", "Torres", "CC", "1040506070",
                "3007778899", "andres.torres@winetcontrol.com", "Avenida 5 # 6-7",
                "Supervisor", "WiNet Norte Telecom S.A.S.", "Clave123", "EMP-001");

        empleadoCtrl.cambiarEstadoEmpleado("1040506070", false);

        empleadoCtrl.registrarEmpleado("Sofia", "Vargas", "CC", "1050607080",
                "3001112233", "sofia.vargas@winetcontrol.com", "Sede Central Plataforma",
                "Soporte Plataforma", "Plataforma Central", "Clave123", "PLATAFORMA");

        empleadoCtrl.registrarEmpleado("Mateo", "Ruiz", "CC", "1060708090",
                "3002223344", "mateo.ruiz@winetcontrol.com", "Sede Central Plataforma",
                "Especialista Nivel 2", "Plataforma Central", "Clave123", "PLATAFORMA");

        soporteCtrl.radicarTicket("Alejandro Escudero", "1045678901", "CLIENTE", "alejandrocastellar04@gmail.com",
                "[Lentitud o Intermitencia] Lentitud en horario nocturno",
                "Presento caídas de velocidad entre las 8:00 p.m. y 10:00 p.m. en el sector norte.",
                "CLIENTE", "EMP-001", false);

        soporteCtrl.radicarTicket("Maria Gomez", "1098765432", "CLIENTE", "maria.gomez@email.com",
                "[Configuración de Router / WiFi] Solicitud de cambio de clave WiFi",
                "Deseo actualizar la contraseña de mi router de fibra óptica por seguridad.",
                "CLIENTE", "EMP-001", false);

        soporteCtrl.radicarTicket("Administrador Local (WiNet Norte)", "1000000001", "ADMIN", "admin@winetcontrol.com",
                "[Error del Sistema] Consulta sobre generación masiva de facturas PDF",
                "Necesitamos habilitar la descarga conjunta de comprobantes del mes para nuestra sucursal.",
                "PLATAFORMA", "EMP-001", false);

        System.out.println("[seed] 2 empresas, 4 planes, 4 clientes, 3 empleados empresa, 2 empleados plataforma y 3 tickets sembrados.");
    }

    private static void asegurarDatosBaseMultiTenant() {
        boolean huboCambios = false;
        List<Empresa> empresas = empresaCtrl.listarEmpresas().getDatos();
        if (empresas == null || empresas.isEmpty()) {
            empresaCtrl.registrarEmpresa(new Empresa(
                    "EMP-001", "WiNet Norte Telecom S.A.S.", "901234567-1",
                    "contacto@winetnorte.com", "3008888888", "Sucursal Local Norte # 45-20",
                    "Plan SaaS Empresarial", 350000, true, "1000000001"
            ));
            empresaCtrl.registrarEmpresa(new Empresa(
                    "EMP-002", "FibraCaribe ISP S.A.S.", "901876543-2",
                    "gerencia@fibracaribe.com", "3017776655", "Av. Pedro de Heredia # 30-12",
                    "Plan SaaS Pro", 250000, true, "1000000002"
            ));
            usuarioCtrl.registrarAdministrador(new Administrador(
                    "Carlos", "Caribe", "CC", "1000000002",
                    "3017776655", "admin.caribe@fibracaribe.com", "Av. Pedro de Heredia # 30-12",
                    "Admin123!", "ADMIN", "FibraCaribe ISP S.A.S.", "EMP-002"
            ));
            huboCambios = true;
        }
        List<Empleado> empPlat = empleadoCtrl.listarEmpleadosPorEmpresa("PLATAFORMA").getDatos();
        if (empPlat == null || empPlat.isEmpty()) {
            empleadoCtrl.registrarEmpleado("Sofia", "Vargas", "CC", "1050607080",
                    "3001112233", "sofia.vargas@winetcontrol.com", "Sede Central Plataforma",
                    "Soporte Plataforma", "Plataforma Central", "Clave123", "PLATAFORMA");
            empleadoCtrl.registrarEmpleado("Mateo", "Ruiz", "CC", "1060708090",
                    "3002223344", "mateo.ruiz@winetcontrol.com", "Sede Central Plataforma",
                    "Especialista Nivel 2", "Plataforma Central", "Clave123", "PLATAFORMA");
            huboCambios = true;
        }
        List<Cliente> clientesActuales = usuarioCtrl.listarClientes().getDatos();
        if (clientesActuales == null || clientesActuales.isEmpty()) {
            usuarioCtrl.registrarCliente("Alejandro", "Escudero", "CC", "1045678901",
                    "3001234567", "alejandrocastellar04@gmail.com", "Calle 10 # 20-30", "Clave123", "Clave123", "EMP-001");
            usuarioCtrl.registrarCliente("Maria", "Gomez", "CC", "1098765432",
                    "3109876543", "maria.gomez@email.com", "Carrera 15 # 45-10", "Clave456", "Clave456", "EMP-001");
            usuarioCtrl.asignarPlan("1045678901", "Plan Fibra 100M");
            usuarioCtrl.asignarPlan("1098765432", "Plan Básico 50M");
            usuarioCtrl.cambiarEstadoCliente("1098765432", false);
            huboCambios = true;
        }
        if (soporteCtrl.listarPendientesFiltrados("PLATAFORMA", "").isEmpty()
                && soporteCtrl.listarEnProcesoFiltrados("PLATAFORMA", "").isEmpty()
                && soporteCtrl.listarCerradosFiltrados("PLATAFORMA", "").isEmpty()) {
            soporteCtrl.radicarTicket("Administrador Local (WiNet Norte)", "1000000001", "ADMIN", "admin@winetcontrol.com",
                    "[Error del Sistema] Consulta sobre generación masiva de facturas PDF",
                    "Necesitamos habilitar la descarga conjunta de comprobantes del mes para nuestra sucursal.",
                    "PLATAFORMA", "EMP-001", false);
            huboCambios = true;
        }
        if (huboCambios) {
            persistirTodo();
        }
    }
}
