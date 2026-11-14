package vistas;

import controladores.UsuarioController;
import entidades.Cliente;
import utils.Respuesta;

/**
 * Plantilla base para la Vista de Inicio de Sesión (Login).
 * Puede extender de JFrame (Swing) o usarse como controlador de vista JavaFX / Consola.
 */
public class LoginView {

    private final UsuarioController usuarioController;

    public LoginView() {
        this.usuarioController = new UsuarioController();
    }

    public Respuesta<Cliente> procesarLogin(String identificacion, String contrasena) {
        return usuarioController.login(identificacion, contrasena);
    }
}
