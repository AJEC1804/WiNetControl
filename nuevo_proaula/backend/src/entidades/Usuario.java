package entidades;

import utils.ValidadorTexto;

/**
 * Entidad base para todos los usuarios del sistema WiNet Control.
 * Representa los datos personales y credenciales de acceso.
 */
public class Usuario {

    protected String nombre;
    protected String apellido;
    protected String tipo; // Tipo de documento (CC, TI, CE, etc.)
    protected String identificacion;
    protected String telefono;
    protected String correo;
    protected String direccion;
    protected String contrasena;

    public Usuario() {
    }

    public Usuario(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena) {
        this.nombre = nombre != null ? nombre.trim() : "";
        this.apellido = apellido != null ? apellido.trim() : "";
        this.tipo = tipo != null ? tipo.trim() : "CC";
        this.identificacion = identificacion != null ? identificacion.trim() : "";
        this.telefono = telefono != null ? telefono.trim() : "";
        this.correo = correo != null ? correo.trim().toLowerCase() : "";
        this.direccion = direccion != null ? direccion.trim() : "";
        this.contrasena = contrasena != null ? contrasena.trim() : "";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre != null ? nombre.trim() : "";
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido != null ? apellido.trim() : "";
    }

    public String getNombreCompleto() {
        return (nombre + " " + apellido).trim();
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo != null ? tipo.trim() : "CC";
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion != null ? identificacion.trim() : "";
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono != null ? telefono.trim() : "";
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo != null ? correo.trim().toLowerCase() : "";
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion != null ? direccion.trim() : "";
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena != null ? contrasena.trim() : "";
    }

    public String getTipoUsuario() {
        return "Usuario";
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + tipo + ": " + identificacion + ") - " + correo;
    }
}
