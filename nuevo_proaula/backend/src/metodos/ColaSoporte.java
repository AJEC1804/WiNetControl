package metodos;

import java.io.Serializable;

import entidades.Soporte;
import java.util.ArrayList;
import java.util.List;

/**
 * Cola (FIFO) especializada para Tickets de Soporte Técnico.
 * Estructura de datos autosuficiente mediante nodos enlazados desde el frente al final.
 */
public class ColaSoporte implements Serializable {
    private static final long serialVersionUID = 1L;

    // Nodo doblemente enlazado para Cola de Soporte
    public static class NodoSoporte implements Serializable {
        private static final long serialVersionUID = 1L;
        private Soporte dato;
        private NodoSoporte siguiente;
        private NodoSoporte anterior;

        public NodoSoporte(Soporte dato) {
            this.dato = dato;
            this.siguiente = null;
            this.anterior = null;
        }

        public Soporte getDato() { return dato; }
        public void setDato(Soporte dato) { this.dato = dato; }
        public NodoSoporte getSiguiente() { return siguiente; }
        public void setSiguiente(NodoSoporte siguiente) { this.siguiente = siguiente; }
        public NodoSoporte getAnterior() { return anterior; }
        public void setAnterior(NodoSoporte anterior) { this.anterior = anterior; }
    }

    private NodoSoporte frente;
    private NodoSoporte final_;
    private int tamano;

    public ColaSoporte() {
        this.frente = null;
        this.final_ = null;
        this.tamano = 0;
    }

    // Encolar (Enqueue)
    public void encolar(Soporte nuevoMensaje) {
        if (nuevoMensaje == null) return;
        NodoSoporte nuevo = new NodoSoporte(nuevoMensaje);
        if (frente == null && final_ == null) {
            frente = nuevo;
            final_ = nuevo;
        } else {
            nuevo.setAnterior(final_);
            final_.setSiguiente(nuevo);
            final_ = nuevo;
        }
        tamano++;
    }

    // Desencolar (Dequeue)
    public Soporte desencolar() {
        if (frente == null && final_ == null) {
            return null;
        }
        NodoSoporte aux = frente;
        if (frente == final_) {
            frente = null;
            final_ = null;
        } else {
            frente = frente.getSiguiente();
            frente.setAnterior(null);
        }
        aux.setSiguiente(null);
        aux.setAnterior(null);
        tamano--;
        return aux.getDato();
    }

    public Soporte getFrente() {
        return (frente != null) ? frente.getDato() : null;
    }

    public Soporte getFinal_() {
        return (final_ != null) ? final_.getDato() : null;
    }

    public List<Soporte> listarPendientes() {
        List<Soporte> lista = new ArrayList<>();
        NodoSoporte sapo = frente;
        while (sapo != null) {
            if (sapo.getDato() != null) {
                lista.add(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return lista;
    }

    public boolean estaVacia() {
        return frente == null && final_ == null;
    }

    public int getTamano() {
        return tamano;
    }

    public void recorrer() {
        if (estaVacia()) {
            System.out.println("Cola de soporte vacía");
            return;
        }
        NodoSoporte sapo = frente;
        while (sapo != null) {
            System.out.println(sapo.getDato());
            sapo = sapo.getSiguiente();
        }
    }
}
