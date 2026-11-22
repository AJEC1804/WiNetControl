package controladores;

import entidades.Administrador;
import entidades.Cliente;
import entidades.Usuario;
import modelos.ClienteModel;
import modelos.FacturaModel;
import modelos.PlanModel;
import utils.EncriptadorUtil;
import utils.PersistenciaUtil;
import utils.Respuesta;
import utils.ValidadorTexto;

import java.util.ArrayList;
import java.util.List;

public class UsuarioController {

    private final ClienteModel clienteModel;
    private final PlanModel planModel;
    private final FacturaModel facturaModel;
    private List<Administrador> administradoresSistema;

    private static final String ARCHIVO_ADMINS = "administradores.dat";
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
        cargarAdministradores();
        if (this.administradoresSistema.isEmpty()) {
            inicializarUsuariosAdministrativos();
        }
    }

    private void inicializarUsuariosAdministrativos() {
        Administrador superAdmin = new Administrador(
                "Super Administrador", "Global", "CC", "9999999999",
                "3009999999", "superadmin@winetcontrol.com", "Sede Central Corporativa",
                EncriptadorUtil.encriptarContrasena("Super123!"), "SUPERADMIN", "Plataforma Central", "PLATAFORMA"
        );

        Administrador adminLocal = new Administrador(
                "Administrador", "Local", "CC", "1000000001",
                "3008888888", "admin@winetcontrol.com", "Sucursal Local Norte # 45-20",
                EncriptadorUtil.encriptarContrasena("Admin123!"), "ADMIN", "WiNet Norte Telecom S.A.S.", "EMP-001"
        );

        administradoresSistema.add(superAdmin);
        administradoresSistema.add(adminLocal);
    }

    @SuppressWarnings("unchecked")
    private void cargarAdministradores() {
        List<Administrador> cargados = PersistenciaUtil.cargar(ARCHIVO_ADMINS, List.class);
        if (cargados != null && !cargados.isEmpty()) {
            this.administradoresSistema = new ArrayList<>(cargados);
        }
    }

    public void guardarAdministradores() {
        PersistenciaUtil.guardar(new ArrayList<>(administradoresSistema), ARCHIVO_ADMINS);
    }

    public static ClienteModel getSharedClienteModel() { return sharedClienteModel; }
    public static PlanModel getSharedPlanModel() { return sharedPlanModel; }
    public static FacturaModel getSharedFacturaModel() { return sharedFacturaModel; }

    public Respuesta<Usuario> autenticarUsuario(String identificador, String contrasena) {
        if (!ValidadorTexto.esNoVacio(identificador) || !ValidadorTexto.esNoVacio(contrasena)) {
            return Respuesta.error("Debe ingresar identificación/usuario y contraseña.");
        }

        String idLimpio = identificador.trim();

        for (Administrador admin : administradoresSistema) {
            boolean coincideId = admin.getIdentificacion().equalsIgnoreCase(idLimpio);
            boolean coincideCorreo = admin.getCorreo() != null && !admin.getCorreo().isEmpty()
                    && admin.getCorreo().equalsIgnoreCase(idLimpio);
            boolean coincideAlias = (admin.esSuperAdmin() && idLimpio.equalsIgnoreCase("superadmin"))
                    || ("1000000001".equals(admin.getIdentificacion()) && idLimpio.equalsIgnoreCase("admin123"));

            if (coincideId || coincideCorreo || coincideAlias) {
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

        Respuesta<Cliente> respCliente = clienteModel.autenticar(idLimpio, contrasena);
        if (respCliente.isExito()) {
            return Respuesta.exito(respCliente.getMensaje(), respCliente.getDatos());
        }

        return Respuesta.error(respCliente.getMensaje(), respCliente.getCodigo());
    }

    public Respuesta<Administrador> registrarAdministrador(Administrador nuevoAdmin) {
        if (nuevoAdmin == null || !ValidadorTexto.validarIdentificacion(nuevoAdmin.getIdentificacion())) {
            return Respuesta.error("La identificación del administrador debe tener entre 8 y 10 dígitos.", 400);
        }
        if (!ValidadorTexto.esNoVacio(nuevoAdmin.getNombre()) || !ValidadorTexto.esNoVacio(nuevoAdmin.getApellido())) {
            return Respuesta.error("El nombre y apellido del administrador son obligatorios.", 400);
        }
        if (!ValidadorTexto.validarCorreo(nuevoAdmin.getCorreo())) {
            return Respuesta.error("El correo del administrador no es válido.", 400);
        }
        for (Administrador a : administradoresSistema) {
            if (a.getIdentificacion().equalsIgnoreCase(nuevoAdmin.getIdentificacion())) {
                return Respuesta.error("Ya existe un administrador con identificación " + nuevoAdmin.getIdentificacion(), 400);
            }
            if (a.getCorreo().equalsIgnoreCase(nuevoAdmin.getCorreo())) {
                return Respuesta.error("Ya existe un administrador con el correo " + nuevoAdmin.getCorreo(), 400);
            }
        }
        String clave = ValidadorTexto.esNoVacio(nuevoAdmin.getContrasena()) ? nuevoAdmin.getContrasena() : "Admin123!";
        if (!ValidadorTexto.validarPassword(clave)) {
            return Respuesta.error("La contraseña del administrador debe tener mínimo 6 caracteres con letras y números.", 400);
        }
        nuevoAdmin.setContrasena(EncriptadorUtil.encriptarContrasena(clave));
        nuevoAdmin.setRol("ADMIN");
        nuevoAdmin.setActivo(true);
        administradoresSistema.add(nuevoAdmin);
        return Respuesta.exito("Administrador registrado exitosamente.", nuevoAdmin);
    }

    public Administrador buscarAdministradorPorEmpresa(String empresaId) {
        if (!ValidadorTexto.esNoVacio(empresaId)) return null;
        for (Administrador a : administradoresSistema) {
            if (a != null && !a.esSuperAdmin() && empresaId.equalsIgnoreCase(a.getEmpresaId())) {
                return a;
            }
        }
        return null;
    }

    public Administrador obtenerSuperAdmin() {
        for (Administrador a : administradoresSistema) {
            if (a != null && a.esSuperAdmin()) return a;
        }
        return null;
    }

    public void actualizarEmpresaEnAdmins(String empresaId, String nuevoNombreEmpresa) {
        if (!ValidadorTexto.esNoVacio(empresaId) || !ValidadorTexto.esNoVacio(nuevoNombreEmpresa)) return;
        for (Administrador a : administradoresSistema) {
            if (a != null && !a.esSuperAdmin() && empresaId.equalsIgnoreCase(a.getEmpresaId())) {
                a.setSucursal(nuevoNombreEmpresa);
            }
        }
    }

    public Respuesta<Cliente> registrarCliente(String nombre, String apellido, String tipoDocumento,
                                              String identificacion, String telefono, String correo,
                                              String direccion, String contrasena, String confirmarContrasena) {
        return registrarCliente(nombre, apellido, tipoDocumento, identificacion, telefono, correo,
                direccion, contrasena, confirmarContrasena, "EMP-001");
    }

    public Respuesta<Cliente> registrarCliente(String nombre, String apellido, String tipoDocumento,
                                              String identificacion, String telefono, String correo,
                                              String direccion, String contrasena, String confirmarContrasena,
                                              String empresaId) {

        String planInicial = "";

        Cliente nuevoCliente = new Cliente(
                nombre, apellido, tipoDocumento, identificacion,
                telefono, correo, direccion, contrasena,
                planInicial
        );
        nuevoCliente.setEmpresaId(empresaId);

        return clienteModel.registrarCliente(nuevoCliente, confirmarContrasena, planInicial);
    }

    public Respuesta<List<Cliente>> listarClientesPorEmpresa(String empresaId) {
        List<Cliente> todos = clienteModel.listarClientes().getDatos();
        List<Cliente> filtrados = new ArrayList<>();
        if (todos != null) {
            if (!ValidadorTexto.esNoVacio(empresaId)) {
                filtrados.addAll(todos);
            } else {
                for (Cliente c : todos) {
                    if (c != null && empresaId.trim().equalsIgnoreCase(c.getEmpresaId())) {
                        filtrados.add(c);
                    }
                }
            }
        }
        return Respuesta.exito("Clientes de la empresa: " + filtrados.size(), filtrados);
    }

    public Respuesta<Cliente> login(String identificacion, String contrasena) {
        return clienteModel.autenticar(identificacion, contrasena);
    }

    public Respuesta<Cliente> buscarCliente(String identificacion) {
        return clienteModel.consultarCliente(identificacion);
    }

    public Respuesta<Cliente> actualizarCliente(Cliente clienteModificado) {
        if (clienteModificado == null) {
            return Respuesta.error("Cliente no válido.");
        }
        return clienteModel.actualizarCliente(clienteModificado.getIdentificacion(), clienteModificado);
    }

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
        if (plan != null) {
            datos.setPlanActual(plan);
        }

        return clienteModel.actualizarCliente(identificacionActual, datos, plan != null);
    }

    public Respuesta<Cliente> asignarPlan(String identificacionCliente, String nombrePlan) {
        return clienteModel.asignarPlan(identificacionCliente, nombrePlan, planModel, facturaModel);
    }

    public Respuesta<Boolean> eliminarCliente(String identificacion) {
        return clienteModel.eliminarCliente(identificacion);
    }

    public Respuesta<Cliente> activarCliente(String identificacion) {
        return clienteModel.activarCliente(identificacion);
    }

    public Respuesta<Cliente> desactivarCliente(String identificacion) {
        return clienteModel.desactivarCliente(identificacion);
    }

    public Respuesta<Cliente> cambiarEstadoCliente(String identificacion, boolean activo) {
        return clienteModel.cambiarEstadoCliente(identificacion, activo);
    }

    public Respuesta<List<Cliente>> listarClientesPorEstado(boolean activo) {
        return clienteModel.listarClientesPorEstado(activo);
    }

    public Respuesta<List<Cliente>> listarClientes() {
        return clienteModel.listarClientes();
    }

    public List<Administrador> listarAdministradores() {
        return new ArrayList<>(administradoresSistema);
    }
}
