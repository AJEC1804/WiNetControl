/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Gyanela Meza
 */
public class Planes extends PlanBase {

    private String fechaActivacion;

    public Planes(int idPlan, String nombrePlan, String velocidadPlan, double precio, String descripcionPlan) {
        super(idPlan, nombrePlan, velocidadPlan, precio, descripcionPlan);
        setFechaActivacion("");
    }

    public String getFechaActivacion() {
        return fechaActivacion;
    }

    public final void setFechaActivacion(String fechaActivacion) {
        if (fechaActivacion != null && !fechaActivacion.trim().isEmpty()) {
            this.fechaActivacion = fechaActivacion.trim();
        } else {
            this.fechaActivacion = "Sin fecha";
        }
    }

    @Override
    public String getInfoPlan() {
        return super.getInfoPlan() + " - Activacion: " + fechaActivacion;
    }

    @Override
    public String toString() {
        return idPlan + " " + nombrePlan + " - " + velocidadPlan + " " + precio + " " + descripcionPlan;
    }
}

