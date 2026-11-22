package metodos;

import entidades.Empresa;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListaEmpresas implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class NodoEmpresa implements Serializable {
        private static final long serialVersionUID = 1L;
        private Empresa dato;
        private NodoEmpresa siguiente;
        private NodoEmpresa anterior;

        public NodoEmpresa(Empresa dato) {
            this.dato = dato;
        }

        public Empresa getDato() { return dato; }
        public void setDato(Empresa dato) { this.dato = dato; }
        public NodoEmpresa getSiguiente() { return siguiente; }
        public void setSiguiente(NodoEmpresa siguiente) { this.siguiente = siguiente; }
        public NodoEmpresa getAnterior() { return anterior; }
        public void setAnterior(NodoEmpresa anterior) { this.anterior = anterior; }
    }

    private NodoEmpresa primero;
    private NodoEmpresa ultimo;
    private int tamano;

    public ListaEmpresas() {
        this.primero = null;
        this.ultimo = null;
        this.tamano = 0;
    }

    public void insertarFinal(Empresa nueva) {
        if (nueva == null) return;
        NodoEmpresa nodo = new NodoEmpresa(nueva);
        if (primero == null) {
            primero = nodo;
            ultimo = nodo;
        } else {
            ultimo.setSiguiente(nodo);
            nodo.setAnterior(ultimo);
            ultimo = nodo;
        }
        tamano++;
    }

    public Empresa buscarPorId(String idEmpresa) {
        if (idEmpresa == null || primero == null) return null;
        String id = idEmpresa.trim();
        NodoEmpresa aux = primero;
        while (aux != null) {
            if (aux.getDato() != null && aux.getDato().getIdEmpresa().equalsIgnoreCase(id)) {
                return aux.getDato();
            }
            aux = aux.getSiguiente();
        }
        return null;
    }

    public Empresa buscarPorNit(String nit) {
        if (nit == null || primero == null) return null;
        String buscado = nit.trim();
        NodoEmpresa aux = primero;
        while (aux != null) {
            if (aux.getDato() != null && aux.getDato().getNit().equalsIgnoreCase(buscado)) {
                return aux.getDato();
            }
            aux = aux.getSiguiente();
        }
        return null;
    }

    public List<Empresa> listarTodas() {
        List<Empresa> lista = new ArrayList<>();
        NodoEmpresa aux = primero;
        while (aux != null) {
            if (aux.getDato() != null) lista.add(aux.getDato());
            aux = aux.getSiguiente();
        }
        return lista;
    }

    public List<Empresa> listarPorEstado(boolean activo) {
        List<Empresa> lista = new ArrayList<>();
        NodoEmpresa aux = primero;
        while (aux != null) {
            if (aux.getDato() != null && aux.getDato().isActivo() == activo) {
                lista.add(aux.getDato());
            }
            aux = aux.getSiguiente();
        }
        return lista;
    }

    public boolean eliminar(String idEmpresa) {
        if (idEmpresa == null || primero == null) return false;
        String id = idEmpresa.trim();
        NodoEmpresa aux = primero;
        while (aux != null) {
            if (aux.getDato() != null && aux.getDato().getIdEmpresa().equalsIgnoreCase(id)) {
                if (aux.getAnterior() != null) aux.getAnterior().setSiguiente(aux.getSiguiente());
                else primero = aux.getSiguiente();

                if (aux.getSiguiente() != null) aux.getSiguiente().setAnterior(aux.getAnterior());
                else ultimo = aux.getAnterior();

                aux.setSiguiente(null);
                aux.setAnterior(null);
                tamano--;
                return true;
            }
            aux = aux.getSiguiente();
        }
        return false;
    }

    public int getTamano() { return tamano; }
}
