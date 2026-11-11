package entidades;

import utils.ValidadorTexto;

/**
 * Entidad base para todos los usuarios del sistema WiNet Control.
 * Representa los datos personales, credenciales de acceso, estado y rol del usuario.
 * Roles soportados:
 * - SUPERADMIN: Acceso total al sistema y administración global.
 * - ADMIN: Administrador local de sucursal.
 * - CLIENTE: Suscriptor de servicios de telecomunicaciones.
 */
public class Usuario {

    protected String nombre;
    protected String apellido;
    protected String tipo; // Tipo de documento (CC, TI, CE, NIT, etc.)
    protected String identificacion;
    protected String telefono;
    protected String correo;
    protected String direccion;
    protected String contrasena;
    protected String rol; // SUPERADMIN, ADMIN, CLIENTE
    protected boolean activo;

    public Usuario() {
        this.tipo = "CC";
        this.rol = "CLIENTE";
        this.activo = true;
    }

    public Usuario(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena) {
        this(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, "CLIENTE", true);
    }

    public Usuario(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena, String rol) {
        this(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, rol, true);
    }

    public Usuario(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena,
                   String rol, boolean activo) {
        this.nombre = nombre != null ? nombre.trim() : "";
        this.apellido = apellido != null ? apellido.trim() : "";
        this.tipo = tipo != null ? tipo.trim() : "CC";
        this.identificacion = identificacion != null ? identificacion.trim() : "";
        this.telefono = telefono != null ? telefono.trim() : "";
        this.correo = correo != null ? correo.trim().toLowerCase() : "";
        this.direccion = direccion != null ? direccion.trim() : "";
        this.contrasena = contrasena != null ? contrasena.trim() : "";
        this.rol = (rol != null && !rol.trim().isEmpty()) ? rol.trim().toUpperCase() : "CLIENTE";
        this.activo = activo;
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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = (rol != null && !rol.trim().isEmpty()) ? rol.trim().toUpperCase() : "CLIENTE";
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getEstado() {
        return activo ? "ACTIVO" : "INACTIVO";
    }

    public void setEstado(String estado) {
        if (estado != null) {
            this.activo = estado.equalsIgnoreCase("ACTIVO") || estado.equalsIgnoreCase("TRUE");
        }
    }

    public boolean esSuperAdmin() {
        return "SUPERADMIN".equalsIgnoreCase(rol);
    }

    public boolean esAdmin() {
        return "ADMIN".equalsIgnoreCase(rol) || esSuperAdmin();
    }

    public boolean esEmpleado() {
        return "EMPLEADO".equalsIgnoreCase(this.rol);
    }

    public boolean esCliente() {
        return "CLIENTE".equalsIgnoreCase(rol);
    }

    public String getTipoUsuario() {
        if (esSuperAdmin()) return "Superusuario";
        if ("ADMIN".equalsIgnoreCase(rol)) return "Administrador Local";
        return "Cliente";
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " (" + tipo + ": " + identificacion + ") - " + correo + " [Rol: " + rol + " | Estado: " + getEstado() + "]";
    }
}
