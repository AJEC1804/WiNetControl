public class Listas_simplemente_Enlazadas {
    public static void main(String[] args) {
        Lista lista = new Lista();
        



        lista.insertar_inicio(new Nodo("koko"));
        lista.insertar_inicio(new Nodo("pacha"));
        lista.insertar_inicio(new Nodo("kiko"));
        lista.insertar_inicio(new Nodo("popi"));
        lista.insertar_inicio(new Nodo("kuku"));
        lista.insertar_inicio(new Nodo("kaka"));

        lista.insertar_antes_nodo_ref_simple(new Nodo("kiki"), "kiko");
        lista.insertar_despues_nodo_ref_simple(new Nodo("kuku"), "koko");

        lista.recorrer();

        System.out.println("" + lista.getprimero().getDato());
        System.out.println("" + lista.getprimero().getSiguiente());
    }
}