package entidades;

import java.io.Serializable;

/**
 * Entidad que representa un plan de conectividad o internet ofrecido por la empresa.
 * Incluye estado (activo/inactivo) para permitir o suspender su contratación comercial.
 */
public class Plan implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idPlan;
    private String nombrePlan;
    private String velocidadPlan;
    private double precio;
    private String descripcionPlan;
    private String fechaActivacion;
    private boolean activo;

    public Plan() {
        this.activo = true;
    }

    public Plan(int idPlan, String nombrePlan, String velocidadPlan, double precio, String descripcionPlan) {
        this.idPlan = idPlan;
        this.nombrePlan = nombrePlan != null ? nombrePlan.trim() : "";
        this.velocidadPlan = velocidadPlan != null ? velocidadPlan.trim() : "0 Mbps";
        this.precio = Math.max(0, precio);
        this.descripcionPlan = descripcionPlan != null ? descripcionPlan.trim() : "";
        this.fechaActivacion = "";
        this.activo = true;
    }

    public Plan(int idPlan, String nombrePlan, String velocidadPlan, double precio, String descripcionPlan, String fechaActivacion) {
        this(idPlan, nombrePlan, velocidadPlan, precio, descripcionPlan);
        this.fechaActivacion = fechaActivacion != null ? fechaActivacion.trim() : "";
    }

    public Plan(int idPlan, String nombrePlan, String velocidadPlan, double precio, String descripcionPlan, String fechaActivacion, boolean activo) {
        this(idPlan, nombrePlan, velocidadPlan, precio, descripcionPlan, fechaActivacion);
        this.activo = activo;
    }

    public int getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(int idPlan) {
        this.idPlan = idPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan != null ? nombrePlan.trim() : "";
    }

    public String getVelocidadPlan() {
        return velocidadPlan;
    }

    public void setVelocidadPlan(String velocidadPlan) {
        this.velocidadPlan = velocidadPlan != null ? velocidadPlan.trim() : "0 Mbps";
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = Math.max(0, precio);
    }

    public String getDescripcionPlan() {
        return descripcionPlan;
    }

    public void setDescripcionPlan(String descripcionPlan) {
        this.descripcionPlan = descripcionPlan != null ? descripcionPlan.trim() : "";
    }

    public String getFechaActivacion() {
        return fechaActivacion;
    }

    public void setFechaActivacion(String fechaActivacion) {
        this.fechaActivacion = fechaActivacion != null ? fechaActivacion.trim() : "";
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
    public String toString() {
        return idPlan + " - " + nombrePlan + " (" + velocidadPlan + ") - $" + String.format("%,.0f", precio) + " [" + getEstado() + "]";
    }
}
