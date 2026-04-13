/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Gyanela Meza
 */
public class Planes {

    private int idPlan;
    private String nombrePlan;
    private String velocidadPlan;
    private String descripcionPlan;
    private double precio;
    private String fechaActivacion;

    public Planes(int idPlan, String nombrePlan, String velocidadPlan, double precio, String descripcionPlan) {
        setIdPlan(idPlan);
        setNombrePlan(nombrePlan);
        setVelocidadPlan(velocidadPlan);
        setPrecio(precio);
        setDescripcionPlan(descripcionPlan);
        setFechaActivacion("");
    }

    public int getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(int idPlan) {
        if (idPlan > 0) {
            this.idPlan = idPlan;
        } else {
            this.idPlan = 1;
        }
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        if (nombrePlan != null && !nombrePlan.trim().isEmpty()) {
            this.nombrePlan = nombrePlan.trim();
        } else {
            this.nombrePlan = "Sin nombre";
        }
    }

    public String getVelocidadPlan() {
        return velocidadPlan;
    }

    public void setVelocidadPlan(String velocidadPlan) {
        if (velocidadPlan != null && !velocidadPlan.trim().isEmpty()) {
            this.velocidadPlan = velocidadPlan.trim();
        } else {
            this.velocidadPlan = "0 Mbps";
        }
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio >= 0) {
            this.precio = precio;
        } else {
            this.precio = 0;
        }
    }

    public String getDescripcionPlan() {
        return descripcionPlan;
    }

    public void setDescripcionPlan(String descripcionPlan) {
        if (descripcionPlan != null && !descripcionPlan.trim().isEmpty()) {
            this.descripcionPlan = descripcionPlan.trim();
        } else {
            this.descripcionPlan = "Sin descripcion";
        }
    }

    public String getFechaActivacion() {
        return fechaActivacion;
    }

    public void setFechaActivacion(String fechaActivacion) {
        if (fechaActivacion != null && !fechaActivacion.trim().isEmpty()) {
            this.fechaActivacion = fechaActivacion.trim();
        } else {
            this.fechaActivacion = "Sin fecha";
        }
    }

    @Override
    public String toString() {
        return idPlan + " " + nombrePlan + " - " + velocidadPlan + " " + precio + " " + descripcionPlan;
    }
}

