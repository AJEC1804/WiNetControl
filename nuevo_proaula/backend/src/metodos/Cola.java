package metodos;

import java.io.Serializable;

public class Cola implements Serializable {
    private static final long serialVersionUID = 1L;

    private Nodo frente;
    private Nodo final_;
    private int tamano;

    public Cola() {
        this.frente = null;
        this.final_ = null;
        this.tamano = 0;
    }

    public void encolar(Nodo nuevo) {
        if (nuevo == null) return;
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

    public Nodo desencolar() {
        if (frente == null) {
            final_ = null;
            tamano = 0;
            System.out.println("Cola vacía");
            return null;
        }
        Nodo aux = frente;
        if (frente == final_) {
            frente = null;
            final_ = null;
        } else {
            frente = frente.getSiguiente();
            if (frente != null) {
                frente.setAnterior(null);
            }
        }
        aux.setSiguiente(null);
        aux.setAnterior(null);
        if (tamano > 0) {
            tamano--;
        }
        return aux;
    }

    public Nodo getFrente() {
        return frente;
    }

    public Nodo getFinal_() {
        return final_;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamano() {
        return tamano;
    }

    public void recorrer() {
        if (estaVacia()) {
            System.out.println("Cola vacía");
            return;
        }
        Nodo sapo = frente;
        System.out.print("[FRENTE] -> ");
        while (sapo != null) {
            System.out.print("[" + sapo.getDato() + "] -> ");
            sapo = sapo.getSiguiente();
        }
        System.out.println("[FINAL]");
    }
}
