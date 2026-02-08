package unimet.proyectoso1.sistema;

import unimet.proyectoso1.modelo.EstadoProceso;
import unimet.proyectoso1.modelo.PCB;
import unimet.proyectoso1.gui.VentanaPrincipal;
import unimet.proyectoso1.estructuras.Cola;

public class Planificador {

    public static void planificarLargoPlazo() {
        while (!Nucleo.colaNuevos.estaVacia() && Nucleo.colaListos.getTamano() < Nucleo.LIMITE_MEMORIA_RAM) {
            PCB proceso = Nucleo.colaNuevos.desencolar();
            proceso.setEstado(EstadoProceso.LISTO);
            Nucleo.colaListos.encolar(proceso);
            System.out.println("[PLANIFICADOR] Admitido en RAM: " + proceso.getNombre());
        }
    }

    public static void verificarDeadlines() {
        if (Nucleo.procesoEnEjecucion != null) {
            Nucleo.procesoEnEjecucion.decrementarDeadline(); 
            if (Nucleo.procesoEnEjecucion.getDeadline() <= 0) {
                marcarComoFallido(Nucleo.procesoEnEjecucion);
                Nucleo.procesoEnEjecucion = null;
            }
        }
        procesarColaDeadlines(Nucleo.colaListos);

        procesarColaDeadlines(Nucleo.colaNuevos);

    }
    
        private static void procesarColaDeadlines(Cola<PCB> cola) {
    int tamano = cola.getTamano();
    for (int i = 0; i < tamano; i++) {
        PCB p = cola.desencolar();
        
        // 1. PRIMERO RESTAR: Así permitimos que el 1 pase a ser 0
        p.decrementarDeadline(); 
        
        // 2. LUEGO VERIFICAR: Si es 0 o menor, ha fallado
        if (p.getDeadline() <= 0) { 
            // Esto genera el log: "Fallo de Deadline en Proceso Y" 
            marcarComoFallido(p); 
        } else {
            // Si aún le queda tiempo, vuelve a la cola
            cola.encolar(p); 
        }
    }
}

    private static void marcarComoFallido(PCB p) {
    p.setEstado(EstadoProceso.TERMINADO); 
    Nucleo.colaTerminados.encolar(p);
    
    Nucleo.misionesFallidas++;
    
    System.out.println("[ALERTA] Fallo de Deadline en Proceso " + p.getNombre());
}

    public static void planificarCortoPlazo(String algoritmo) {
        if (Nucleo.colaListos.estaVacia() && Nucleo.procesoEnEjecucion == null) {
            return;
        }
        if (Nucleo.procesoEnEjecucion == null && !Nucleo.colaListos.estaVacia()) {
        asignarCPU();
    }

        switch (algoritmo.toUpperCase()) {
            case "FCFS":
                ejecutarFCFS();
                break;
            case "SRT":
                ejecutarSRT();
                break;
            default:
                ejecutarFCFS();
                break;
        }
    }

    private static void ejecutarFCFS() {
        if (Nucleo.procesoEnEjecucion == null && !Nucleo.colaListos.estaVacia()) {
            asignarCPU();
        }
    }

    private static void ejecutarSRT() {
        if (!Nucleo.colaListos.estaVacia()) {
            PCB masCortoEnCola = buscarMasCortoEnListos();
            
            if (Nucleo.procesoEnEjecucion != null) {
                int restanteActual = Nucleo.procesoEnEjecucion.getInstruccionesTotales() - Nucleo.procesoEnEjecucion.getProgramCounter();
                int restanteNuevo = masCortoEnCola.getInstruccionesTotales() - masCortoEnCola.getProgramCounter();

                if (restanteNuevo < restanteActual) {
                    System.out.println("[SRT] Preempción: " + masCortoEnCola.getNombre() + " expulsa a " + Nucleo.procesoEnEjecucion.getNombre());
                    Nucleo.procesoEnEjecucion.setEstado(EstadoProceso.LISTO);
                    Nucleo.colaListos.encolar(Nucleo.procesoEnEjecucion);
                    Nucleo.procesoEnEjecucion = null;
                }
            }
            
            if (Nucleo.procesoEnEjecucion == null) {
                asignarCPU();
            }
        }
    }

    private static void asignarCPU() {
        PCB siguiente = Nucleo.colaListos.desencolar();
        siguiente.setEstado(EstadoProceso.EJECUCION);
        Nucleo.procesoEnEjecucion = siguiente;
        System.out.println("[DISPATCHER] CPU asignada a: " + siguiente.getNombre());
    }

    private static PCB buscarMasCortoEnListos() {
        return Nucleo.colaListos.verFrente(); 
    }
}