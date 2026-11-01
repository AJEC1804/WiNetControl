public class Lista {
    private Nodo primero;
    
    public Lista () {
        this.primero=null;
    }

    public void insertar_inicio (nodo nuevo) {
        if (primero==null) {
            primero=nuevo;
        } else {
            nuevo.setSiguiente(primero);
            this.primero=nuevo;
        }
    }

    public void insertar_antes_nodo_ref_simple (nodo nuevo , String ref) {
        if (primero==null) {
            System.out.println("lista vacia");
        } else {
            Nodo sapo = primero;
            Nodo sapa = primero;
            while (sapo!=null) {
                if (sapo.getDato().equals(ref)) {
                    break;
                }
                sapa=sapo;
                sapo=sapo.getSiguiente();
            }
            sapa=sapo;
            sapo=sapo.getSiguiente();
            if (sapo==null) {
                System.out.println("nodo no encontrado");
            } else {
                nuevo.setSiguiente(sapo);
                sapa.setSiguiente(nuevo);
            }
        }
    }

    public void insertar_despues_nodo_ref_simple (nodo nuevo , String ref) {
       if (primero==null) {
        System.out.println("lista vacia");
       } else {
        Nodo sapo = primero;
        while (sapo!=null){
            if (sapo.getDato().equals(ref)) {
                break;
            }
            sapo=sapo.getSiguiente();
        }
    }
        if (sapo==null) {
        System.out.println("nodo no encontrado");
    } else {
        nuevo.setSiguiente(sapo.getSiguiente());
        sapo.setSiguiente(nuevo);
    }



    public void recorrer () {
        if (this.primero==null) {
            System.out.println("lista vacia");
        }else {
            Nodo sapo = primero;
            while (sapo!=null) {
                System.out.println(sapo.getDato()+"]->");
                sapo=sapo.getSiguiente();
            }

        }
    }

    public void recorrerback () {}


    public void insertar_final (nodo nuevo) {
        if (primero==null) {
            this.primero=nuevo;
        } else {
                Nodo sapo = primero;
                while (sapo.getSiguiente()!=null) {
                    sapo=sapo.getSiguiente();
                }
                sapo.setSiguiente(nuevo);
        }
    }

    public void insertar_final_lista_doble(nodo nuevo) {
        if (this.primero==null && this.ultimo==null) {
            this.primero=nuevo;
            this.ultimo=nuevo;
        } else {
            this.ultimo.setSiguiente(nuevo);
            nuevo.setAnterior(this.ultimo);
            this.ultimo=nuevo;
        }
    }

    public void eliminar_inicio_lista_simple () {
        if (primero==null) {
            System.out.println("lista vacia");
        } else {
            Nodo sapo = primero;
            this.primero=this.primero.getSiguiente();
            sapo.setSiguiente(null);
            sapo=null;
            }
        }
    }
        public void eliminar_antes_nodo_ref_simple (nodo nuevo , String ref) {
        if (primero==null) {
            System.out.println("lista vacia");
        } else {
            Nodo sapo = primero;
            Nodo sapa = primero;
            nodo sapito = primero;
            while (sapo!=null) {
                if (sapo.getDato().equals(ref)) {
                    break;
                }
                sapito=sapa;
                sapa=sapo;
                sapo=sapo.getSiguiente();
            }
            sapa=sapo;
            sapo=sapo.getSiguiente();
            if (sapo==null) {
                System.out.println("nodo no encontrado");
            } else {
                sapito.setSiguiente(sapo);
                sapa.setSiguiente(null);
                sapa=null;
            }
        }
    }

    public void eliminar_nodo_referente_simple (String ref) {
        if (primero==null) {
            System.out.println("lista vacia");
        } else {
            Nodo sapo = primero;
            Nodo sapa = primero;
            while (sapo!=null) {
                if (sapo.getDato().equals(ref)) {
                    break;
                }
                sapa=sapo;
                sapo=sapo.getSiguiente();
            }
            sapa=sapo;
            sapo=sapo.getSiguiente();
            if (sapo==null) {
                System.out.println("nodo no encontrado");
            } else {
                sapa.setSiguiente(sapo.getSiguiente());
                sapo.setSiguiente(null);
                sapo=null;
            }
        }
    }
public void eliminar_inicio_lista_doble () {
            if (this.primero==null && this.ultimo==null) {
                System.out.println("lista vacia");
            } else {
                if (this.primero==this.ultimo) {
                    this.primero=null;
                    this.ultimo=null;
                } else {
                    Nodo sapo = this.primero;
                    this.primero=this.primero.getSiguiente();
                    this.primero.setSiguiente(null);
                    sapo.setSiguiente(null);
                    this.primero.setAnterior(null);
                    sapo=null;
                }
            }
        
        }

 public void eliminar_final_lista_simple () {
    if (primero==null) {
        System.out.println("lista vacia");  
        }else {
            Nodo sapo = primero;
            while (sapo.getSiguiente().getSiguiente()!=null) {
                sapo=sapo.getSiguiente();
            }
            Nodo ultimo = sapo.getSiguiente();
            sapo.setSiguiente(null);
            ultimo=null;
        }

   }

    public void eliminar_antes_nodo_ref_doble (nodo nuevo , String ref) {
        if (primero==null) {
            System.out.println("lista vacia");
        } else {
            Nodo sapo = primero;
            Nodo sapa = primero;
            
            while (sapo!=null) {
                if (sapo.getDato().equals(ref)) {
                    break;
                }
                sapa=sapo;
                sapo=sapo.getSiguiente();
            }
            sapa=sapo;
            sapo=sapo.getSiguiente();
            if (sapo==null) {
                System.out.println("nodo no encontrado");
            } else {
                Nodo sapa = sapo.getAnterior();
                sapa.getAnterior().setSiguiente(sapo);
                sapo.setAnterior(sapa.getAnterior());
                sapa.setSiguiente(null);
                sapa.setAnterior(null);
                sapa = null;
            }
        }
    }

    public void eliminar_nodo_referente_doble (String ref) {
        if (primero==null) {
            System.out.println("lista vacia");
        } else {
            Nodo sapo = primero;
            Nodo sapa = primero;
            while (sapo!=null) {
                if (sapo.getDato().equals(ref)) {
                    break;
                }
                sapa=sapo;
                sapo=sapo.getSiguiente();
            }
            sapa=sapo;
            sapo=sapo.getSiguiente();
            if (sapo==null) {
                System.out.println("nodo no encontrado");
            } else {
                sapa.setSiguiente(sapo.getSiguiente());
                sapo.setSiguiente(null);
                sapo=null;
            }
        }
    }

    public void insertar_antes_nodo_ref_doble (nodo nuevo , String ref) {
        if (primero==null) {
            System.out.println("lista vacia");
        } else {
            Nodo sapo = primero;
            while (sapo!=null) {
                if (sapo.getDato().equals(ref)) {
                    break;
                }
                sapa=sapo;
                sapo=sapo.getSiguiente();
            }
            sapo=sapo.getSiguiente();
            if (sapo==null) {
                System.out.println("nodo no encontrado");
            } else {
              nuevo.setanterior(sapo.getAnterior());
              nuevo.setSiguiente(sapo);
                sapo.getAnterior().setSiguiente(nuevo);
                sapo.setAnterior(nuevo);
            }
        }
    }

    public void nodo_referente_doble (String ref) {
        if (primero==null) {
            System.out.println("lista vacia");
        } else {
            Nodo sapo = primero;
            while (sapo!=null) {
                if (sapo.getDato().equals(ref)) {
                    break;
                }
                sapo=sapo.getSiguiente();
            }
            if (sapo==null) {
                System.out.println("nodo no encontrado");
            } else {
                sapo.getAnterior().setSiguiente(sapo.getSiguiente());
                sapo.getSiguiente().setAnterior(sapo.getAnterior());
                sapo.setSiguiente(null);
                sapo.setAnterior(null);
                sapo=null;
            }
        }
    }



public Nodo getprimero () {
    return primero;
}

public void setprimero (nodo primero) {
    this.primero = primero;
}

}