
package unimet.proyectoso1.estructuras;

public class Cola<T> {
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

    public T verFrente() {
        if (estaVacia()) return null;
        return frente.getDato();
    }

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
