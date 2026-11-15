package entidades;

import java.io.Serializable;

import utils.FormateadorFecha;

/**
 * Entidad que representa a un cliente suscriptor del servicio de telecomunicaciones.
 * Hereda de Usuario (rol = "CLIENTE") e incluye estado (activo/inactivo),
 * plan suscrito y fecha de registro.
 */
public class Cliente extends Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String planActual;
    private String fechaRegistro;
    private String fechaInicioPlan;

    public Cliente() {
        super();
        this.rol = "CLIENTE";
        this.planActual = "Sin plan";
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
        this.fechaInicioPlan = "";
        this.activo = true;
    }

    public Cliente(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena,
                   String planActual) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, "CLIENTE", true);
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
        setPlanActual(planActual);
    }

    public Cliente(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena,
                   String planActual, String fechaRegistro, boolean activo) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, "CLIENTE", activo);
        this.fechaRegistro = (fechaRegistro != null && !fechaRegistro.trim().isEmpty()) ? fechaRegistro : FormateadorFecha.fechaActualTexto();
        setPlanActual(planActual);
        if (!"Sin plan".equalsIgnoreCase(this.planActual)) {
            this.fechaInicioPlan = this.fechaRegistro;
        }
    }

    public String getPlanActual() {
        return planActual;
    }

    public void setPlanActual(String planActual) {
        String nuevo = (planActual != null && !planActual.trim().isEmpty()) ? planActual.trim() : "Sin plan";
        if ("Sin plan".equalsIgnoreCase(nuevo)) {
            this.planActual = "Sin plan";
            this.fechaInicioPlan = "";
        } else {
            if (this.planActual == null || !this.planActual.equalsIgnoreCase(nuevo)
                    || this.fechaInicioPlan == null || this.fechaInicioPlan.trim().isEmpty()) {
                this.fechaInicioPlan = FormateadorFecha.fechaActualTexto();
            }
            this.planActual = nuevo;
        }
    }

    public String getFechaInicioPlan() {
        return fechaInicioPlan;
    }

    public void setFechaInicioPlan(String fechaInicioPlan) {
        this.fechaInicioPlan = fechaInicioPlan;
    }

    public String getFechaEfectivaPlan() {
        if (fechaInicioPlan != null && !fechaInicioPlan.trim().isEmpty()) {
            return fechaInicioPlan.trim();
        }
        return (fechaRegistro != null) ? fechaRegistro.trim() : "";
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
