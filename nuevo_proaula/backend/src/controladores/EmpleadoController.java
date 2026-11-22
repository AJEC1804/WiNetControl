package controladores;

import entidades.Empleado;
import modelos.EmpleadoModel;
import utils.Respuesta;

import java.util.List;

public class EmpleadoController {

    private final EmpleadoModel empleadoModel;

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

    public Respuesta<Empleado> registrarEmpleado(String nombre, String apellido, String tipo,
                                                 String identificacion, String telefono,
                                                 String correo, String direccion,
                                                 String cargo, String sucursal, String contrasena) {
        return registrarEmpleado(nombre, apellido, tipo, identificacion, telefono, correo,
                direccion, cargo, sucursal, contrasena, "EMP-001");
    }

    public Respuesta<Empleado> registrarEmpleado(String nombre, String apellido, String tipo,
                                                 String identificacion, String telefono,
                                                 String correo, String direccion,
                                                 String cargo, String sucursal, String contrasena,
                                                 String empresaId) {

        Empleado nuevo = new Empleado(
                nombre, apellido, tipo, identificacion,
                telefono, correo, direccion, contrasena,
                cargo, sucursal, "", empresaId
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

    public Respuesta<List<Empleado>> listarEmpleadosPorEmpresa(String empresaId) {
        return empleadoModel.listarEmpleadosPorEmpresa(empresaId);
    }

    public Respuesta<List<Empleado>> listarEmpleadosPorEmpresaYEstado(String empresaId, boolean activo) {
        return empleadoModel.listarEmpleadosPorEmpresaYEstado(empresaId, activo);
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

    public Respuesta<Boolean> eliminarEmpleado(String identificacion) {
        return empleadoModel.eliminarEmpleado(identificacion);
    }

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
