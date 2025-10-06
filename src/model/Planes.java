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
    private int codigo;
    private String nombre;
    private String velocidad;
    private int precio;
    private String descripcion;
  

    public Planes(String nombre, String apellido,String tipo, String identificacion, String telefono, String correo, String direccion, String contrasena, String confContrasena) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.velocidad = velocidad;
        this.precio = precio;
        this.descripcion = descripcion;
       
    }

    public int getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getVelocidad() { return velocidad; }
    public int getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }
   
    
   
   

    
    public String toString() {
        return codigo + " " + nombre + " - " + velocidad + " " + precio + " " + descripcion + " ";
    }
}

