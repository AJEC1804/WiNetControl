package entidades;

import utils.FormateadorFecha;

/**
 * Entidad que representa a un cliente suscriptor del servicio de telecomunicaciones.
 * Incluye estado (activo/inactivo) para habilitar o deshabilitar el acceso y los servicios.
 */
public class Cliente extends Usuario {

    private String planActual;
    private String fechaRegistro;
    private boolean activo;

    public Cliente() {
        super();
        this.planActual = "Sin plan";
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
        this.activo = true;
    }

    public Cliente(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena,
                   String planActual) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena);
        this.planActual = (planActual != null && !planActual.trim().isEmpty()) ? planActual.trim() : "Sin plan";
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
        this.activo = true;
    }

    public Cliente(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena,
                   String planActual, String fechaRegistro, boolean activo) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena);
        this.planActual = (planActual != null && !planActual.trim().isEmpty()) ? planActual.trim() : "Sin plan";
        this.fechaRegistro = (fechaRegistro != null && !fechaRegistro.trim().isEmpty()) ? fechaRegistro : FormateadorFecha.fechaActualTexto();
        this.activo = activo;
    }

    public String getPlanActual() {
        return planActual;
    }

    public void setPlanActual(String planActual) {
        this.planActual = (planActual != null && !planActual.trim().isEmpty()) ? planActual.trim() : "Sin plan";
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
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

    @Override
    public String getTipoUsuario() {
        return "Cliente";
    }

    @Override
    public String toString() {
        return super.toString() + " | Plan: " + planActual + " | Estado: " + getEstado() + " | Registrado: " + fechaRegistro;
    }
}
