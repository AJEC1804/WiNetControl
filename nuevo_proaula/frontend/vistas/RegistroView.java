package vistas;

import controladores.UsuarioController;
import entidades.Cliente;
import utils.Respuesta;

/**
 * Vista de Autorregistro de Clientes - Tarea de Alejandro Escudero (Issue #7).
 * Encargada de conectar los campos del formulario con UsuarioController.registrarCliente(...).
 */
public class RegistroView {

    private final UsuarioController usuarioController;

    public RegistroView() {
        this.usuarioController = new UsuarioController();
    }

    public RegistroView(UsuarioController usuarioController) {
        this.usuarioController = (usuarioController != null) ? usuarioController : new UsuarioController();
    }

    public Respuesta<Cliente> procesarRegistro(String nombre, String apellido, String tipoDocumento,
                                              String identificacion, String telefono, String correo,
                                              String direccion, String contrasena, String confirmarContrasena) {
        return usuarioController.registrarCliente(
                nombre, apellido, tipoDocumento, identificacion,
                telefono, correo, direccion, contrasena, confirmarContrasena
        );
    }
}
