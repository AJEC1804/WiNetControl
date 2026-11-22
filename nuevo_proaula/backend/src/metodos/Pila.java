package metodos;

import java.io.Serializable;

public class Pila implements Serializable {
    private static final long serialVersionUID = 1L;

    private Nodo cima;
    private int tamano;

    public Pila() {
        this.cima = null;
        this.tamano = 0;
    }

    public void apilar(Nodo nuevo) {
        if (nuevo == null) return;
        if (cima == null) {
            cima = nuevo;
        } else {
            nuevo.setSiguiente(cima);
            cima = nuevo;
        }
        tamano++;
    }

    public Nodo desapilar() {
        if (cima == null) {
            System.out.println("Pila vacía");
            return null;
        }
        Nodo aux = cima;
        cima = cima.getSiguiente();
        aux.setSiguiente(null);
        tamano--;
        return aux;
    }

    public Nodo getCima() {
        return cima;
    }

    public boolean estaVacia() {
        return cima == null;
    }

    public int getTamano() {
        return tamano;
    }

    public void recorrer() {
        if (cima == null) {
            System.out.println("Pila vacía");
            return;
        }
        Nodo sapo = cima;
        System.out.println("--- CIMA DE LA PILA ---");
        while (sapo != null) {
            System.out.println("| " + sapo.getDato() + " |");
            sapo = sapo.getSiguiente();
        }
        System.out.println("--- BASE DE LA PILA ---");
    }
}
