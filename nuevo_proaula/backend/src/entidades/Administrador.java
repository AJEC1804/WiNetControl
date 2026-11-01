package entidades;

/**
 * Entidad que representa a un Administrador del sistema WiNet Control.
 * Admite dos niveles según el rol heredado de Usuario:
 * 1. SUPERADMIN: Superusuario con acceso global a todas las sedes, módulos y auditoría.
 * 2. ADMIN: Administrador local encargado de la gestión de clientes, servicios y soporte de su sucursal.
 */
public class Administrador extends Usuario {

    private String sucursal;
    private String nivelAcceso; // "GLOBAL" o "LOCAL"

    public Administrador() {
        super();
        this.rol = "ADMIN";
        this.sucursal = "Sucursal Principal";
        this.nivelAcceso = "LOCAL";
    }

    public Administrador(String nombre, String apellido, String tipo, String identificacion,
                         String telefono, String correo, String direccion, String contrasena,
                         String rol, String sucursal) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, rol, true);
        this.sucursal = (sucursal != null && !sucursal.trim().isEmpty()) ? sucursal.trim() : "Sucursal Principal";
        this.nivelAcceso = "SUPERADMIN".equalsIgnoreCase(rol) ? "GLOBAL" : "LOCAL";
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = (sucursal != null && !sucursal.trim().isEmpty()) ? sucursal.trim() : "Sucursal Principal";
    }

    public String getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(String nivelAcceso) {
        this.nivelAcceso = nivelAcceso;
    }

    public boolean isAccesoGlobal() {
        return "GLOBAL".equalsIgnoreCase(this.nivelAcceso) || esSuperAdmin();
    }

    @Override
    public String getTipoUsuario() {
        return esSuperAdmin() ? "Superusuario" : "Administrador Local";
    }

    @Override
    public String toString() {
        return super.toString() + " | Sucursal: " + sucursal + " | Acceso: " + nivelAcceso;
    }
}
