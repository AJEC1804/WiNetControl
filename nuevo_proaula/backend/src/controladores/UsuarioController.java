package controladores;

import entidades.Cliente;
import modelos.ClienteModel;
import modelos.FacturaModel;
import modelos.PlanModel;
import utils.Respuesta;
import java.util.List;

/**
 * Controlador de Usuarios y Clientes.
 * Actúa como intermediario directo entre la vista y el modelo de negocio.
 */
public class UsuarioController {

    private final ClienteModel clienteModel;
    private final PlanModel planModel;
    private final FacturaModel facturaModel;

    private static ClienteModel sharedClienteModel = new ClienteModel();
    private static PlanModel sharedPlanModel = new PlanModel();
    private static FacturaModel sharedFacturaModel = new FacturaModel();

    public UsuarioController() {
        this(sharedClienteModel, sharedPlanModel, sharedFacturaModel);
    }

    public UsuarioController(ClienteModel clienteModel, PlanModel planModel, FacturaModel facturaModel) {
        this.clienteModel = (clienteModel != null) ? clienteModel : sharedClienteModel;
        this.planModel = (planModel != null) ? planModel : sharedPlanModel;
        this.facturaModel = (facturaModel != null) ? facturaModel : sharedFacturaModel;
    }

    public static ClienteModel getSharedClienteModel() { return sharedClienteModel; }
    public static PlanModel getSharedPlanModel() { return sharedPlanModel; }
    public static FacturaModel getSharedFacturaModel() { return sharedFacturaModel; }

    /**
     * Registro de nuevo cliente con parámetros descriptivos.
     */
    public Respuesta<Cliente> registrarCliente(String nombre, String apellido, String tipoDocumento,
                                              String identificacion, String telefono, String correo,
                                              String direccion, String contrasena, String confirmarContrasena) {

        String planInicial = planModel.obtenerPlanPorDefecto().getNombrePlan();

        Cliente nuevoCliente = new Cliente(
                nombre, apellido, tipoDocumento, identificacion,
                telefono, correo, direccion, contrasena,
                planInicial
        );

        return clienteModel.registrarCliente(nuevoCliente, confirmarContrasena, planInicial);
    }

    /**
     * Inicio de sesión / autenticación.
     */
    public Respuesta<Cliente> login(String identificacion, String contrasena) {
        return clienteModel.autenticar(identificacion, contrasena);
    }

    /**
     * Búsqueda de cliente por identificación.
     */
    public Respuesta<Cliente> buscarCliente(String identificacion) {
        return clienteModel.consultarCliente(identificacion);
    }

    /**
     * Actualización rápida pasando el objeto Cliente directamente.
     */
    public Respuesta<Cliente> actualizarCliente(Cliente clienteModificado) {
        if (clienteModificado == null) {
            return Respuesta.error("Cliente no válido.");
        }
        return clienteModel.actualizarCliente(clienteModificado.getIdentificacion(), clienteModificado);
    }

    /**
     * Actualización con campos individuales.
     */
    public Respuesta<Cliente> actualizarCliente(String identificacionActual, String nombre,
                                                String apellido, String tipoDocumento,
                                                String telefono, String correo,
                                                String direccion, String contrasena, String plan) {

        Cliente datos = new Cliente();
        datos.setNombre(nombre);
        datos.setApellido(apellido);
        datos.setTipo(tipoDocumento);
        datos.setTelefono(telefono);
        datos.setCorreo(correo);
        datos.setDireccion(direccion);
        datos.setContrasena(contrasena);
        datos.setPlanActual(plan);

        return clienteModel.actualizarCliente(identificacionActual, datos);
    }

    /**
     * Asignar o cambiar de plan a un cliente.
     */
    public Respuesta<Cliente> asignarPlan(String identificacionCliente, String nombrePlan) {
        return clienteModel.asignarPlan(identificacionCliente, nombrePlan, planModel, facturaModel);
    }

    /**
     * Eliminación de cliente por identificación.
     */
    public Respuesta<Boolean> eliminarCliente(String identificacion) {
        return clienteModel.eliminarCliente(identificacion);
    }

    /**
     * Activar un cliente.
     */
    public Respuesta<Cliente> activarCliente(String identificacion) {
        return clienteModel.activarCliente(identificacion);
    }

    /**
     * Desactivar un cliente.
     */
    public Respuesta<Cliente> desactivarCliente(String identificacion) {
        return clienteModel.desactivarCliente(identificacion);
    }

    /**
     * Cambiar estado activo/inactivo de un cliente.
     */
    public Respuesta<Cliente> cambiarEstadoCliente(String identificacion, boolean activo) {
        return clienteModel.cambiarEstadoCliente(identificacion, activo);
    }

    /**
     * Listar clientes según estado (activos o inactivos).
     */
    public Respuesta<List<Cliente>> listarClientesPorEstado(boolean activo) {
        return clienteModel.listarClientesPorEstado(activo);
    }

    /**
     * Listado general de clientes.
     */
    public Respuesta<List<Cliente>> listarClientes() {
        return clienteModel.listarClientes();
    }
}
