/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Gyanela Meza
 */
public class cliente {
    public class Cliente {
    private String nombre;
    private String apellido;
    private String telefono;
    private String documento;
    private String correo;

    public Cliente(String nombre, String apellido, String telefono, String documento, String correo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.documento = documento;
        this.correo = correo;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public String getDocumento() { return documento; }
    public String getCorreo() { return correo; }

    
    public String toString() {
        return nombre + " " + apellido + " - " + documento;
    }
}
    
}
