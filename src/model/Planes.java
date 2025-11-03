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

    // Constructor correcto
    public Planes(int codigo, String nombre, String velocidad, int precio, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.velocidad = velocidad;
        this.precio = precio;
        this.descripcion = descripcion;
    }

    // Getters
    public int getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getVelocidad() { return velocidad; }
    public int getPrecio() { return precio; }
    public String getDescripcion() { return descripcion; }

    // Setters
    public void setCodigo(int codigo) { this.codigo = codigo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setVelocidad(String velocidad) { this.velocidad = velocidad; }
    public void setPrecio(int precio) { this.precio = precio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // toString()
    public String toString() {
        return codigo + " " + nombre + " - " + velocidad + " " + precio + " " + descripcion;
    }
}


