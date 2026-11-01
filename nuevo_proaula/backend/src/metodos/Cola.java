package metodos;

/**
 * Estructura de datos Cola (FIFO - First In, First Out) implementada con punteros mediante Nodos.
 * Utilizada para encolar solicitudes y tickets en orden de llegada.
 */
public class Cola {

    private Nodo frente;
    private Nodo final_;
    private int tamano;

    public Cola() {
        this.frente = null;
        this.final_ = null;
        this.tamano = 0;
    }

    // Encolar (Enqueue) al final de la cola
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

    // Desencolar (Dequeue) del frente de la cola
    public Nodo desencolar() {
        if (frente == null && final_ == null) {
            System.out.println("Cola vacía");
            return null;
        }
        Nodo aux = frente;
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
        return aux;
    }

    public Nodo getFrente() {
        return frente;
    }

    public Nodo getFinal_() {
        return final_;
    }

    public boolean estaVacia() {
        return frente == null && final_ == null;
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
