package metodos;

import java.io.Serializable;

/**
 * Clase Nodo fundamental para listas simples y listas dobles.
 * Contiene el dato almacenado y las referencias (punteros) al siguiente y anterior.
 */
public class Nodo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dato;
    private Nodo siguiente;
    private Nodo anterior;

    public Nodo(String dato) {
        this.dato = dato;
        this.siguiente = null;
        this.anterior = null;
    }

    public String getDato() {
        return dato;
    }

    public void setDato(String dato) {
        this.dato = dato;
    }

    public Nodo getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(Nodo siguiente) {
        this.siguiente = siguiente;
    }

    public Nodo getAnterior() {
        return anterior;
    }

    public void setAnterior(Nodo anterior) {
        this.anterior = anterior;
    }

    @Override
    public String toString() {
        return "[" + dato + "]";
    }
}
