/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Juanes
 */
public class factura {
    private int idFactura;
    private String Usuario;
    private int idPlan;
    private String fecha;
    private double total;
    private String archivoPDF;
    
    
    public factura(int idFactura, String Usuario, int idPlan, String fecha, double total, String archivoPDF) {
        this.idFactura = idFactura;
        this.Usuario = Usuario;
        this.idPlan = idPlan;
        this.fecha = fecha;
        this.total = total;
        this.archivoPDF = archivoPDF;
    }
    
    public int getIdFactura() { return idFactura; }
    public String getUsuario() { return Usuario; }
    public int getIdPlan() { return idPlan; }
    public String getFecha() { return fecha; }
    public double getTotal() { return total; }
    public String getArchivoPDF() { return archivoPDF; }

    
    
}
