/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Gyanela Meza
 */
public class Cliente {
    private String nombre;
    private String apellido;
    private String tipo;
    private String identificacion;
    private String telefono;
    private String correo;
    private String direccion;
    private String contrasena;
    private String confContrasena;

    public Cliente(String nombre, String apellido,String tipo, String identificacion, String telefono, String correo, String direccion, String contrasena, String confContrasena) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.tipo = tipo;
        this.identificacion = identificacion;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.contrasena = contrasena;
        this.confContrasena = confContrasena;
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTipo() { return tipo; }
    public String getIdentificacion() { return identificacion; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getDireccion() { return direccion; }
    public String getContrasena() { return contrasena; }
    public String getConfContrasena() { return confContrasena; }
    
   
   

    
    public String toString() {
        return nombre + " " + apellido + " - " + tipo + "." + identificacion;
    }
}
