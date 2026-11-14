package metodos;

import java.io.Serializable;

import entidades.Cliente;
import java.util.ArrayList;
import java.util.List;

/**
 * Lista Doblemente Enlazada para la gestión CRUD de Clientes.
 * Estructura de datos autosuficiente mediante nodos con punteros hacia adelante y hacia atrás.
 */
public class ListaClientes implements Serializable {
    private static final long serialVersionUID = 1L;

    // Nodo doblemente enlazado para Clientes
    public static class NodoCliente implements Serializable {
        private static final long serialVersionUID = 1L;
        private Cliente dato;
        private NodoCliente siguiente;
        private NodoCliente anterior;

        public NodoCliente(Cliente dato) {
            this.dato = dato;
            this.siguiente = null;
            this.anterior = null;
        }

        public Cliente getDato() { return dato; }
        public void setDato(Cliente dato) { this.dato = dato; }
        public NodoCliente getSiguiente() { return siguiente; }
        public void setSiguiente(NodoCliente siguiente) { this.siguiente = siguiente; }
        public NodoCliente getAnterior() { return anterior; }
        public void setAnterior(NodoCliente anterior) { this.anterior = anterior; }
    }

    private NodoCliente primero;
    private NodoCliente ultimo;
    private int tamano;

    public ListaClientes() {
        this.primero = null;
        this.ultimo = null;
        this.tamano = 0;
    }

    // --- CREATE (Insertar) ---

    public void insertarInicio(Cliente nuevo) {
        if (nuevo == null) return;
        NodoCliente nuevoNodo = new NodoCliente(nuevo);
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

    public void insertarFinal(Cliente nuevo) {
        if (nuevo == null) return;
        NodoCliente nuevoNodo = new NodoCliente(nuevo);
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

    public boolean insertarAntesDe(Cliente nuevo, String identificacionReferencia) {
        if (nuevo == null || identificacionReferencia == null || primero == null) return false;

        if (primero.getDato().getIdentificacion().equalsIgnoreCase(identificacionReferencia)) {
            insertarInicio(nuevo);
            return true;
        }

        NodoCliente sapo = primero;
        while (sapo != null && !sapo.getDato().getIdentificacion().equalsIgnoreCase(identificacionReferencia)) {
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) return false;

        NodoCliente nuevoNodo = new NodoCliente(nuevo);
        nuevoNodo.setAnterior(sapo.getAnterior());
        nuevoNodo.setSiguiente(sapo);
        sapo.getAnterior().setSiguiente(nuevoNodo);
        sapo.setAnterior(nuevoNodo);
        tamano++;
        return true;
    }

    public boolean insertarDespuesDe(Cliente nuevo, String identificacionReferencia) {
        if (nuevo == null || identificacionReferencia == null || primero == null) return false;

        if (ultimo.getDato().getIdentificacion().equalsIgnoreCase(identificacionReferencia)) {
            insertarFinal(nuevo);
            return true;
        }

        NodoCliente sapo = primero;
        while (sapo != null && !sapo.getDato().getIdentificacion().equalsIgnoreCase(identificacionReferencia)) {
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) return false;

        NodoCliente nuevoNodo = new NodoCliente(nuevo);
        nuevoNodo.setSiguiente(sapo.getSiguiente());
        nuevoNodo.setAnterior(sapo);
        sapo.getSiguiente().setAnterior(nuevoNodo);
        sapo.setSiguiente(nuevoNodo);
        tamano++;
        return true;
    }

    // --- READ (Buscar y Listar) ---

    public NodoCliente buscarNodoPorIdentificacion(String identificacion) {
        if (identificacion == null || primero == null) return null;
        String idBuscada = identificacion.trim();
        NodoCliente sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getIdentificacion().equalsIgnoreCase(idBuscada)) {
                return sapo;
            }
            sapo = sapo.getSiguiente();
        }
        return null;
    }

    public Cliente buscarPorIdentificacion(String identificacion) {
        NodoCliente nodo = buscarNodoPorIdentificacion(identificacion);
        return (nodo != null) ? nodo.getDato() : null;
    }

