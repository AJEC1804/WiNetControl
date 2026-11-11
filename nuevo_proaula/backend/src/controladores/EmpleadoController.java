package controladores;

import entidades.Empleado;
import modelos.EmpleadoModel;
import utils.Respuesta;

import java.util.List;

/**
 * Controlador del módulo de Empleados (Issue #13).
 * Es el intermediario entre la vista (página web / ApiServer) y el modelo,
 * exactamente igual que UsuarioController con los clientes.
 *
 * Los métodos de este controlador son los que llama el puente HTTP.
 */
public class EmpleadoController {

    private final EmpleadoModel empleadoModel;

    // Modelo compartido: así todos los controladores ven los mismos empleados
    private static final EmpleadoModel sharedEmpleadoModel = new EmpleadoModel();

    public EmpleadoController() {
        this(sharedEmpleadoModel);
    }

    public EmpleadoController(EmpleadoModel empleadoModel) {
        this.empleadoModel = (empleadoModel != null) ? empleadoModel : sharedEmpleadoModel;
    }

    public static EmpleadoModel getSharedEmpleadoModel() {
        return sharedEmpleadoModel;
    }

    /**
     * Registro con parámetros individuales (es lo que usa el formulario).
     */
    public Respuesta<Empleado> registrarEmpleado(String nombre, String apellido, String tipo,
                                                 String identificacion, String telefono,
                                                 String correo, String direccion,
                                                 String cargo, String sucursal, String contrasena) {

        Empleado nuevo = new Empleado(
                nombre, apellido, tipo, identificacion,
                telefono, correo, direccion, contrasena,
                cargo, sucursal, ""
        );

        return empleadoModel.registrarEmpleado(nuevo);
    }

    public Respuesta<Empleado> registrarEmpleado(Empleado empleado) {
        return empleadoModel.registrarEmpleado(empleado);
    }

    public Respuesta<List<Empleado>> listarEmpleados() {
        return empleadoModel.listarEmpleados();
    }

    public Respuesta<List<Empleado>> listarEmpleadosPorEstado(boolean activo) {
        return empleadoModel.listarEmpleadosPorEstado(activo);
    }

    public Respuesta<List<Empleado>> listarEmpleadosPorSucursal(String sucursal) {
        return empleadoModel.listarEmpleadosPorSucursal(sucursal);
    }

    public Respuesta<metodos.ListaEmpleados> buscarEmpleados(String fragmento) {
        return empleadoModel.buscarParcial(fragmento);
    }

    public Respuesta<Empleado> buscarEmpleado(String identificacion) {
        return empleadoModel.buscarEmpleado(identificacion);
    }

    public Respuesta<Empleado> actualizarEmpleado(String identificacionActual, Empleado datosNuevos) {
        return empleadoModel.actualizarEmpleado(identificacionActual, datosNuevos);
    }

    public Respuesta<Empleado> actualizarEmpleado(String identificacionActual, String nombre,
                                                  String apellido, String tipo, String telefono,
                                                  String correo, String direccion,
                                                  String cargo, String sucursal) {

        Empleado datos = new Empleado();
        datos.setNombre(nombre);
        datos.setApellido(apellido);
        datos.setTipo(tipo);
        datos.setTelefono(telefono);
        datos.setCorreo(correo);
        datos.setDireccion(direccion);
        datos.setCargo(cargo);
        datos.setSucursal(sucursal);

        return empleadoModel.actualizarEmpleado(identificacionActual, datos);
    }

    /** Elimina el registro del empleado (solo para SUPERADMIN). */
    public Respuesta<Boolean> eliminarEmpleado(String identificacion) {
        return empleadoModel.eliminarEmpleado(identificacion);
    }

    /** Login del empleado: valida cédula + contraseña (SHA-256) y que esté activo. */
    public Respuesta<Empleado> autenticar(String identificacion, String contrasena) {
        return empleadoModel.autenticar(identificacion, contrasena);
    }

    public Respuesta<Empleado> cambiarEstadoEmpleado(String identificacion, boolean activo) {
        return empleadoModel.cambiarEstadoEmpleado(identificacion, activo);
    }

    public Respuesta<Empleado> cambiarRolEmpleado(String identificacion, String nuevoRol) {
        return empleadoModel.cambiarRolEmpleado(identificacion, nuevoRol);
    }
}
