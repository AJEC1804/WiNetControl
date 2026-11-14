package modelos;

import utils.PersistenciaUtil;

import entidades.Cliente;
import entidades.Plan;
import metodos.ListaClientes;
import utils.EncriptadorUtil;
import utils.FormateadorFecha;
import utils.ValidadorTexto;
import utils.Respuesta;
import java.util.List;

/**
 * Modelo de Negocio para Clientes.
 * Aplica reglas de negocio, validaciones de integridad,
 * encriptación de credenciales y asignación de planes.
 */
public class ClienteModel {

    private ListaClientes listaClientes;

    private static final String ARCHIVO = "clientes.dat";

    public ClienteModel() {
        this.listaClientes = new ListaClientes();
        cargarEstado();
    }

    public ClienteModel(ListaClientes listaClientes) {
        this.listaClientes = listaClientes;
    }

    public ListaClientes getListaClientes() {
        return listaClientes;
    }

    /**
     * Registra un nuevo cliente validando las reglas de negocio.
     */
    public Respuesta<Cliente> registrarCliente(Cliente cliente, String confContrasena, String planPorDefecto) {
        if (cliente == null) {
            return Respuesta.error("Los datos del cliente no pueden ser nulos.");
        }

        // 1. Campos obligatorios
        if (!ValidadorTexto.esNoVacio(cliente.getNombre()) ||
            !ValidadorTexto.esNoVacio(cliente.getApellido()) ||
            !ValidadorTexto.esNoVacio(cliente.getIdentificacion()) ||
            !ValidadorTexto.esNoVacio(cliente.getTelefono()) ||
            !ValidadorTexto.esNoVacio(cliente.getCorreo()) ||
            !ValidadorTexto.esNoVacio(cliente.getDireccion()) ||
            !ValidadorTexto.esNoVacio(cliente.getContrasena())) {
            return Respuesta.error("Todos los campos obligatorios deben ser diligenciados.");
        }

        // 2. Validaciones de formato
        if (!ValidadorTexto.validarIdentificacion(cliente.getIdentificacion())) {
            return Respuesta.error("La identificación debe ser numérica y contener entre 8 y 10 dígitos.");
        }

        if (!ValidadorTexto.validarCorreo(cliente.getCorreo())) {
            return Respuesta.error("El formato del correo electrónico es inválido.");
        }

        if (!ValidadorTexto.validarTelefono(cliente.getTelefono())) {
            return Respuesta.error("El teléfono debe ser numérico y contener 10 dígitos.");
        }

        if (!ValidadorTexto.validarPassword(cliente.getContrasena())) {
            return Respuesta.error("La contraseña debe tener mínimo 6 caracteres e incluir letras y números.");
        }

        if (confContrasena != null && !cliente.getContrasena().equals(confContrasena)) {
            return Respuesta.error("Las contraseñas no coinciden.");
        }

        // 3. Unicidad
        if (listaClientes.existeIdentificacion(cliente.getIdentificacion())) {
            return Respuesta.error("Ya existe un cliente registrado con esta identificación.");
        }

        if (listaClientes.existeCorreo(cliente.getCorreo())) {
            return Respuesta.error("Ya existe un cliente registrado con este correo electrónico.");
        }

        // 4. Asignación de plan inicial y encriptación
        if (!ValidadorTexto.esNoVacio(cliente.getPlanActual()) || cliente.getPlanActual().equalsIgnoreCase("Sin plan")) {
            cliente.setPlanActual(ValidadorTexto.esNoVacio(planPorDefecto) ? planPorDefecto : "");
        }

        cliente.setContrasena(EncriptadorUtil.encriptarContrasena(cliente.getContrasena()));
        listaClientes.insertarFinal(cliente);

        return Respuesta.exito("Cliente registrado exitosamente.", cliente);
    }

