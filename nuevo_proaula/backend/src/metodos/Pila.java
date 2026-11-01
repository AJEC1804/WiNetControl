package metodos;

/**
 * Estructura de datos Pila (LIFO - Last In, First Out) implementada con punteros mediante Nodos.
 * Utilizada para apilar y desapilar elementos (como el historial de facturas).
 */
public class Pila {

    private Nodo cima;
    private int tamano;

    public Pila() {
        this.cima = null;
        this.tamano = 0;
    }

    // Apilar (Push) en la cima
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

    // Desapilar (Pop) de la cima
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

    // Consultar cima (Peek)
    public Nodo getCima() {
        return cima;
    }

    public boolean estaVacia() {
        return cima == null;
    }

    public int getTamano() {
        return tamano;
    }

    // Recorrer desde la cima hacia la base
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
