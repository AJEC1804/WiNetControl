package metodos;

/**
 * Implementación de Lista Doblemente Enlazada.
 * Estructura bidireccional con punteros 'primero' y 'ultimo',
 * implementando fielmente la metodología vista en clase
 * y completando todos los métodos requeridos para el CRUD completo.
 */
public class ListaDoble {

    private Nodo primero;
    private Nodo ultimo;

    public ListaDoble() {
        this.primero = null;
        this.ultimo = null;
    }

    // 1. Insertar al inicio de la lista doble
    public void insertar_inicio(Nodo nuevo) {
        if (nuevo == null) return;
        if (primero == null && ultimo == null) {
            primero = nuevo;
            ultimo = nuevo;
        } else {
            nuevo.setSiguiente(primero);
            primero.setAnterior(nuevo);
            primero = nuevo;
        }
    }

    // 2. Insertar al final de la lista doble
    public void insertar_final_lista_doble(Nodo nuevo) {
        if (nuevo == null) return;
        if (this.primero == null && this.ultimo == null) {
            this.primero = nuevo;
            this.ultimo = nuevo;
        } else {
            this.ultimo.setSiguiente(nuevo);
            nuevo.setAnterior(this.ultimo);
            this.ultimo = nuevo;
        }
    }

    // 3. Insertar antes de un nodo referente
    public boolean insertar_antes_nodo_ref_doble(Nodo nuevo, String ref) {
        if (nuevo == null || ref == null) return false;
        if (primero == null) {
            System.out.println("Lista vacía");
            return false;
        }

        // Si el referente es el primero
        if (primero.getDato().equalsIgnoreCase(ref)) {
            insertar_inicio(nuevo);
            return true;
        }

        Nodo sapo = primero;
        while (sapo != null && !sapo.getDato().equalsIgnoreCase(ref)) {
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) {
            System.out.println("Nodo no encontrado");
            return false;
        } else {
            nuevo.setAnterior(sapo.getAnterior());
            nuevo.setSiguiente(sapo);
            sapo.getAnterior().setSiguiente(nuevo);
            sapo.setAnterior(nuevo);
            return true;
        }
    }

    // 4. [COMPLETADO] Insertar después de un nodo referente
    public boolean insertar_despues_nodo_ref_doble(Nodo nuevo, String ref) {
        if (nuevo == null || ref == null) return false;
        if (primero == null) {
            System.out.println("Lista vacía");
            return false;
        }

        // Si el referente es el último
        if (ultimo.getDato().equalsIgnoreCase(ref)) {
            insertar_final_lista_doble(nuevo);
            return true;
        }

        Nodo sapo = primero;
        while (sapo != null && !sapo.getDato().equalsIgnoreCase(ref)) {
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) {
            System.out.println("Nodo referente no encontrado");
            return false;
        } else {
            nuevo.setSiguiente(sapo.getSiguiente());
            nuevo.setAnterior(sapo);
            sapo.getSiguiente().setAnterior(nuevo);
            sapo.setSiguiente(nuevo);
            return true;
        }
    }

    // 5. Eliminar al inicio de la lista doble
    public Nodo eliminar_inicio_lista_doble() {
        if (this.primero == null && this.ultimo == null) {
            System.out.println("Lista vacía");
            return null;
        }

        Nodo sapo = this.primero;
        if (this.primero == this.ultimo) {
            this.primero = null;
            this.ultimo = null;
        } else {
            this.primero = this.primero.getSiguiente();
            this.primero.setAnterior(null);
            sapo.setSiguiente(null);
        }
        return sapo;
    }

    // 6. [COMPLETADO] Eliminar al final de la lista doble
    public Nodo eliminar_final_lista_doble() {
        if (this.primero == null && this.ultimo == null) {
            System.out.println("Lista vacía");
            return null;
        }

        Nodo sapo = this.ultimo;
        if (this.primero == this.ultimo) {
            this.primero = null;
            this.ultimo = null;
        } else {
            this.ultimo = this.ultimo.getAnterior();
            this.ultimo.setSiguiente(null);
            sapo.setAnterior(null);
        }
        return sapo;
    }

    // 7. Eliminar el nodo referente en lista doble
    public boolean eliminar_nodo_referente_doble(String ref) {
        if (primero == null || ref == null) {
            System.out.println("Lista vacía");
            return false;
        }

        Nodo sapo = primero;
        while (sapo != null && !sapo.getDato().equalsIgnoreCase(ref)) {
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) {
            System.out.println("Nodo no encontrado");
            return false;
        }

        // Si es el único elemento
        if (sapo == primero && sapo == ultimo) {
            primero = null;
            ultimo = null;
        } else if (sapo == primero) {
            eliminar_inicio_lista_doble();
        } else if (sapo == ultimo) {
            eliminar_final_lista_doble();
        } else {
            sapo.getAnterior().setSiguiente(sapo.getSiguiente());
            sapo.getSiguiente().setAnterior(sapo.getAnterior());
            sapo.setSiguiente(null);
            sapo.setAnterior(null);
        }
        return true;
    }

