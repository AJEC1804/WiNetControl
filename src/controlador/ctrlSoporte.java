/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;
import model.soporte;


/**
 *
 * @author EUMAPE
 */
public class ctrlSoporte {
    static final int ELEMENTOS = 50; 
    private static soporte[] mensajes = new soporte[ELEMENTOS]; 
    private static int contadorMensajes = 0; 

   
    public boolean guardarMensaje(soporte mensaje) {
        if (contadorMensajes >= ELEMENTOS) {
            return false;
        }
        mensajes[contadorMensajes] = mensaje; 
        contadorMensajes++; 
        return true;
    }
    
    public boolean enviarMensaje(soporte mensaje, String correoDestino) {
    // Guardar el mensaje en el arreglo
        boolean guardado = guardarMensaje(mensaje);
        String cuerpo = mensaje.getUsuario() + "-" + mensaje.getAsunto() + "-" + mensaje.getDescripcion();
        
        boolean enviado = correoSoporte.enviarCorreo(correoDestino, "Soporte - " + mensaje.getAsunto(), cuerpo);

        return enviado;
    }

    public soporte getMensaje(int index) {
        if (index >= 0 && index < contadorMensajes) {
            return mensajes[index];
        }
        return null;
    }

    public soporte[] getTodosLosMensajes() {
        return mensajes;
    }

    // Contador de mensajes actuales
    public int getContadorMensajes() {
        return contadorMensajes;
    }
}
