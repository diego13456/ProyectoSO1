package unimet.proyectoso1.estructuras;

public class Cola<T> extends ListaEnlazada<T> {
    private Nodo<T> frente;
    private Nodo<T> finalCola;
    private int tamano;

    public Cola() {
        this.frente = null;
        this.finalCola = null;
        this.tamano = 0;
    }

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            finalCola.setSiguiente(nuevo);
        }
        finalCola = nuevo;
        tamano++;
    }
    
     public void encolarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = nuevo;
            finalCola = nuevo;
        } else {
            nuevo.setSiguiente(frente);
            frente = nuevo;
        }
        tamano++;
    }

    public void insertar(T dato) {
        encolar(dato);
    }

    public T desencolar() {
        if (estaVacia()) return null; 

        T dato = frente.getDato();
        frente = frente.getSiguiente();
        
        if (frente == null) {
            finalCola = null; 
        }
        tamano--;
        return dato;
    }
     public boolean remover(T dato) {
        if (estaVacia()) return false;

        // Caso 1: Es el frente
        if (frente.getDato().equals(dato)) {
            desencolar();
            return true;
        }

        // Caso 2: Buscar en el resto
        Nodo<T> actual = frente;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getDato().equals(dato)) {
                Nodo<T> aEliminar = actual.getSiguiente();
                actual.setSiguiente(aEliminar.getSiguiente());
                
                // Si eliminamos el último, actualizamos finalCola
                if (aEliminar == finalCola) {
                    finalCola = actual;
                }
                tamano--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    public T verFrente() {
        if (estaVacia()) return null;
        return frente.getDato();
    }
    
    

    @Override 
    public boolean estaVacia() {
        return frente == null;
    }
    
    public int getTamano() {
        return tamano;
    }
    
    public T obtenerPorIndice(int indice) {
        if (indice < 0 || indice >= tamano) {
            return null;
        }
        
        Nodo<T> actual = frente;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getDato();
    }
}