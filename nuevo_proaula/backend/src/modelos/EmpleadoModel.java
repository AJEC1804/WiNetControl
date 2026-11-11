package modelos;

import entidades.Empleado;
import metodos.ListaEmpleados;
import utils.EncriptadorUtil;
import utils.FormateadorFecha;
import utils.Respuesta;
import utils.ValidadorTexto;

import java.util.List;

/**
 * Modelo de negocio del módulo de Empleados.
 * Aplica las mismas reglas que ClienteModel:
 * - Cédula entre 8 y 10 dígitos, única.
 * - Correo con formato válido y único.
 * - Teléfono de 10 dígitos.
 * - Contraseña de mínimo 6 caracteres, guardada con SHA-256.
 * - No se eliminan empleados: solo se activan o desactivan (historial).
 */
public class EmpleadoModel {

    private final ListaEmpleados listaEmpleados;

    public EmpleadoModel() {
        this.listaEmpleados = new ListaEmpleados();
    }

    public ListaEmpleados getListaEmpleados() {
        return listaEmpleados;
    }

    // ------------------------------------------------------------------
    // REGISTRAR
    // ------------------------------------------------------------------
    public Respuesta<Empleado> registrarEmpleado(Empleado empleado) {
        if (empleado == null) {
            return Respuesta.error("Los datos del empleado no pueden ser nulos.");
        }

        Respuesta<Boolean> validacion = validarDatosBasicos(empleado, true);
        if (!validacion.isExito()) {
            return Respuesta.error(validacion.getMensaje(), validacion.getCodigo());
        }

        if (listaEmpleados.buscarPorIdentificacion(empleado.getIdentificacion()) != null) {
            return Respuesta.error("Ya existe un empleado con la identificación: " + empleado.getIdentificacion(), 400);
        }

        if (listaEmpleados.buscarPorCorreo(empleado.getCorreo()) != null) {
            return Respuesta.error("Ya existe un empleado con el correo: " + empleado.getCorreo(), 400);
        }

        // Contraseña por defecto y hash
        String clave = ValidadorTexto.esNoVacio(empleado.getContrasena())
                ? empleado.getContrasena()
                : "Clave123";

        empleado.setContrasena(EncriptadorUtil.encriptarSHA256(clave));
        if (empleado.getRol() != null && empleado.getRol().equalsIgnoreCase("ADMIN")) {
            empleado.setRol("ADMIN");
        } else {
            empleado.setRol("EMPLEADO");
        }

        if (!ValidadorTexto.esNoVacio(empleado.getFechaIngreso())) {
            empleado.setFechaIngreso(FormateadorFecha.fechaActualTexto());
        }

        listaEmpleados.insertarFinal(empleado);

        return Respuesta.exito("Empleado registrado exitosamente.", empleado);
    }

    // ------------------------------------------------------------------
    // CONSULTAR
    // ------------------------------------------------------------------
    public Respuesta<List<Empleado>> listarEmpleados() {
        List<Empleado> lista = listaEmpleados.listarATodos();
        return Respuesta.exito("Empleados registrados: " + lista.size(), lista);
    }

    public Respuesta<List<Empleado>> listarEmpleadosPorEstado(boolean activo) {
        List<Empleado> lista = listaEmpleados.listarPorEstado(activo);
        String etiqueta = activo ? "activos" : "inactivos";
        return Respuesta.exito("Empleados " + etiqueta + ": " + lista.size(), lista);
    }

    public Respuesta<List<Empleado>> listarEmpleadosPorSucursal(String sucursal) {
        return Respuesta.exito("Empleados de la sucursal " + sucursal,
                listaEmpleados.listarPorSucursal(sucursal));
    }

    public Respuesta<ListaEmpleados> buscarParcial(String fragmento) {
        ListaEmpleados encontrados = listaEmpleados.buscarPorIdentificacionParcial(fragmento);
        return Respuesta.exito("Coincidencias: " + encontrados.getTamano(), encontrados);
    }

    public Respuesta<Empleado> buscarEmpleado(String identificacion) {
        if (!ValidadorTexto.esNoVacio(identificacion)) {
            return Respuesta.error("La identificación es obligatoria.", 400);
        }

        Empleado empleado = listaEmpleados.buscarPorIdentificacion(identificacion.trim());
        if (empleado == null) {
            return Respuesta.error("No se encontró un empleado con la identificación: " + identificacion, 404);
        }
        return Respuesta.exito("Empleado encontrado.", empleado);
    }

    // ------------------------------------------------------------------
    // ACTUALIZAR
    // ------------------------------------------------------------------
    public Respuesta<Empleado> actualizarEmpleado(String identificacionActual, Empleado datosNuevos) {
        if (!ValidadorTexto.esNoVacio(identificacionActual)) {
            return Respuesta.error("La identificación del empleado es obligatoria.", 400);
        }
        if (datosNuevos == null) {
            return Respuesta.error("Los datos nuevos no pueden ser nulos.", 400);
        }

        Empleado existente = listaEmpleados.buscarPorIdentificacion(identificacionActual.trim());
        if (existente == null) {
            return Respuesta.error("No se encontró un empleado con la identificación: " + identificacionActual, 404);
        }

        // La cédula no se modifica: se valida la del empleado que ya existe
        datosNuevos.setIdentificacion(existente.getIdentificacion());

        Respuesta<Boolean> validacion = validarDatosBasicos(datosNuevos, false);
        if (!validacion.isExito()) {
            return Respuesta.error(validacion.getMensaje(), validacion.getCodigo());
        }

        // Correo repetido en OTRO empleado
        Empleado conEseCorreo = listaEmpleados.buscarPorCorreo(datosNuevos.getCorreo());
        if (conEseCorreo != null && !conEseCorreo.getIdentificacion().equals(existente.getIdentificacion())) {
            return Respuesta.error("El correo " + datosNuevos.getCorreo() + " ya lo usa otro empleado.", 400);
        }

        listaEmpleados.actualizarEmpleado(identificacionActual.trim(), datosNuevos);

        return Respuesta.exito("Empleado actualizado correctamente.", existente);
    }

