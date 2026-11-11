package metodos;

import entidades.Empleado;
import java.util.ArrayList;
import java.util.List;

/**
 * Lista Doblemente Enlazada para la gestión CRUD de Empleados.
 * Estructura de datos autosuficiente mediante nodos con punteros hacia adelante y hacia atrás.
 * (Misma estructura y mismo estilo que ListaClientes y ListaPlanes.)
 */
public class ListaEmpleados {

    // Nodo doblemente enlazado para Empleados
    public static class NodoEmpleado {
        private Empleado dato;
        private NodoEmpleado siguiente;
        private NodoEmpleado anterior;

        public NodoEmpleado(Empleado dato) {
            this.dato = dato;
            this.siguiente = null;
            this.anterior = null;
        }

        public Empleado getDato() { return dato; }
        public void setDato(Empleado dato) { this.dato = dato; }
        public NodoEmpleado getSiguiente() { return siguiente; }
        public void setSiguiente(NodoEmpleado siguiente) { this.siguiente = siguiente; }
        public NodoEmpleado getAnterior() { return anterior; }
        public void setAnterior(NodoEmpleado anterior) { this.anterior = anterior; }
    }

    private NodoEmpleado primero;
    private NodoEmpleado ultimo;
    private int tamano;

    public ListaEmpleados() {
        this.primero = null;
        this.ultimo = null;
        this.tamano = 0;
    }

    // --- CREATE (Insertar) ---

    public void insertarInicio(Empleado nuevo) {
        if (nuevo == null) return;
        NodoEmpleado nuevoNodo = new NodoEmpleado(nuevo);
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

    public void insertarFinal(Empleado nuevo) {
        if (nuevo == null) return;
        NodoEmpleado nuevoNodo = new NodoEmpleado(nuevo);
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

    public boolean insertarAntesDe(Empleado nuevo, String identificacionReferencia) {
        if (nuevo == null || identificacionReferencia == null || primero == null) return false;

        if (primero.getDato().getIdentificacion().equalsIgnoreCase(identificacionReferencia)) {
            insertarInicio(nuevo);
            return true;
        }

        NodoEmpleado sapo = primero;
        while (sapo != null && !sapo.getDato().getIdentificacion().equalsIgnoreCase(identificacionReferencia)) {
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) return false;

        NodoEmpleado nuevoNodo = new NodoEmpleado(nuevo);
        nuevoNodo.setAnterior(sapo.getAnterior());
        nuevoNodo.setSiguiente(sapo);
        sapo.getAnterior().setSiguiente(nuevoNodo);
        sapo.setAnterior(nuevoNodo);
        tamano++;
        return true;
    }

    public boolean insertarDespuesDe(Empleado nuevo, String identificacionReferencia) {
        if (nuevo == null || identificacionReferencia == null || primero == null) return false;

        NodoEmpleado sapo = primero;
        while (sapo != null && !sapo.getDato().getIdentificacion().equalsIgnoreCase(identificacionReferencia)) {
            sapo = sapo.getSiguiente();
        }

        if (sapo == null) return false;

        NodoEmpleado nuevoNodo = new NodoEmpleado(nuevo);
        nuevoNodo.setSiguiente(sapo.getSiguiente());
        nuevoNodo.setAnterior(sapo);

        if (sapo.getSiguiente() != null) {
            sapo.getSiguiente().setAnterior(nuevoNodo);
        } else {
            ultimo = nuevoNodo;
        }

        sapo.setSiguiente(nuevoNodo);
        tamano++;
        return true;
    }

    // --- READ (Buscar y Listar) ---

    public NodoEmpleado buscarNodoPorIdentificacion(String identificacion) {
        if (identificacion == null || primero == null) return null;
        String idBuscada = identificacion.trim();
        NodoEmpleado sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getIdentificacion().equalsIgnoreCase(idBuscada)) {
                return sapo;
            }
            sapo = sapo.getSiguiente();
        }
        return null;
    }

    public Empleado buscarPorIdentificacion(String identificacion) {
        NodoEmpleado nodo = buscarNodoPorIdentificacion(identificacion);
        return (nodo != null) ? nodo.getDato() : null;
    }

