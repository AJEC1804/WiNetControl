/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author EUMAPE
 */
public class plan {
    
    private int idPlan;
    private String nombrePlan;
    private String descripcionPlan;
    private String fechaActivacion;
    private String fechaVencimiento;
    private String estado;
    private double valorMensual;
    private String metodoPago;
    
    public plan (int idPlan, String nombrePlan, String descripcionPlan, String fechaActivacion, String fechaVencimiento, String estado, double valorMensual, String metodoPago){
        
        this.idPlan = idPlan;
        this.nombrePlan = nombrePlan;
        this.descripcionPlan= descripcionPlan;
        this.fechaActivacion = fechaActivacion;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
        this.valorMensual = valorMensual;
        this.metodoPago = metodoPago;
    }

    // con lo que puedo obtener valores
    public int getIdPlan() { return idPlan;}
    public String getNombrePlan() { return nombrePlan;}
    public String getDescripcionPlan() { return descripcionPlan;}
    public String getFechaActivacion() { return fechaActivacion;}
    public String getFechaVencimiento() { return fechaVencimiento;}
    public String getEstado() { return estado;}
    public double getValorMensual() { return valorMensual;}
    public String getMetodoPago() { return metodoPago;}
    
    //para poder cambiar esos valores
    public void setIdPlan(int idPlan) { this.idPlan = idPlan; }
    public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }
    public void setDescripcionPlan(String descripcionPlan) { this.descripcionPlan = descripcionPlan; }
    public void setFechaActivacion(String fechaActivacion) { this.fechaActivacion = fechaActivacion; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setValorMensual(double valorMensual) { this.valorMensual = valorMensual; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}
    