    // ------------------------------------------------------------------
    // ESTADO (no se elimina: se desactiva)
    // ------------------------------------------------------------------
    public Respuesta<Empleado> cambiarEstadoEmpleado(String identificacion, boolean activo) {
        if (!ValidadorTexto.esNoVacio(identificacion)) {
            return Respuesta.error("La identificación es obligatoria.", 400);
        }

        if (!listaEmpleados.cambiarEstado(identificacion.trim(), activo)) {
            return Respuesta.error("No se encontró un empleado con la identificación: " + identificacion, 404);
        }

        Empleado empleado = listaEmpleados.buscarPorIdentificacion(identificacion.trim());
        String mensaje = activo
                ? "Empleado ACTIVADO correctamente."
                : "Empleado DESACTIVADO correctamente.";

        return Respuesta.exito(mensaje, empleado);
    }

    public Respuesta<Empleado> cambiarRolEmpleado(String identificacion, String nuevoRol) {
        if (!ValidadorTexto.esNoVacio(identificacion)) {
            return Respuesta.error("La identificación es obligatoria.", 400);
        }

        String rolFinal = (nuevoRol != null && nuevoRol.trim().equalsIgnoreCase("ADMIN"))
                ? "ADMIN"
                : "EMPLEADO";

        if (!listaEmpleados.cambiarRol(identificacion.trim(), rolFinal)) {
            return Respuesta.error("No se encontró un empleado con la identificación: " + identificacion, 404);
        }

        Empleado empleado = listaEmpleados.buscarPorIdentificacion(identificacion.trim());
        return Respuesta.exito("Rol del empleado actualizado a " + rolFinal + ".", empleado);
    }


    // ------------------------------------------------------------------
    // AUTENTICAR (login del empleado)
    // ------------------------------------------------------------------
    public Respuesta<Empleado> autenticar(String identificacion, String contrasenaPlana) {
        if (!ValidadorTexto.esNoVacio(identificacion) || !ValidadorTexto.esNoVacio(contrasenaPlana)) {
            return Respuesta.error("Credenciales incompletas.", 400);
        }

        Empleado empleado = listaEmpleados.buscarPorIdentificacion(identificacion.trim());

        if (empleado == null) {
            return Respuesta.error("Usuario o contraseña incorrectos.", 401);
        }

        if (!empleado.isActivo()) {
            return Respuesta.error("El empleado se encuentra INACTIVO. Comuníquese con la administración.", 403);
        }

        if (EncriptadorUtil.verificar(contrasenaPlana, empleado.getContrasena())) {
            return Respuesta.exito("Inicio de sesión exitoso.", empleado);
        }

        return Respuesta.error("Usuario o contraseña incorrectos.", 401);
    }


    // ------------------------------------------------------------------
    // ELIMINAR (solo SUPERADMIN). El resto de roles solo desactiva.
    // ------------------------------------------------------------------
    public Respuesta<Boolean> eliminarEmpleado(String identificacion) {
        if (!ValidadorTexto.esNoVacio(identificacion)) {
            return Respuesta.error("La identificación es obligatoria.", 400);
        }

        Empleado empleado = listaEmpleados.buscarPorIdentificacion(identificacion.trim());

        if (empleado == null) {
            return Respuesta.error("No se encontró un empleado con la identificación: " + identificacion, 404);
        }

        listaEmpleados.eliminarEmpleado(identificacion.trim());

        return Respuesta.exito("Empleado eliminado correctamente del sistema.", Boolean.TRUE);
    }

    // ------------------------------------------------------------------
    // VALIDACIONES COMPARTIDAS
    // ------------------------------------------------------------------
    private Respuesta<Boolean> validarDatosBasicos(Empleado empleado, boolean esNuevo) {
        if (!ValidadorTexto.validarIdentificacion(empleado.getIdentificacion())) {
            return Respuesta.error("La identificación debe ser numérica y contener entre 8 y 10 dígitos.", 400);
        }
        if (!ValidadorTexto.esNoVacio(empleado.getNombre())) {
            return Respuesta.error("El nombre es obligatorio.", 400);
        }
        if (!ValidadorTexto.esNoVacio(empleado.getApellido())) {
            return Respuesta.error("El apellido es obligatorio.", 400);
        }
        if (!ValidadorTexto.validarCorreo(empleado.getCorreo())) {
            return Respuesta.error("El formato del correo electrónico es inválido.", 400);
        }
        if (!ValidadorTexto.validarTelefono(empleado.getTelefono())) {
            return Respuesta.error("El teléfono debe tener exactamente 10 dígitos numéricos.", 400);
        }
        if (esNuevo && ValidadorTexto.esNoVacio(empleado.getContrasena())
                && !ValidadorTexto.validarPassword(empleado.getContrasena())) {
            return Respuesta.error("La contraseña debe tener mínimo 6 caracteres.", 400);
        }
        return Respuesta.exito("Datos válidos.", Boolean.TRUE);
    }
}
