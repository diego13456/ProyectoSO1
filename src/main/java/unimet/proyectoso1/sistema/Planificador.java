package unimet.proyectoso1.sistema;

import unimet.proyectoso1.modelo.EstadoProceso;
import unimet.proyectoso1.modelo.PCB;
import unimet.proyectoso1.gui.VentanaPrincipal;

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

        int tamano = Nucleo.colaListos.getTamano();
        for (int i = 0; i < tamano; i++) {
            PCB p = Nucleo.colaListos.desencolar();
            p.decrementarDeadline();
            
            if (p.getDeadline() <= 0) {
                marcarComoFallido(p);
            } else {
                Nucleo.colaListos.encolar(p); 
            }
        }
    }

    private static void marcarComoFallido(PCB p) {
        p.setEstado(EstadoProceso.TERMINADO); 
        Nucleo.colaTerminados.encolar(p);
        System.out.println("[ALERTA] Misión Fallida (Deadline): " + p.getNombre());
    }

    public static void planificarCortoPlazo(String algoritmo) {
        if (Nucleo.colaListos.estaVacia() && Nucleo.procesoEnEjecucion == null) {
            return;
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