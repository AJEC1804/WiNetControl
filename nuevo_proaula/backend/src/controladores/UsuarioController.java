package controladores;

import entidades.Administrador;
import entidades.Cliente;
import entidades.Usuario;
import modelos.ClienteModel;
import modelos.FacturaModel;
import modelos.PlanModel;
import utils.EncriptadorUtil;
import utils.Respuesta;
import utils.ValidadorTexto;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de Usuarios, Administradores y Clientes.
 * Actúa como intermediario directo entre la vista y los modelos de negocio.
 * Administra el control de acceso basado en 3 roles:
 * 1. SUPERADMIN: Control global del sistema (todas las sedes y configuraciones).
 * 2. ADMIN: Administrador local de sucursal.
 * 3. CLIENTE: Suscriptor normal de telecomunicaciones.
 */
public class UsuarioController {

    private final ClienteModel clienteModel;
    private final PlanModel planModel;
    private final FacturaModel facturaModel;
    private final List<Administrador> administradoresSistema;

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
        this.administradoresSistema = new ArrayList<>();
        inicializarUsuariosAdministrativos();
    }

    /**
     * Inicializa las cuentas administrativas predeterminadas:
     * - Superusuario Global: acceso total.
     * - Administrador Local: gestión local de sucursal.
     */
    private void inicializarUsuariosAdministrativos() {
        // 1. Superusuario Global
        Administrador superAdmin = new Administrador(
                "Super Administrador", "Global", "CC", "9999999999",
                "3009999999", "superadmin@winetcontrol.com", "Sede Central Corporativa",
                EncriptadorUtil.encriptarSHA256("Super123!"), "SUPERADMIN", "Sede Central (Global)"
        );

        // 2. Administrador Local
        Administrador adminLocal = new Administrador(
                "Administrador", "Local", "CC", "1000000001",
                "3008888888", "admin@winetcontrol.com", "Sucursal Local Norte # 45-20",
                EncriptadorUtil.encriptarSHA256("Admin123!"), "ADMIN", "Sucursal Local Norte"
        );

        administradoresSistema.add(superAdmin);
        administradoresSistema.add(adminLocal);
    }

    public static ClienteModel getSharedClienteModel() { return sharedClienteModel; }
    public static PlanModel getSharedPlanModel() { return sharedPlanModel; }
    public static FacturaModel getSharedFacturaModel() { return sharedFacturaModel; }

    /**
     * Autenticación universal del sistema con soporte para los 3 roles:
     * SUPERADMIN, ADMIN y CLIENTE.
     * Acepta identificación numérica o alias de usuario ("superadmin", "admin123").
     */
    public Respuesta<Usuario> autenticarUsuario(String identificador, String contrasena) {
        if (!ValidadorTexto.esNoVacio(identificador) || !ValidadorTexto.esNoVacio(contrasena)) {
            return Respuesta.error("Debe ingresar identificación/usuario y contraseña.");
        }

        String idLimpio = identificador.trim();

        // 1. Verificar si coincide con cuentas de Superadmin o Admin Local
        for (Administrador admin : administradoresSistema) {
            boolean coincideId = admin.getIdentificacion().equalsIgnoreCase(idLimpio);
            boolean coincideAlias = (admin.esSuperAdmin() && idLimpio.equalsIgnoreCase("superadmin"))
                    || (!admin.esSuperAdmin() && idLimpio.equalsIgnoreCase("admin123"));

            if (coincideId || coincideAlias) {
                if (!admin.isActivo()) {
                    return Respuesta.error("El usuario administrativo se encuentra INACTIVO.", 403);
                }
                if (EncriptadorUtil.verificar(contrasena, admin.getContrasena())) {
                    return Respuesta.exito("Bienvenido " + admin.getTipoUsuario() + " al sistema.", admin);
                } else {
                    return Respuesta.error("Identificación o contraseña incorrectas.", 401);
                }
            }
        }

        // 2. Si no es administrador, verificar en clientes registrados
        Respuesta<Cliente> respCliente = clienteModel.autenticar(idLimpio, contrasena);
        if (respCliente.isExito()) {
            return Respuesta.exito(respCliente.getMensaje(), respCliente.getDatos());
        }

        return Respuesta.error(respCliente.getMensaje(), respCliente.getCodigo());
    }

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
     * Inicio de sesión de clientes (compatibilidad directa con vistas de clientes).
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

    /**
     * Listado de administradores registrados en el sistema.
     */
    public List<Administrador> listarAdministradores() {
        return new ArrayList<>(administradoresSistema);
    }
}