    public Empleado buscarPorCorreo(String correo) {
        if (correo == null || primero == null) return null;
        String correoBuscado = correo.trim().toLowerCase();
        NodoEmpleado sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getCorreo().equalsIgnoreCase(correoBuscado)) {
                return sapo.getDato();
            }
            sapo = sapo.getSiguiente();
        }
        return null;
    }

    /**
     * Búsqueda parcial: recorre la lista y devuelve los empleados cuya
     * identificación O nombre completo contenga el texto buscado.
     * Ejemplo: "lucia" encuentra a "Lucia Ramirez"; "1020" encuentra a "1020304050".
     */
    public ListaEmpleados buscarPorIdentificacionParcial(String fragmento) {
        ListaEmpleados encontrados = new ListaEmpleados();
        if (fragmento == null || primero == null) return encontrados;
        String buscado = fragmento.trim().toLowerCase();
        NodoEmpleado sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && (sapo.getDato().getIdentificacion().contains(buscado)
                    || (sapo.getDato().getNombre() + " " + sapo.getDato().getApellido()).toLowerCase().contains(buscado))) {
                encontrados.insertarFinal(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return encontrados;
    }

    public List<Empleado> listarATodos() {
        List<Empleado> lista = new ArrayList<>();
        NodoEmpleado sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null) {
                lista.add(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return lista;
    }

    public List<Empleado> listarPorEstado(boolean activo) {
        List<Empleado> lista = new ArrayList<>();
        NodoEmpleado sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().isActivo() == activo) {
                lista.add(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return lista;
    }

    public List<Empleado> listarPorSucursal(String sucursal) {
        List<Empleado> lista = new ArrayList<>();
        if (sucursal == null || primero == null) return lista;

        String buscada = sucursal.trim();
        NodoEmpleado sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getSucursal().equalsIgnoreCase(buscada)) {
                lista.add(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return lista;
    }

    /** Recorrido hacia atrás: demuestra que la lista es doblemente enlazada. */
    public List<Empleado> listarAlReves() {
        List<Empleado> lista = new ArrayList<>();
        NodoEmpleado sapo = ultimo;
        while (sapo != null) {
            if (sapo.getDato() != null) {
                lista.add(sapo.getDato());
            }
            sapo = sapo.getAnterior();
        }
        return lista;
    }

    // --- UPDATE (Actualizar y cambiar estado) ---

    public boolean actualizarEmpleado(String identificacion, Empleado eActualizado) {
        if (identificacion == null || eActualizado == null) return false;
        NodoEmpleado nodo = buscarNodoPorIdentificacion(identificacion);
        if (nodo == null) return false;

        Empleado e = nodo.getDato();
        if (eActualizado.getNombre() != null && !eActualizado.getNombre().isEmpty()) e.setNombre(eActualizado.getNombre());
        if (eActualizado.getApellido() != null && !eActualizado.getApellido().isEmpty()) e.setApellido(eActualizado.getApellido());
        if (eActualizado.getTipo() != null && !eActualizado.getTipo().isEmpty()) e.setTipo(eActualizado.getTipo());
        if (eActualizado.getTelefono() != null && !eActualizado.getTelefono().isEmpty()) e.setTelefono(eActualizado.getTelefono());
        if (eActualizado.getCorreo() != null && !eActualizado.getCorreo().isEmpty()) e.setCorreo(eActualizado.getCorreo());
        if (eActualizado.getDireccion() != null && !eActualizado.getDireccion().isEmpty()) e.setDireccion(eActualizado.getDireccion());
        if (eActualizado.getCargo() != null && !eActualizado.getCargo().isEmpty()) e.setCargo(eActualizado.getCargo());
        if (eActualizado.getSucursal() != null && !eActualizado.getSucursal().isEmpty()) e.setSucursal(eActualizado.getSucursal());
        if (eActualizado.getRol() != null && !eActualizado.getRol().isEmpty()) e.setRol(eActualizado.getRol());
        e.setActivo(eActualizado.isActivo());
        return true;
    }

    public boolean cambiarRol(String identificacion, String nuevoRol) {
        if (identificacion == null || nuevoRol == null || primero == null) return false;
        String idBuscada = identificacion.trim();
        NodoEmpleado sapo = primero;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getIdentificacion().equalsIgnoreCase(idBuscada)) {
                sapo.getDato().setRol(nuevoRol.trim().toUpperCase());
                return true;
            }
            sapo = sapo.getSiguiente();
        }
        return false;
    }

    public boolean cambiarEstado(String identificacion, boolean nuevoEstado) {
        NodoEmpleado nodo = buscarNodoPorIdentificacion(identificacion);
        if (nodo != null) {
            nodo.getDato().setActivo(nuevoEstado);
            return true;
        }
        return false;
    }

    // --- DELETE (Eliminar) ---

    public boolean eliminarEmpleado(String identificacion) {
        if (identificacion == null || primero == null) return false;

        NodoEmpleado sapo = buscarNodoPorIdentificacion(identificacion);
        if (sapo == null) return false;

        if (sapo.getAnterior() != null) {
            sapo.getAnterior().setSiguiente(sapo.getSiguiente());
        } else {
            primero = sapo.getSiguiente();
        }

        if (sapo.getSiguiente() != null) {
            sapo.getSiguiente().setAnterior(sapo.getAnterior());
        } else {
            ultimo = sapo.getAnterior();
        }

        sapo.setSiguiente(null);
        sapo.setAnterior(null);
        tamano--;
        return true;
    }

    public NodoEmpleado getPrimero() {
        return primero;
    }

    public NodoEmpleado getUltimo() {
        return ultimo;
    }

    public int getTamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return primero == null;
    }
}
