package controlador;

import entidades.Nodos;
import model.soporte;

/**
 *
 * @author EUMAPE
 */
public class ctrlSoporte {

    // Punteros de la cola
    private static Nodos.NodoSoporte frente = null;
    private static Nodos.NodoSoporte final_ = null;

    // Metodo para encolar un mensaje de soporte al final (FIFO)
    public static void encolar(soporte nuevoMensaje) {
        if (nuevoMensaje == null) {
            return;
        }
        Nodos.NodoSoporte nuevo = new Nodos.NodoSoporte(nuevoMensaje);
        if (frente == null && final_ == null) {
            frente = nuevo;
            final_ = nuevo;
        } else {
            nuevo.setAnterior(final_);
            final_.setSiguiente(nuevo);
            final_ = nuevo;
        }
    }

    // Metodo para desencolar un mensaje del frente (FIFO)
    public static soporte desencolar() {
        if (frente == null && final_ == null) {
            return null;
        }
        Nodos.NodoSoporte aux = frente;
        if (frente == final_) {
            frente = null;
            final_ = null;
        } else {
            frente = frente.getSiguiente();
            frente.setAnterior(null);
        }
        return aux.getDato();
    }

    // Guardar mensaje en la cola
    public boolean guardarMensaje(soporte mensaje) {
        if (mensaje == null) {
            return false;
        }
        encolar(mensaje);
        return true;
    }

    // Enviar mensaje y encolarlo
    public boolean enviarMensaje(soporte mensaje, String correoDestino) {
        boolean guardado = guardarMensaje(mensaje);
        String cuerpo = mensaje.getUsuario() + "-" + mensaje.getAsunto() + "-" + mensaje.getDescripcion();
        boolean enviado = correoSoporte.enviarCorreo(correoDestino, "Soporte - " + mensaje.getAsunto(), cuerpo);
        return enviado;
    }

    public static Nodos.NodoSoporte getFrente() {
        return frente;
    }

    public static Nodos.NodoSoporte getFinal_() {
        return final_;
    }
}
