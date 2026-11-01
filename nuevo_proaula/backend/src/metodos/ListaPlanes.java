package metodos;

import entidades.Plan;
import java.util.ArrayList;
import java.util.List;

/**
 * Lista Doblemente Enlazada para la gestión CRUD de Planes de Internet.
 * Estructura de datos autosuficiente mediante nodos con punteros hacia adelante y hacia atrás.
 */
public class ListaPlanes {

    // Nodo doblemente enlazado para Planes
    public static class NodoPlan {
        private Plan dato;
        private NodoPlan siguiente;
        private NodoPlan anterior;

        public NodoPlan(Plan dato) {
            this.dato = dato;
            this.siguiente = null;
            this.anterior = null;
        }

        public Plan getDato() { return dato; }
        public void setDato(Plan dato) { this.dato = dato; }
        public NodoPlan getSiguiente() { return siguiente; }
        public void setSiguiente(NodoPlan siguiente) { this.siguiente = siguiente; }
        public NodoPlan getAnterior() { return anterior; }
        public void setAnterior(NodoPlan anterior) { this.anterior = anterior; }
    }

    private NodoPlan primero;
    private NodoPlan ultimo;
    private int tamano;

    public ListaPlanes() {
        this.primero = null;
        this.ultimo = null;
        this.tamano = 0;
    }

    // --- CREATE (Insertar) ---

    public void insertarInicio(Plan nuevo) {
        if (nuevo == null) return;
        NodoPlan nuevoNodo = new NodoPlan(nuevo);
        if (primero == null && ultimo == null) {
            primero = nuevoNodo;
            ultimo = nuevoNodo;
        } else {
            nuevoNodo.setSiguiente(primero);
            primero.setAnterior(nuevoNodo);
            primero = nuevoNodo;
        }
        tamano++;
    }

    public void insertarFinal(Plan nuevo) {
        if (nuevo == null) return;
        NodoPlan nuevoNodo = new NodoPlan(nuevo);
        if (primero == null && ultimo == null) {
            primero = nuevoNodo;
            ultimo = nuevoNodo;
        } else {
            ultimo.setSiguiente(nuevoNodo);
            nuevoNodo.setAnterior(ultimo);
            ultimo = nuevoNodo;
        }
        tamano++;
    }

    // --- READ (Buscar y Listar) ---

    public NodoPlan buscarNodoPorId(int idPlan) {
        NodoPlan sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getIdPlan() == idPlan) {
                return sapo;
            }
            sapo = sapo.getSiguiente();
        }
        return null;
    }

    public Plan buscarPorId(int idPlan) {
        NodoPlan nodo = buscarNodoPorId(idPlan);
        return (nodo != null) ? nodo.getDato() : null;
    }

    public Plan buscarPorNombre(String nombrePlan) {
        if (nombrePlan == null || primero == null) return null;
        String nombreBuscado = nombrePlan.trim();
        NodoPlan sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getNombrePlan().equalsIgnoreCase(nombreBuscado)) {
                return sapo.getDato();
            }
            sapo = sapo.getSiguiente();
        }
        return null;
    }

    public List<Plan> listarATodos() {
        List<Plan> lista = new ArrayList<>();
        NodoPlan sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null) {
                lista.add(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return lista;
    }

    public Plan obtenerPrimerPlanDisponible() {
        NodoPlan sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().isActivo()) {
                return sapo.getDato();
            }
            sapo = sapo.getSiguiente();
        }
        return (primero != null) ? primero.getDato() : null;
    }

    public boolean cambiarEstado(int idPlan, boolean nuevoEstado) {
        NodoPlan nodo = buscarNodoPorId(idPlan);
        if (nodo != null) {
            nodo.getDato().setActivo(nuevoEstado);
            return true;
        }
        return false;
    }

    public List<Plan> listarPorEstado(boolean activo) {
        List<Plan> filtrados = new ArrayList<>();
        NodoPlan sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().isActivo() == activo) {
                filtrados.add(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return filtrados;
    }

    // --- UPDATE (Actualizar) ---

    public boolean actualizarPlan(int idPlan, Plan planActualizado) {
        if (planActualizado == null) return false;
        NodoPlan nodo = buscarNodoPorId(idPlan);
        if (nodo == null) return false;

        Plan p = nodo.getDato();
        if (planActualizado.getIdPlan() > 0) p.setIdPlan(planActualizado.getIdPlan());
        if (planActualizado.getNombrePlan() != null && !planActualizado.getNombrePlan().isEmpty()) p.setNombrePlan(planActualizado.getNombrePlan());
        if (planActualizado.getVelocidadPlan() != null && !planActualizado.getVelocidadPlan().isEmpty()) p.setVelocidadPlan(planActualizado.getVelocidadPlan());
        if (planActualizado.getPrecio() >= 0) p.setPrecio(planActualizado.getPrecio());
        if (planActualizado.getDescripcionPlan() != null && !planActualizado.getDescripcionPlan().isEmpty()) p.setDescripcionPlan(planActualizado.getDescripcionPlan());
        if (planActualizado.getFechaActivacion() != null && !planActualizado.getFechaActivacion().isEmpty()) p.setFechaActivacion(planActualizado.getFechaActivacion());
        p.setActivo(planActualizado.isActivo());
        return true;
    }

    // --- DELETE (Eliminar) ---

    public boolean eliminarPorId(int idPlan) {
        NodoPlan nodoEliminar = buscarNodoPorId(idPlan);
        if (nodoEliminar == null) return false;

        if (nodoEliminar == primero && nodoEliminar == ultimo) {
            primero = null;
            ultimo = null;
        } else if (nodoEliminar == primero) {
            primero = primero.getSiguiente();
            primero.setAnterior(null);
            nodoEliminar.setSiguiente(null);
        } else if (nodoEliminar == ultimo) {
            ultimo = ultimo.getAnterior();
            ultimo.setSiguiente(null);
            nodoEliminar.setAnterior(null);
        } else {
            nodoEliminar.getAnterior().setSiguiente(nodoEliminar.getSiguiente());
            nodoEliminar.getSiguiente().setAnterior(nodoEliminar.getAnterior());
            nodoEliminar.setSiguiente(null);
            nodoEliminar.setAnterior(null);
        }
        tamano--;
        return true;
    }

    // --- VALIDACIONES DE DUPLICADOS ---

    public boolean existeIdPlan(int idPlan) {
        return buscarPorId(idPlan) != null;
    }

    public boolean existeIdPlanEnOtro(int idNuevo, int idActual) {
        NodoPlan sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getIdPlan() == idNuevo && sapo.getDato().getIdPlan() != idActual) {
                return true;
            }
            sapo = sapo.getSiguiente();
        }
        return false;
    }

    // --- RECORRIDOS ---

    public void recorrer() {
        if (primero == null) {
            System.out.println("Lista de planes vacía");
            return;
        }
        NodoPlan sapo = primero;
        while (sapo != null) {
            System.out.println(sapo.getDato());
            sapo = sapo.getSiguiente();
        }
    }

    public void recorrerHaciaAtras() {
        if (ultimo == null) {
            System.out.println("Lista de planes vacía");
            return;
        }
        NodoPlan sapo = ultimo;
        while (sapo != null) {
            System.out.println(sapo.getDato());
            sapo = sapo.getAnterior();
        }
    }

    public int getTamano() { return tamano; }
    public boolean estaVacia() { return primero == null; }
    public NodoPlan getPrimero() { return primero; }
    public NodoPlan getUltimo() { return ultimo; }
}
