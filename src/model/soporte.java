/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author EUMAPE
 */
public class soporte {
    
    private String usuario;
    private String asunto;
    private String mensaje;
    

    // esto es un constructor, para inicializar y crear los objetos de las clases 
    public soporte(String usuario, String asunto, String mensaje) {
        this.usuario = usuario;
        this.asunto = asunto;
        this.mensaje = mensaje;
        
    }
    // son los get para atrapar la informacion
    public String getUsuario() { return usuario; }
    public String getAsunto() { return asunto; }
    public String getDescripcion() { return mensaje; }
    
    
    public String toString() {
        return usuario + " " + asunto + " - " + mensaje;
    }

}
