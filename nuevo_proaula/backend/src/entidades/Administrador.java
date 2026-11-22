package entidades;

import java.io.Serializable;

public class Administrador extends Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sucursal;
    private String nivelAcceso;
    private String empresaId;

    public Administrador() {
        super();
        this.rol = "ADMIN";
        this.sucursal = "Sucursal Principal";
        this.nivelAcceso = "LOCAL";
        this.empresaId = "EMP-001";
    }

    public Administrador(String nombre, String apellido, String tipo, String identificacion,
                         String telefono, String correo, String direccion, String contrasena,
                         String rol, String sucursal) {
        this(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena,
             rol, sucursal, "SUPERADMIN".equalsIgnoreCase(rol) ? "PLATAFORMA" : "EMP-001");
    }

    public Administrador(String nombre, String apellido, String tipo, String identificacion,
                         String telefono, String correo, String direccion, String contrasena,
                         String rol, String sucursal, String empresaId) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, rol, true);
        this.sucursal = (sucursal != null && !sucursal.trim().isEmpty()) ? sucursal.trim() : "Sucursal Principal";
        this.nivelAcceso = "SUPERADMIN".equalsIgnoreCase(rol) ? "GLOBAL" : "LOCAL";
        this.empresaId = (empresaId != null && !empresaId.trim().isEmpty())
                ? empresaId.trim()
                : ("SUPERADMIN".equalsIgnoreCase(rol) ? "PLATAFORMA" : "EMP-001");
    }

    public String getEmpresaId() {
        return (empresaId != null && !empresaId.trim().isEmpty())
                ? empresaId
                : (esSuperAdmin() ? "PLATAFORMA" : "EMP-001");
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = (empresaId != null && !empresaId.trim().isEmpty()) ? empresaId.trim() : "EMP-001";
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
