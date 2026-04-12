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
        this.idPlan = idPlan;
        this.nombrePlan = nombrePlan;
        this.velocidadPlan = velocidadPlan;
        this.precio = precio;
        this.descripcionPlan = descripcionPlan;
        this.fechaActivacion = ""; 
    }

    public int getIdPlan() { return idPlan; }
    public String getNombrePlan() { return nombrePlan; }
    public String getVelocidadPlan() { return velocidadPlan; }
    public String getDescripcionPlan() { return descripcionPlan; }
    public double getPrecio() { return precio; }
    public String getFechaActivacion() { return fechaActivacion; }

    public void setIdPlan(int idPlan) { this.idPlan = idPlan; }
    public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }
    public void setVelocidadPlan(String velocidadPlan) { this.velocidadPlan = velocidadPlan; }
    public void setDescripcionPlan(String descripcionPlan) { this.descripcionPlan = descripcionPlan; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setFechaActivacion(String fechaActivacion) { this.fechaActivacion = fechaActivacion; }
}