    /**
     * Actualiza los datos de un cliente existente respetando la integridad.
     */
    public Respuesta<Cliente> actualizarCliente(String identificacionActual, Cliente datosNuevos) {
        if (!ValidadorTexto.esNoVacio(identificacionActual)) {
            return Respuesta.error("Identificación actual requerida.");
        }

        Cliente actual = listaClientes.buscarPorIdentificacion(identificacionActual);
        if (actual == null) {
            return Respuesta.error("No se encontró ningún cliente con la identificación suministrada.", 404);
        }

        // Validación de nueva identificación si cambió
        if (ValidadorTexto.esNoVacio(datosNuevos.getIdentificacion()) &&
            !datosNuevos.getIdentificacion().equalsIgnoreCase(actual.getIdentificacion())) {
            if (!ValidadorTexto.validarIdentificacion(datosNuevos.getIdentificacion())) {
                return Respuesta.error("La nueva identificación no tiene un formato válido (8 a 10 dígitos).");
            }
            if (listaClientes.existeIdentificacionEnOtro(datosNuevos.getIdentificacion(), actual.getIdentificacion())) {
                return Respuesta.error("La nueva identificación ya está registrada en otro cliente.");
            }
            actual.setIdentificacion(datosNuevos.getIdentificacion());
        }

        // Validación de nuevo correo si cambió
        if (ValidadorTexto.esNoVacio(datosNuevos.getCorreo()) &&
            !datosNuevos.getCorreo().equalsIgnoreCase(actual.getCorreo())) {
            if (!ValidadorTexto.validarCorreo(datosNuevos.getCorreo())) {
                return Respuesta.error("El nuevo correo tiene un formato inválido.");
            }
            if (listaClientes.existeCorreoEnOtro(datosNuevos.getCorreo(), actual.getIdentificacion())) {
                return Respuesta.error("El nuevo correo ya está registrado en otro cliente.");
            }
            actual.setCorreo(datosNuevos.getCorreo());
        }

        // Validación de teléfono si cambió
        if (ValidadorTexto.esNoVacio(datosNuevos.getTelefono())) {
            if (!ValidadorTexto.validarTelefono(datosNuevos.getTelefono())) {
                return Respuesta.error("El nuevo teléfono debe tener 10 dígitos numéricos.");
            }
            actual.setTelefono(datosNuevos.getTelefono());
        }

        if (ValidadorTexto.esNoVacio(datosNuevos.getNombre())) actual.setNombre(datosNuevos.getNombre());
        if (ValidadorTexto.esNoVacio(datosNuevos.getApellido())) actual.setApellido(datosNuevos.getApellido());
        if (ValidadorTexto.esNoVacio(datosNuevos.getTipo())) actual.setTipo(datosNuevos.getTipo());
        if (ValidadorTexto.esNoVacio(datosNuevos.getDireccion())) actual.setDireccion(datosNuevos.getDireccion());
        if (ValidadorTexto.esNoVacio(datosNuevos.getPlanActual())) actual.setPlanActual(datosNuevos.getPlanActual());

        if (ValidadorTexto.esNoVacio(datosNuevos.getContrasena())) {
            if (!ValidadorTexto.validarPassword(datosNuevos.getContrasena())) {
                return Respuesta.error("La nueva contraseña debe tener mínimo 6 caracteres e incluir letras y números.");
            }
            actual.setContrasena(EncriptadorUtil.encriptarContrasena(datosNuevos.getContrasena()));
        }

        return Respuesta.exito("Cliente actualizado correctamente.", actual);
    }

    /**
     * Asigna o cambia el plan de un cliente existente y actualiza la fecha de activación.
     */
    public Respuesta<Cliente> asignarPlan(String identificacionCliente, String nombrePlan,
                                         PlanModel planModel, FacturaModel facturaModel) {
        if (!ValidadorTexto.esNoVacio(identificacionCliente)) {
            return Respuesta.error("Identificación del cliente requerida.");
        }
        if (!ValidadorTexto.esNoVacio(nombrePlan)) {
            return Respuesta.error("Nombre del plan requerido.");
        }

        Cliente cliente = listaClientes.buscarPorIdentificacion(identificacionCliente);
        if (cliente == null) {
            return Respuesta.error("Cliente no encontrado con identificación: " + identificacionCliente, 404);
        }

        if (!cliente.isActivo()) {
            return Respuesta.error("El cliente está INACTIVO. Debe activarlo antes de asignar o modificar su plan.", 400);
        }

        Plan plan = (planModel != null) ? planModel.consultarPlanPorNombre(nombrePlan).getDatos() : null;
        if (plan == null && planModel != null) {
            Integer cod = ValidadorTexto.parseEntero(nombrePlan);
            if (cod != null) {
                plan = planModel.consultarPlanPorId(cod).getDatos();
            }
        }

        if (plan == null) {
            return Respuesta.error("No se encontró el plan con nombre/código: " + nombrePlan, 404);
        }

        if (!plan.isActivo()) {
            return Respuesta.error("El plan '" + plan.getNombrePlan() + "' se encuentra INACTIVO y no puede ser contratado.", 400);
        }

        cliente.setPlanActual(plan.getNombrePlan());
        plan.setFechaActivacion(FormateadorFecha.fechaActualTexto());

        // Emitir factura automática de la suscripción
        if (facturaModel != null) {
            facturaModel.emitirFactura(cliente, plan);
        }

        return Respuesta.exito("Plan '" + plan.getNombrePlan() + "' asignado exitosamente al cliente.", cliente);
    }

