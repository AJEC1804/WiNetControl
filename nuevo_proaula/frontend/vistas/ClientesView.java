package vistas;

import controladores.UsuarioController;
import entidades.Cliente;
import utils.Respuesta;
import java.util.List;

/**
 * Plantilla base para la Vista de Gestión CRUD de Clientes.
 */
public class ClientesView {

    private final UsuarioController usuarioController;

    public ClientesView() {
        this.usuarioController = new UsuarioController();
    }

    public Respuesta<Cliente> registrar(String nombre, String apellido, String tipo,
                                        String identificacion, String telefono, String correo,
                                        String direccion, String clave, String confClave) {
        return usuarioController.registrarCliente(nombre, apellido, tipo, identificacion, telefono, correo, direccion, clave, confClave);
    }

    public Respuesta<Cliente> buscar(String identificacion) {
        return usuarioController.buscarCliente(identificacion);
    }

    public Respuesta<Cliente> actualizar(String idActual, String nombre, String apellido, String tipo,
                                         String nuevaId, String telefono, String correo, String direccion,
                                         String nuevaClave, String nuevoPlan) {
        return usuarioController.actualizarCliente(idActual, nombre, apellido, tipo, nuevaId, telefono, correo, direccion, nuevaClave, nuevoPlan);
    }

    public Respuesta<Boolean> eliminar(String identificacion) {
        return usuarioController.eliminarCliente(identificacion);
    }

    public Respuesta<List<Cliente>> obtenerListado() {
        return usuarioController.listarClientes();
    }
}
