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

    public int idPlan;
    public String nombrePlan;
    public String velocidadplan;  // Lo dejo por si lo necesitas luego
    public String descripcionPlan;
    public String fechaActivacion;
    public double precio;
    public String planActual;
    // Constructor básico corregido
    public Planes(int idPlan, String nombrePlan, String descripcionPlan, double precio, String fechaActivacion) {
        this.idPlan = idPlan;
        this.nombrePlan = nombrePlan;
        this.descripcionPlan = descripcionPlan;
        this.precio = precio;
        this.fechaActivacion = fechaActivacion;
    }

    // Getters
    public int getIdPlan() { return idPlan; }
    public String getNombrePlan() { return nombrePlan; }
    public String getVelocidadPlan() { return velocidadplan; }
    public String getDescripcionPlan() { return descripcionPlan; }
    public String getFechaActivacion() { return fechaActivacion; }
    public double getPrecio() { return precio; }

    // Setters
    public void setIdPlan(int idPlan) { this.idPlan = idPlan; }
    public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }
    public void setVelocidadPlan(String velocidadPlan) { this.velocidadplan = velocidadPlan; }
    public void setDescripcionPlan(String descripcionPlan) { this.descripcionPlan = descripcionPlan; }
    public void setFechaActivacion(String fechaActivacion) { this.fechaActivacion = fechaActivacion; }
    public void setPrecio(double precio) { this.precio = precio; }
}

