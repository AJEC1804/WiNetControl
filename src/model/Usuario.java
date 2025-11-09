/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author EUMAPE
 */
public class Usuario {

    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;
    private String identificacion;
    private String tipodocumento;
    private String direccion;
    private String contrasena;
    private plan PlanActual;
    
    
   
    public Usuario(String nombre, String apellido, String telefono, String correo, String identificacion, String tipodocumento, String direccion, String contrasena) {
        
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
        this.identificacion = identificacion;
        this.tipodocumento = tipodocumento; 
        this.direccion = direccion;
        this.contrasena = contrasena; 
    }  
    
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getIdentificacion() { return identificacion; }
    public String getTipoDocumento() { return tipodocumento; }
    public String getDireccion() { return direccion; }
    public String getContrasena() { return contrasena; }
    
    public boolean verificarContrasena(String contrasenaIngresada){
      return this.contrasena.equals(contrasenaIngresada);
    }
    
    public plan getPlanActual() {
        return PlanActual;
    }

    
}


