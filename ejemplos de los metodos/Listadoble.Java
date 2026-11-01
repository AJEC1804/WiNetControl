Public class Listadoble {
    private Nodo primero;
    private Nodo ultimo;
        
    public Listadoble () {
        this.primero=null;
        this.ultimo=null;
    }
    
    public void insertar_inicio (Nodo nuevo) {
        if (primero==null && ultimo==null) {
            primero=nuevo;
            ultimo=nuevo;
        } else {
            nuevo.setSiguiente(primero);
            this.primero.sertAnterior(nuevo);
            this.primero=nuevo;
        }
    }
    
    public Nodo getprimero () {
        return primero;
    }
}