package entidades;

import java.io.Serializable;
import utils.FormateadorFecha;

public class Empresa implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idEmpresa;
    private String nombre;
    private String nit;
    private String correo;
    private String telefono;
    private String direccion;
    private String planPlataforma;
    private double precioMensual;
    private String fechaRegistro;
    private boolean activo;
    private String motivoSuspension;
    private String adminIdentificacion;

    public Empresa() {
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
        this.activo = true;
        this.planPlataforma = "Plan SaaS Empresarial";
        this.precioMensual = 250000;
        this.motivoSuspension = "Pago de suscripción del sistema pendiente.";
    }

    public Empresa(String idEmpresa, String nombre, String nit, String correo,
                   String telefono, String direccion, String planPlataforma,
                   double precioMensual, boolean activo, String adminIdentificacion) {
        this.idEmpresa = idEmpresa != null ? idEmpresa.trim() : "";
        this.nombre = nombre != null ? nombre.trim() : "";
        this.nit = nit != null ? nit.trim() : "";
        this.correo = correo != null ? correo.trim().toLowerCase() : "";
        this.telefono = telefono != null ? telefono.trim() : "";
        this.direccion = direccion != null ? direccion.trim() : "";
        this.planPlataforma = (planPlataforma != null && !planPlataforma.trim().isEmpty())
                ? planPlataforma.trim() : "Plan SaaS Empresarial";
        this.precioMensual = Math.max(0, precioMensual);
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
        this.activo = activo;
        this.motivoSuspension = "Pago de suscripción del sistema pendiente.";
        this.adminIdentificacion = adminIdentificacion != null ? adminIdentificacion.trim() : "";
    }

    public String getIdEmpresa() { return idEmpresa != null ? idEmpresa : ""; }
    public void setIdEmpresa(String idEmpresa) { this.idEmpresa = idEmpresa != null ? idEmpresa.trim() : ""; }

    public String getNombre() { return nombre != null ? nombre : ""; }
    public void setNombre(String nombre) { this.nombre = nombre != null ? nombre.trim() : ""; }

    public String getNit() { return nit != null ? nit : ""; }
    public void setNit(String nit) { this.nit = nit != null ? nit.trim() : ""; }

    public String getCorreo() { return correo != null ? correo : ""; }
    public void setCorreo(String correo) { this.correo = correo != null ? correo.trim().toLowerCase() : ""; }

    public String getTelefono() { return telefono != null ? telefono : ""; }
    public void setTelefono(String telefono) { this.telefono = telefono != null ? telefono.trim() : ""; }

    public String getDireccion() { return direccion != null ? direccion : ""; }
    public void setDireccion(String direccion) { this.direccion = direccion != null ? direccion.trim() : ""; }

    public String getPlanPlataforma() { return planPlataforma != null ? planPlataforma : "Plan SaaS Empresarial"; }
    public void setPlanPlataforma(String planPlataforma) {
        if (planPlataforma != null && !planPlataforma.trim().isEmpty()) {
            this.planPlataforma = planPlataforma.trim();
        }
    }

    public double getPrecioMensual() { return precioMensual; }
    public void setPrecioMensual(double precioMensual) { this.precioMensual = Math.max(0, precioMensual); }

    public String getFechaRegistro() { return fechaRegistro != null ? fechaRegistro : ""; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro != null ? fechaRegistro.trim() : ""; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getEstado() { return activo ? "ACTIVO" : "INACTIVO"; }

    public String getMotivoSuspension() {
        return (motivoSuspension != null && !motivoSuspension.trim().isEmpty())
                ? motivoSuspension
                : "Pago de suscripción del sistema pendiente.";
    }
    public void setMotivoSuspension(String motivoSuspension) {
        this.motivoSuspension = (motivoSuspension != null && !motivoSuspension.trim().isEmpty())
                ? motivoSuspension.trim()
                : "Pago de suscripción del sistema pendiente.";
    }

    public String getAdminIdentificacion() { return adminIdentificacion != null ? adminIdentificacion : ""; }
    public void setAdminIdentificacion(String adminIdentificacion) {
        this.adminIdentificacion = adminIdentificacion != null ? adminIdentificacion.trim() : "";
    }
}
