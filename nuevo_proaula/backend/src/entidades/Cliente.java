package entidades;

import utils.FormateadorFecha;

/**
 * Entidad que representa a un cliente suscriptor del servicio de telecomunicaciones.
 * Hereda de Usuario (rol = "CLIENTE") e incluye estado (activo/inactivo),
 * plan suscrito y fecha de registro.
 */
public class Cliente extends Usuario {

    private String planActual;
    private String fechaRegistro;

    public Cliente() {
        super();
        this.rol = "CLIENTE";
        this.planActual = "Sin plan";
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
        this.activo = true;
    }

    public Cliente(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena,
                   String planActual) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, "CLIENTE", true);
        this.planActual = (planActual != null && !planActual.trim().isEmpty()) ? planActual.trim() : "Sin plan";
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
    }

    public Cliente(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena,
                   String planActual, String fechaRegistro, boolean activo) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, "CLIENTE", activo);
        this.planActual = (planActual != null && !planActual.trim().isEmpty()) ? planActual.trim() : "Sin plan";
        this.fechaRegistro = (fechaRegistro != null && !fechaRegistro.trim().isEmpty()) ? fechaRegistro : FormateadorFecha.fechaActualTexto();
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

    @Override
    public String getTipoUsuario() {
        return "Cliente";
    }

    @Override
    public String toString() {
        return super.toString() + " | Plan: " + planActual + " | Registrado: " + fechaRegistro;
    }
}