    // 8. Eliminar antes de un nodo referente
    public boolean eliminar_antes_nodo_ref_doble(String ref) {
        if (primero == null || ref == null) {
            System.out.println("Lista vacía");
            return false;
        }

        Nodo sapo = primero;
        while (sapo != null && !sapo.getDato().equalsIgnoreCase(ref)) {
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) {
            System.out.println("Nodo referente no encontrado");
            return false;
        }

        if (sapo == primero) {
            System.out.println("No existe ningún nodo antes del referente (es el primero)");
            return false;
        }

        Nodo sapa = sapo.getAnterior();
        if (sapa == primero) {
            eliminar_inicio_lista_doble();
        } else {
            sapa.getAnterior().setSiguiente(sapo);
            sapo.setAnterior(sapa.getAnterior());
            sapa.setSiguiente(null);
            sapa.setAnterior(null);
        }
        return true;
    }

    // 9. [COMPLETADO] Eliminar después de un nodo referente
    public boolean eliminar_despues_nodo_ref_doble(String ref) {
        if (primero == null || ref == null) {
            System.out.println("Lista vacía");
            return false;
        }

        Nodo sapo = primero;
        while (sapo != null && !sapo.getDato().equalsIgnoreCase(ref)) {
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) {
            System.out.println("Nodo referente no encontrado");
            return false;
        }

        if (sapo == ultimo) {
            System.out.println("No existe ningún nodo después del referente (es el último)");
            return false;
        }

        Nodo sapa = sapo.getSiguiente();
        if (sapa == ultimo) {
            eliminar_final_lista_doble();
        } else {
            sapo.setSiguiente(sapa.getSiguiente());
            sapa.getSiguiente().setAnterior(sapo);
            sapa.setSiguiente(null);
            sapa.setAnterior(null);
        }
        return true;
    }

    // 10. [COMPLETADO - BUSCAR CRUD]
    public Nodo buscar_nodo_doble(String ref) {
        if (primero == null || ref == null) return null;
        Nodo sapo = primero;
        while (sapo != null) {
            if (sapo.getDato().equalsIgnoreCase(ref)) {
                return sapo;
            }
            sapo = sapo.getSiguiente();
        }
        return null;
    }

    // 11. [COMPLETADO - ACTUALIZAR CRUD]
    public boolean actualizar_nodo_doble(String ref, String nuevoDato) {
        Nodo encontrado = buscar_nodo_doble(ref);
        if (encontrado != null) {
            encontrado.setDato(nuevoDato);
            return true;
        }
        return false;
    }

    // 12. Recorrer hacia adelante (de primero a ultimo)
    public void recorrer() {
        if (this.primero == null) {
            System.out.println("Lista vacía");
        } else {
            Nodo sapo = primero;
            StringBuilder sb = new StringBuilder("null <- ");
            while (sapo != null) {
                sb.append("[").append(sapo.getDato()).append("]");
                if (sapo.getSiguiente() != null) {
                    sb.append(" <=> ");
                }
                sapo = sapo.getSiguiente();
            }
            sb.append(" -> null");
            System.out.println(sb.toString());
        }
    }

    // 13. [COMPLETADO - RECORRERBACK] Recorrer hacia atrás (de ultimo a primero)
    public void recorrerback() {
        if (this.ultimo == null) {
            System.out.println("Lista vacía");
        } else {
            Nodo sapo = ultimo;
            StringBuilder sb = new StringBuilder("null <- ");
            while (sapo != null) {
                sb.append("[").append(sapo.getDato()).append("]");
                if (sapo.getAnterior() != null) {
                    sb.append(" <=> ");
                }
                sapo = sapo.getAnterior();
            }
            sb.append(" -> null");
            System.out.println(sb.toString());
        }
    }

    public boolean estaVacia() {
        return primero == null;
    }

    public int getTamano() {
        int c = 0;
        Nodo sapo = primero;
        while (sapo != null) {
            c++;
            sapo = sapo.getSiguiente();
        }
        return c;
    }

    public Nodo getPrimero() {
        return primero;
    }

    public void setPrimero(Nodo primero) {
        this.primero = primero;
    }

    public Nodo getUltimo() {
        return ultimo;
    }

    public void setUltimo(Nodo ultimo) {
        this.ultimo = ultimo;
    }
}
