package metodos;

import java.io.Serializable;

public class ListaSimple implements Serializable {
    private static final long serialVersionUID = 1L;

    private Nodo primero;

    public ListaSimple() {
        this.primero = null;
    }

    public void insertar_inicio(Nodo nuevo) {
        if (nuevo == null) return;
        if (primero == null) {
            primero = nuevo;
        } else {
            nuevo.setSiguiente(primero);
            this.primero = nuevo;
        }
    }

    public void insertar_final(Nodo nuevo) {
        if (nuevo == null) return;
        if (primero == null) {
            this.primero = nuevo;
        } else {
            Nodo sapo = primero;
            while (sapo.getSiguiente() != null) {
                sapo = sapo.getSiguiente();
            }
            sapo.setSiguiente(nuevo);
        }
    }

    public boolean insertar_antes_nodo_ref_simple(Nodo nuevo, String ref) {
        if (nuevo == null || ref == null) return false;
        if (primero == null) {
            System.out.println("Lista vacía");
            return false;
        }

        if (primero.getDato().equalsIgnoreCase(ref)) {
            nuevo.setSiguiente(primero);
            this.primero = nuevo;
            return true;
        }

        Nodo sapo = primero;
        Nodo sapa = null;

        while (sapo != null && !sapo.getDato().equalsIgnoreCase(ref)) {
            sapa = sapo;
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) {
            System.out.println("Nodo no encontrado");
            return false;
        } else {
            nuevo.setSiguiente(sapo);
            sapa.setSiguiente(nuevo);
            return true;
        }
    }

    public boolean insertar_despues_nodo_ref_simple(Nodo nuevo, String ref) {
        if (nuevo == null || ref == null) return false;
        if (primero == null) {
            System.out.println("Lista vacía");
            return false;
        }

        Nodo sapo = primero;
        while (sapo != null) {
            if (sapo.getDato().equalsIgnoreCase(ref)) {
                break;
            }
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) {
            System.out.println("Nodo no encontrado");
            return false;
        } else {
            nuevo.setSiguiente(sapo.getSiguiente());
            sapo.setSiguiente(nuevo);
            return true;
        }
    }

    public Nodo eliminar_inicio_lista_simple() {
        if (primero == null) {
            System.out.println("Lista vacía");
            return null;
        }
        Nodo sapo = primero;
        this.primero = this.primero.getSiguiente();
        sapo.setSiguiente(null);
        return sapo;
    }

    public Nodo eliminar_final_lista_simple() {
        if (primero == null) {
            System.out.println("Lista vacía");
            return null;
        }
        if (primero.getSiguiente() == null) {
            Nodo eliminado = primero;
            primero = null;
            return eliminado;
        }

        Nodo sapo = primero;
        while (sapo.getSiguiente().getSiguiente() != null) {
            sapo = sapo.getSiguiente();
        }

        Nodo ultimo = sapo.getSiguiente();
        sapo.setSiguiente(null);
        return ultimo;
    }

    public boolean eliminar_nodo_referente_simple(String ref) {
        if (primero == null || ref == null) {
            System.out.println("Lista vacía o referencia nula");
            return false;
        }

        if (primero.getDato().equalsIgnoreCase(ref)) {
            eliminar_inicio_lista_simple();
            return true;
        }

        Nodo sapo = primero;
        Nodo sapa = null;

        while (sapo != null && !sapo.getDato().equalsIgnoreCase(ref)) {
            sapa = sapo;
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) {
            System.out.println("Nodo no encontrado");
            return false;
        } else {
            sapa.setSiguiente(sapo.getSiguiente());
            sapo.setSiguiente(null);
            return true;
        }
    }

    public boolean eliminar_antes_nodo_ref_simple(String ref) {
        if (primero == null || primero.getSiguiente() == null || ref == null) {
            System.out.println("No hay suficientes nodos para eliminar antes del referente");
            return false;
        }

        if (primero.getSiguiente().getDato().equalsIgnoreCase(ref)) {
            eliminar_inicio_lista_simple();
            return true;
        }

        Nodo sapito = null;
        Nodo sapa = primero;
        Nodo sapo = primero.getSiguiente();

        while (sapo != null && !sapo.getDato().equalsIgnoreCase(ref)) {
            sapito = sapa;
            sapa = sapo;
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) {
            System.out.println("Nodo no encontrado");
            return false;
        } else {
            sapito.setSiguiente(sapo);
            sapa.setSiguiente(null);
            return true;
        }
    }

    public boolean eliminar_despues_nodo_ref_simple(String ref) {
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

        if (sapo.getSiguiente() == null) {
            System.out.println("No hay ningún nodo después del referente");
            return false;
        }

        Nodo aEliminar = sapo.getSiguiente();
        sapo.setSiguiente(aEliminar.getSiguiente());
        aEliminar.setSiguiente(null);
        return true;
    }

    public Nodo buscar_nodo_simple(String ref) {
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

    public boolean actualizar_nodo_simple(String ref, String nuevoDato) {
        Nodo encontrado = buscar_nodo_simple(ref);
        if (encontrado != null) {
            encontrado.setDato(nuevoDato);
            return true;
        }
        return false;
    }

    public void recorrer() {
        if (this.primero == null) {
            System.out.println("Lista vacía");
        } else {
            Nodo sapo = primero;
            StringBuilder sb = new StringBuilder();
            while (sapo != null) {
                sb.append("[").append(sapo.getDato()).append("]->");
                sapo = sapo.getSiguiente();
            }
            sb.append("null");
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
}
