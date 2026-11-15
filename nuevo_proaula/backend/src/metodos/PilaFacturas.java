package metodos;

import java.io.Serializable;

import entidades.Factura;
import java.util.ArrayList;
import java.util.List;

/**
 * Pila (LIFO) especializada para Facturación.
 * Estructura de datos autosuficiente mediante nodos enlazados desde la cima.
 */
public class PilaFacturas implements Serializable {
    private static final long serialVersionUID = 1L;

    // Nodo simplemente enlazado para Pila de Facturas
    public static class NodoFactura implements Serializable {
        private static final long serialVersionUID = 1L;
        private Factura dato;
        private NodoFactura siguiente;

        public NodoFactura(Factura dato) {
            this.dato = dato;
            this.siguiente = null;
        }

        public Factura getDato() { return dato; }
        public void setDato(Factura dato) { this.dato = dato; }
        public NodoFactura getSiguiente() { return siguiente; }
        public void setSiguiente(NodoFactura siguiente) { this.siguiente = siguiente; }
    }

    private NodoFactura cima;
    private int tamano;

    public PilaFacturas() {
        this.cima = null;
        this.tamano = 0;
    }

    // Apilar (Push)
    public void apilar(Factura nueva) {
        if (nueva == null) return;
        NodoFactura nuevoNodo = new NodoFactura(nueva);
        if (cima == null) {
            cima = nuevoNodo;
        } else {
            nuevoNodo.setSiguiente(cima);
            cima = nuevoNodo;
        }
        tamano++;
    }

    // Desapilar (Pop)
    public Factura desapilar() {
        if (cima == null) return null;
        NodoFactura aux = cima;
        cima = cima.getSiguiente();
        aux.setSiguiente(null);
        tamano--;
        return aux.getDato();
    }

    // Consultar Cima (Peek)
    public Factura getCima() {
        return (cima != null) ? cima.getDato() : null;
    }

    // Búsqueda LIFO: desapila hacia una pila auxiliar y luego restaura el orden original
    public Factura buscarPorId(int idFactura) {
        if (cima == null) return null;

        PilaFacturas aux = new PilaFacturas();
        Factura encontrada = null;

        while (!estaVacia()) {
            Factura actual = desapilar();
            aux.apilar(actual);
            if (actual != null && actual.getIdFactura() == idFactura) {
                encontrada = actual;
                break;
            }
        }

        while (!aux.estaVacia()) {
            apilar(aux.desapilar());
        }
        return encontrada;
    }

    // Búsqueda LIFO por usuario o cédula usando pila auxiliar
    public List<Factura> obtenerFacturasUsuario(String usuarioOId) {
        List<Factura> lista = new ArrayList<>();
        if (usuarioOId == null || cima == null) return lista;
        String criterio = usuarioOId.trim();

        PilaFacturas aux = new PilaFacturas();
        while (!estaVacia()) {
            Factura f = desapilar();
            aux.apilar(f);
            if (f != null) {
                boolean coincideUsuario = f.getUsuario() != null && f.getUsuario().equalsIgnoreCase(criterio);
                boolean coincideId = f.getIdentificacionCliente() != null && f.getIdentificacionCliente().equalsIgnoreCase(criterio);
                if (coincideUsuario || coincideId) {
                    lista.add(f);
                }
            }
        }

        while (!aux.estaVacia()) {
            apilar(aux.desapilar());
        }
        return lista;
    }

    // Búsqueda LIFO por usuario/cédula, mes y año usando pila auxiliar
    public Factura buscarFacturaMes(String usuarioOId, int mes, int ano) {
        if (usuarioOId == null || cima == null) return null;
        String criterio = usuarioOId.trim();
        String token = "-" + mes + "-" + ano;
        String tokenSlash = String.format("/%02d/%d", mes, ano);

        PilaFacturas aux = new PilaFacturas();
        Factura encontrada = null;

        while (!estaVacia()) {
            Factura f = desapilar();
            aux.apilar(f);
            if (f != null) {
                boolean coincideUsuario = f.getUsuario() != null && f.getUsuario().equalsIgnoreCase(criterio);
                boolean coincideId = f.getIdentificacionCliente() != null && f.getIdentificacionCliente().equalsIgnoreCase(criterio);
                if (coincideUsuario || coincideId) {
                    if ((f.getArchivoPDF() != null && f.getArchivoPDF().contains(token)) ||
                        (f.getFecha() != null && f.getFecha().contains(tokenSlash))) {
                        encontrada = f;
                        break;
                    }
                }
            }
        }

        while (!aux.estaVacia()) {
            apilar(aux.desapilar());
        }
        return encontrada;
    }

    public List<Factura> listarTodas() {
        List<Factura> lista = new ArrayList<>();
        NodoFactura sapo = cima;
        while (sapo != null) {
            if (sapo.getDato() != null) {
                lista.add(sapo.getDato());
            }
            sapo = sapo.getSiguiente();
        }
        return lista;
    }

    public boolean estaVacia() { return cima == null; }
    public int getTamano() { return tamano; }

    public void recorrer() {
        if (cima == null) {
            System.out.println("Pila de facturas vacía");
            return;
        }
        NodoFactura sapo = cima;
        while (sapo != null) {
            System.out.println(sapo.getDato());
            sapo = sapo.getSiguiente();
        }
    }
}