    public Cliente buscarPorCorreo(String correo) {
        if (correo == null || primero == null) return null;
        String correoBuscado = correo.trim().toLowerCase();
        NodoCliente sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getCorreo().equalsIgnoreCase(correoBuscado)) {
                return sapo.getDato();
            }
            sapo = sapo.getSiguiente();
        }
        return null;
    }

    public List<Cliente> listarATodos() {
        List<Cliente> lista = new ArrayList<>();
        NodoCliente sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null) {
                lista.add(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return lista;
    }

    // --- UPDATE (Actualizar) ---

    public boolean actualizarCliente(String identificacion, Cliente cActualizado) {
        if (identificacion == null || cActualizado == null) return false;
        NodoCliente nodo = buscarNodoPorIdentificacion(identificacion);
        if (nodo == null) return false;

        Cliente c = nodo.getDato();
        if (cActualizado.getNombre() != null && !cActualizado.getNombre().isEmpty()) c.setNombre(cActualizado.getNombre());
        if (cActualizado.getApellido() != null && !cActualizado.getApellido().isEmpty()) c.setApellido(cActualizado.getApellido());
        if (cActualizado.getTipo() != null && !cActualizado.getTipo().isEmpty()) c.setTipo(cActualizado.getTipo());
        if (cActualizado.getTelefono() != null && !cActualizado.getTelefono().isEmpty()) c.setTelefono(cActualizado.getTelefono());
        if (cActualizado.getCorreo() != null && !cActualizado.getCorreo().isEmpty()) c.setCorreo(cActualizado.getCorreo());
        if (cActualizado.getDireccion() != null && !cActualizado.getDireccion().isEmpty()) c.setDireccion(cActualizado.getDireccion());
        if (cActualizado.getContrasena() != null && !cActualizado.getContrasena().isEmpty()) c.setContrasena(cActualizado.getContrasena());
        if (cActualizado.getPlanActual() != null && !cActualizado.getPlanActual().isEmpty()) c.setPlanActual(cActualizado.getPlanActual());
        c.setActivo(cActualizado.isActivo());
        return true;
    }

    public boolean cambiarEstado(String identificacion, boolean nuevoEstado) {
        NodoCliente nodo = buscarNodoPorIdentificacion(identificacion);
        if (nodo != null) {
            nodo.getDato().setActivo(nuevoEstado);
            return true;
        }
        return false;
    }

    public List<Cliente> listarPorEstado(boolean activo) {
        List<Cliente> filtrados = new ArrayList<>();
        NodoCliente sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().isActivo() == activo) {
                filtrados.add(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return filtrados;
    }

    // --- DELETE (Eliminar) ---

    public boolean eliminarPorIdentificacion(String identificacion) {
        if (identificacion == null || primero == null) return false;
        NodoCliente nodoEliminar = buscarNodoPorIdentificacion(identificacion);
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

    // --- VALIDACIONES DE DUPLICADOS Y AUTENTICACIÓN ---

    public boolean existeIdentificacion(String identificacion) {
        return buscarPorIdentificacion(identificacion) != null;
    }

    public boolean existeCorreo(String correo) {
        return buscarPorCorreo(correo) != null;
    }

    public boolean existeIdentificacionEnOtro(String identificacion, String idActual) {
        NodoCliente sapo = primero;
        while (sapo != null) {
            Cliente c = sapo.getDato();
            if (c != null && !c.getIdentificacion().equalsIgnoreCase(idActual)
                    && c.getIdentificacion().equalsIgnoreCase(identificacion)) {
                return true;
            }
            sapo = sapo.getSiguiente();
        }
        return false;
    }

    public boolean existeCorreoEnOtro(String correo, String idActual) {
        NodoCliente sapo = primero;
        while (sapo != null) {
            Cliente c = sapo.getDato();
            if (c != null && !c.getIdentificacion().equalsIgnoreCase(idActual)
                    && c.getCorreo().equalsIgnoreCase(correo)) {
                return true;
            }
            sapo = sapo.getSiguiente();
        }
        return false;
    }

    public Cliente autenticar(String identificacion, String contrasena) {
        Cliente c = buscarPorIdentificacion(identificacion);
        if (c != null && c.getContrasena().equals(contrasena)) {
            return c;
        }
        return null;
    }

    // --- RECORRIDOS ---

    public void recorrer() {
        if (primero == null) {
            System.out.println("Lista de clientes vacía");
            return;
        }
        NodoCliente sapo = primero;
        while (sapo != null) {
            System.out.println(sapo.getDato());
            sapo = sapo.getSiguiente();
        }
    }

    public void recorrerHaciaAtras() {
        if (ultimo == null) {
            System.out.println("Lista de clientes vacía");
            return;
        }
        NodoCliente sapo = ultimo;
        while (sapo != null) {
            System.out.println(sapo.getDato());
            sapo = sapo.getAnterior();
        }
    }

    public int getTamano() { return tamano; }
    public boolean estaVacia() { return primero == null; }
    public NodoCliente getPrimero() { return primero; }
    public NodoCliente getUltimo() { return ultimo; }
}
