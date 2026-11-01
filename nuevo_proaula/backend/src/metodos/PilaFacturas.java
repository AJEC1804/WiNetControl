package metodos;

import entidades.Factura;
import java.util.ArrayList;
import java.util.List;

/**
 * Pila (LIFO) especializada para Facturación.
 * Estructura de datos autosuficiente mediante nodos enlazados desde la cima.
 */
public class PilaFacturas {

    // Nodo simplemente enlazado para Pila de Facturas
    public static class NodoFactura {
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

    public Factura buscarPorId(int idFactura) {
        NodoFactura sapo = cima;
        while (sapo != null) {
            if (sapo.getDato() != null && sapo.getDato().getIdFactura() == idFactura) {
                return sapo.getDato();
            }
            sapo = sapo.getSiguiente();
        }
        return null;
    }

    public List<Factura> obtenerFacturasUsuario(String usuarioOId) {
        List<Factura> lista = new ArrayList<>();
        if (usuarioOId == null || cima == null) return lista;
        String criterio = usuarioOId.trim();

        NodoFactura sapo = cima;
        while (sapo != null) {
            Factura f = sapo.getDato();
            if (f != null) {
                boolean coincideUsuario = f.getUsuario() != null && f.getUsuario().equalsIgnoreCase(criterio);
                boolean coincideId = f.getIdentificacionCliente() != null && f.getIdentificacionCliente().equalsIgnoreCase(criterio);
                if (coincideUsuario || coincideId) {
                    lista.add(f);
                }
            }
            sapo = sapo.getSiguiente();
        }
        return lista;
    }

    public Factura buscarFacturaMes(String usuario, int mes, int ano) {
        if (usuario == null || cima == null) return null;
        String token = "-" + mes + "-" + ano;
        String tokenSlash = String.format("/%02d/%d", mes, ano);

        NodoFactura sapo = cima;
        while (sapo != null) {
            Factura f = sapo.getDato();
            if (f != null && f.getUsuario() != null && f.getUsuario().equalsIgnoreCase(usuario)) {
                if ((f.getArchivoPDF() != null && f.getArchivoPDF().contains(token)) ||
                    (f.getFecha() != null && f.getFecha().contains(tokenSlash))) {
                    return f;
                }
            }
            sapo = sapo.getSiguiente();
        }
        return null;
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
