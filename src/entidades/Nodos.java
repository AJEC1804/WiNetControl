package entidades;

import model.cliente;
import model.Planes;
import model.soporte;
import model.factura;

/**
 *
 * @author Gyanela Meza
 */
public class Nodos {

    // Nodo para la lista de clientes
    public static class NodoCliente {
        private cliente dato;
        private NodoCliente siguiente;
        private NodoCliente anterior;

        public NodoCliente(cliente dato) {
            this.dato = dato;
            this.siguiente = null;
            this.anterior = null;
        }

        public cliente getDato() {
            return dato;
        }

        public void setDato(cliente dato) {
            this.dato = dato;
        }

        public NodoCliente getSiguiente() {
            return siguiente;
        }

        public void setSiguiente(NodoCliente siguiente) {
            this.siguiente = siguiente;
        }

        public NodoCliente getAnterior() {
            return anterior;
        }

        public void setAnterior(NodoCliente anterior) {
            this.anterior = anterior;
        }
    }

    // Nodo para la lista de planes
    public static class NodoPlan {
        private Planes dato;
        private NodoPlan siguiente;
        private NodoPlan anterior;

        public NodoPlan(Planes dato) {
            this.dato = dato;
            this.siguiente = null;
            this.anterior = null;
        }

        public Planes getDato() {
            return dato;
        }

        public void setDato(Planes dato) {
            this.dato = dato;
        }

        public NodoPlan getSiguiente() {
            return siguiente;
        }

        public void setSiguiente(NodoPlan siguiente) {
            this.siguiente = siguiente;
        }

        public NodoPlan getAnterior() {
            return anterior;
        }

        public void setAnterior(NodoPlan anterior) {
            this.anterior = anterior;
        }
    }

    // Nodo para la cola de soporte
    public static class NodoSoporte {
        private soporte dato;
        private NodoSoporte siguiente;
        private NodoSoporte anterior;

        public NodoSoporte(soporte dato) {
            this.dato = dato;
            this.siguiente = null;
            this.anterior = null;
        }

        public soporte getDato() {
            return dato;
        }

        public void setDato(soporte dato) {
            this.dato = dato;
        }

        public NodoSoporte getSiguiente() {
            return siguiente;
        }

        public void setSiguiente(NodoSoporte siguiente) {
            this.siguiente = siguiente;
        }

        public NodoSoporte getAnterior() {
            return anterior;
        }

        public void setAnterior(NodoSoporte anterior) {
            this.anterior = anterior;
        }
    }

    // Nodo para la pila de facturas
    public static class NodoFactura {
        private factura dato;
        private NodoFactura siguiente;

        public NodoFactura(factura dato) {
            this.dato = dato;
            this.siguiente = null;
        }

        public factura getDato() {
            return dato;
        }

        public void setDato(factura dato) {
            this.dato = dato;
        }

        public NodoFactura getSiguiente() {
            return siguiente;
        }

        public void setSiguiente(NodoFactura siguiente) {
            this.siguiente = siguiente;
        }
    }
}