    /**
     * Elimina un cliente por su identificación.
     */
    public Respuesta<Boolean> eliminarCliente(String identificacion) {
        if (!ValidadorTexto.esNoVacio(identificacion)) {
            return Respuesta.error("Ingrese una identificación válida para eliminar.");
        }

        boolean eliminado = listaClientes.eliminarPorIdentificacion(identificacion);
        if (eliminado) {
            return Respuesta.exito("Cliente eliminado correctamente del sistema.", true);
        } else {
            return Respuesta.error("No se encontró ningún cliente con esa identificación para eliminar.", 404);
        }
    }

    /**
     * Consulta detallada de un cliente.
     */
    public Respuesta<Cliente> consultarCliente(String identificacion) {
        if (!ValidadorTexto.esNoVacio(identificacion)) {
            return Respuesta.error("Ingrese la identificación a consultar.");
        }

        Cliente c = listaClientes.buscarPorIdentificacion(identificacion);
        if (c != null) {
            return Respuesta.exito("Cliente encontrado.", c);
        } else {
            return Respuesta.error("No se encontró un cliente con la identificación especificada.", 404);
        }
    }

    /**
     * Lista todos los clientes registrados.
     */
    public Respuesta<List<Cliente>> listarClientes() {
        List<Cliente> clientes = listaClientes.listarATodos();
        return Respuesta.exito("Listado de clientes obtenido (" + clientes.size() + ").", clientes);
    }

    /**
     * Activa un cliente en el sistema.
     */
    public Respuesta<Cliente> activarCliente(String identificacion) {
        return cambiarEstadoCliente(identificacion, true);
    }

    /**
     * Desactiva un cliente en el sistema.
     */
    public Respuesta<Cliente> desactivarCliente(String identificacion) {
        return cambiarEstadoCliente(identificacion, false);
    }

    /**
     * Cambia el estado activo/inactivo de un cliente.
     */
    public Respuesta<Cliente> cambiarEstadoCliente(String identificacion, boolean nuevoEstado) {
        if (!ValidadorTexto.esNoVacio(identificacion)) {
            return Respuesta.error("Identificación del cliente requerida.");
        }
        Cliente cliente = listaClientes.buscarPorIdentificacion(identificacion);
        if (cliente == null) {
            return Respuesta.error("Cliente no encontrado con identificación: " + identificacion, 404);
        }
        cliente.setActivo(nuevoEstado);
        String estadoTxt = nuevoEstado ? "ACTIVADO" : "DESACTIVADO";
        return Respuesta.exito("Cliente " + estadoTxt + " correctamente.", cliente);
    }

    /**
     * Lista clientes filtrados por su estado (activos o inactivos).
     */
    public Respuesta<List<Cliente>> listarClientesPorEstado(boolean activo) {
        List<Cliente> filtrados = listaClientes.listarPorEstado(activo);
        String estadoTxt = activo ? "activos" : "inactivos";
        return Respuesta.exito("Clientes " + estadoTxt + ": " + filtrados.size(), filtrados);
    }

    /**
     * Autenticación de cliente por identificación y contraseña.
     */
    public Respuesta<Cliente> autenticar(String identificacion, String contrasenaPlana) {
        if (!ValidadorTexto.esNoVacio(identificacion) || !ValidadorTexto.esNoVacio(contrasenaPlana)) {
            return Respuesta.error("Credenciales incompletas.");
        }

        Cliente c = listaClientes.buscarPorIdentificacion(identificacion);
        if (c == null) {
            return Respuesta.error("Usuario o contraseña incorrectos.", 401);
        }

        if (!c.isActivo()) {
            return Respuesta.error("El cliente se encuentra INACTIVO. Comuníquese con la administración.", 403);
        }

        if (EncriptadorUtil.verificar(contrasenaPlana, c.getContrasena())) {
            return Respuesta.exito("Inicio de sesión exitoso.", c);
        } else {
            return Respuesta.error("Usuario o contraseña incorrectos.", 401);
        }
    }

    // ================= PERSISTENCIA =================
    /** Vuelca la lista de clientes al archivo datos/clientes.dat. */
    public void guardarEstado() {
        PersistenciaUtil.guardar(listaClientes, ARCHIVO);
    }

    /** Recupera la lista desde disco, si existe; si no, se queda la vacía. */
    private void cargarEstado() {
        ListaClientes cargada = PersistenciaUtil.cargar(ARCHIVO, ListaClientes.class);
        if (cargada != null) {
            listaClientes = cargada;
        }
    }

}
