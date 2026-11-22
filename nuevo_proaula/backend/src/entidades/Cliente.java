package entidades;

import java.io.Serializable;

import utils.FormateadorFecha;

public class Cliente extends Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String planActual;
    private String fechaRegistro;
    private String fechaInicioPlan;
    private String empresaId;

    public Cliente() {
        super();
        this.rol = "CLIENTE";
        this.planActual = "Sin plan";
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
        this.fechaInicioPlan = "";
        this.activo = true;
        this.empresaId = "EMP-001";
    }

    public Cliente(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena,
                   String planActual) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, "CLIENTE", true);
        this.fechaRegistro = FormateadorFecha.fechaActualTexto();
        this.empresaId = "EMP-001";
        asignarPlanInterno(planActual);
    }

    public Cliente(String nombre, String apellido, String tipo, String identificacion,
                   String telefono, String correo, String direccion, String contrasena,
                   String planActual, String fechaRegistro, boolean activo) {
        super(nombre, apellido, tipo, identificacion, telefono, correo, direccion, contrasena, "CLIENTE", activo);
        this.fechaRegistro = (fechaRegistro != null && !fechaRegistro.trim().isEmpty()) ? fechaRegistro : FormateadorFecha.fechaActualTexto();
        this.empresaId = "EMP-001";
        asignarPlanInterno(planActual);
        if (!"Sin plan".equalsIgnoreCase(this.planActual)) {
            this.fechaInicioPlan = this.fechaRegistro;
        }
    }

    public String getEmpresaId() {
        return (empresaId != null && !empresaId.trim().isEmpty()) ? empresaId : "EMP-001";
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = (empresaId != null && !empresaId.trim().isEmpty()) ? empresaId.trim() : "EMP-001";
    }

    public String getPlanActual() {
        return planActual;
    }

    public void setPlanActual(String planActual) {
        asignarPlanInterno(planActual);
    }

    private void asignarPlanInterno(String planActual) {
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
