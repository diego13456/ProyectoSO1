package unimet.proyectoso1.sistema;

import unimet.proyectoso1.modelo.EstadoProceso;
import unimet.proyectoso1.modelo.PCB;
import unimet.proyectoso1.estructuras.Cola;

public class Planificador {

    public static void planificarLargoPlazo() {
        while (!Nucleo.colaNuevos.estaVacia()) {
            PCB p = Nucleo.colaNuevos.desencolar();
            if (getProcesosEnRAM() < Nucleo.LIMITE_MEMORIA_RAM) {
                p.setEstado(EstadoProceso.LISTO);
                Nucleo.colaListos.encolar(p);
            } else {
                p.setEstado(EstadoProceso.LISTO_SUSPENDIDO);
                Nucleo.colaReadySuspended.encolar(p);
                System.out.println("[PLP] RAM Llena. " + p.getNombre() + " enviado a Disco (Listo/Susp).");
            }
        }
    }
    public static void planificarMedianoPlazo() {
        
        while (getProcesosEnRAM() < Nucleo.LIMITE_MEMORIA_RAM) {
            if (!Nucleo.colaReadySuspended.estaVacia()) {
                PCB p = Nucleo.colaReadySuspended.desencolar();
                if (p != null) {
                    p.setEstado(EstadoProceso.LISTO);
                    Nucleo.colaListos.encolar(p);
                    System.out.println("[PMP] Espacio disponible: Subiendo " + p.getNombre() + " a RAM.");
                }
            } 
            else if (!Nucleo.colaBlockedSuspended.estaVacia()) {
                PCB p = Nucleo.colaBlockedSuspended.desencolar();
                if (p != null) {
                    p.setEstado(EstadoProceso.BLOQUEADO);
                    Nucleo.colaBloqueados.encolar(p);
                    System.out.println("[PMP] Espacio disponible: Subiendo bloqueado " + p.getNombre() + " a RAM.");
                }
            } 
            else {
                break; 
            }
        }

        if (!Nucleo.colaReadySuspended.estaVacia() && getProcesosEnRAM() >= Nucleo.LIMITE_MEMORIA_RAM) {
            
            PCB masUrgenteDisco = buscarMejorCandidato(Nucleo.colaReadySuspended, "EDF");
            
            if (masUrgenteDisco != null) {
                PCB peorBloqueado = buscarPeorCandidato(Nucleo.colaBloqueados);
                PCB peorListo = buscarPeorCandidato(Nucleo.colaListos);
                
                PCB victima = null;
                if (peorBloqueado != null) {
                    victima = peorBloqueado;
                } else {
                    victima = peorListo;
                }

                if (victima != null && masUrgenteDisco.getDeadline() < victima.getDeadline()) {
                    
                    System.out.println("[SWAP] Preempción de Memoria: Sacando " + victima.getNombre() + 
                                       " (Deadline: " + victima.getDeadline() + ") por " + masUrgenteDisco.getNombre() + 
                                       " (Deadline: " + masUrgenteDisco.getDeadline() + ")");
                    
                    if (Nucleo.colaBloqueados.remover(victima)) {
                        victima.setEstado(EstadoProceso.BLOQUEADO_SUSPENDIDO);
                        Nucleo.colaBlockedSuspended.encolar(victima);
                    } else if (Nucleo.colaListos.remover(victima)) {
                        victima.setEstado(EstadoProceso.LISTO_SUSPENDIDO);
                        Nucleo.colaReadySuspended.encolar(victima);
                    }
                    
                    Nucleo.colaReadySuspended.remover(masUrgenteDisco);
                    masUrgenteDisco.setEstado(EstadoProceso.LISTO);
                    Nucleo.colaListos.encolar(masUrgenteDisco);
                }
            }
        }
    }
    
     public static void gestionarBloqueados() {
        
        if (Nucleo.procesoEnEjecucion != null && Math.random() < Nucleo.PROBABILIDAD_IO) {
            PCB p = Nucleo.procesoEnEjecucion;
            p.setEstado(EstadoProceso.BLOQUEADO);
            Nucleo.colaBloqueados.encolar(p);
            
            System.out.println("[I/O] Solicitud de E/S: " + p.getNombre() + " pasa a Bloqueado (RAM).");
            
            Nucleo.procesoEnEjecucion = null;
            Nucleo.contadorQuantum = 0;
        }

        int tamanoBloq = Nucleo.colaBloqueados.getTamano();
        for (int i = 0; i < tamanoBloq; i++) {
            PCB p = Nucleo.colaBloqueados.desencolar();
            if (p == null) continue;

            if (Math.random() < 0.15) {
                p.setEstado(EstadoProceso.LISTO);
                Nucleo.colaListos.encolar(p);
                System.out.println("[I/O] Evento Finalizado: " + p.getNombre() + " vuelve a LISTO (RAM).");
            } else {
                Nucleo.colaBloqueados.encolar(p);
            }
        }

        int tamanoSusp = Nucleo.colaBlockedSuspended.getTamano();
        for (int i = 0; i < tamanoSusp; i++) {
            PCB p = Nucleo.colaBlockedSuspended.desencolar();
            if (p == null) continue;

            if (Math.random() < 0.15) {
                p.setEstado(EstadoProceso.LISTO_SUSPENDIDO);
                Nucleo.colaReadySuspended.encolar(p);
                System.out.println("[I/O] Evento en Disco Finalizado: " + p.getNombre() + " pasa a LISTO_SUSPENDIDO.");
            } else {
                Nucleo.colaBlockedSuspended.encolar(p);
            }
        }
    }

    private static int getProcesosEnRAM() {
    int listos = Nucleo.colaListos.getTamano();
    int bloqueados = Nucleo.colaBloqueados.getTamano();
    int ejecucion = (Nucleo.procesoEnEjecucion != null) ? 1 : 0;
    
    return listos + bloqueados + ejecucion;
}

    private static PCB buscarMejorCandidato(Cola<PCB> cola, String criterio) {
        if (cola == null || cola.estaVacia()) return null;

        PCB mejor = cola.obtenerPorIndice(0);
        
        for (int i = 1; i < cola.getTamano(); i++) {
            PCB actual = cola.obtenerPorIndice(i);
            if (actual == null) continue;

            if (criterio.equals("SRT")) {
                int restMejor = mejor.getInstruccionesTotales() - mejor.getProgramCounter();
                int restActual = actual.getInstruccionesTotales() - actual.getProgramCounter();
                if (restActual < restMejor) mejor = actual;
            } 
            else if (criterio.equals("PRIORIDAD")) {
                if (actual.getPrioridad() < mejor.getPrioridad()) mejor = actual;
            } 
            else if (criterio.equals("EDF")) {
                if (actual.getDeadline() < mejor.getDeadline()) mejor = actual;
            }
        }
        return mejor;
    }

    private static PCB buscarPeorCandidato(Cola<PCB> cola) {
        if (cola == null || cola.estaVacia()) return null;

        PCB peor = cola.obtenerPorIndice(0);
        
        for (int i = 1; i < cola.getTamano(); i++) {
            PCB actual = cola.obtenerPorIndice(i);
            if (actual == null) continue;

            if (actual.getDeadline() > peor.getDeadline()) {
                peor = actual;
            }
        }
        return peor;
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
    procesarColaDeadlines(Nucleo.colaBloqueados);
    procesarColaDeadlines(Nucleo.colaReadySuspended);
    procesarColaDeadlines(Nucleo.colaBlockedSuspended);
    
}

    private static void procesarColaDeadlines(Cola<PCB> cola) {
        int t = cola.getTamano();
        for (int i = 0; i < t; i++) {
            PCB p = cola.desencolar();
            p.decrementarDeadline();
            if (p.getDeadline() <= 0) marcarComoFallido(p);
            else cola.encolar(p);
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

        switch (algoritmo) {
            case "FCFS":
                ejecutarFCFS();
                break;
            case "Round Robin":
                ejecutarRoundRobin();
                break;
            case "SRT":
                ejecutarSRT();
                break;
            case "Prioridad": 
                ejecutarPrioridad();
                break;
            case "EDF":
                ejecutarEDF();
                break;
            default:
                ejecutarFCFS();
                break;
        }
    }


    private static void ejecutarFCFS() {
        if (Nucleo.procesoEnEjecucion == null && !Nucleo.colaListos.estaVacia()) {
            asignarCPU(Nucleo.colaListos.desencolar());
        }
    }

    private static void ejecutarRoundRobin() {
        if (Nucleo.procesoEnEjecucion != null) {
            Nucleo.contadorQuantum++;
            
            if (Nucleo.contadorQuantum >= Nucleo.QUANTUM) {
                System.out.println("[RR] Fin de Quantum para: " + Nucleo.procesoEnEjecucion.getNombre());
                
                PCB saliente = Nucleo.procesoEnEjecucion;
                saliente.setEstado(EstadoProceso.LISTO);
                Nucleo.colaListos.encolar(saliente);
                Nucleo.procesoEnEjecucion = null;
                Nucleo.contadorQuantum = 0;
            }
        }

        if (Nucleo.procesoEnEjecucion == null && !Nucleo.colaListos.estaVacia()) {
            asignarCPU(Nucleo.colaListos.desencolar());
        }
    }

    private static void ejecutarSRT() {
        
        PCB mejorCandidato = buscarMejorCandidato("SRT");
        
        gestionarPreempcion(mejorCandidato, "SRT");
    }

    private static void ejecutarPrioridad() {
        
        PCB mejorCandidato = buscarMejorCandidato("PRIORIDAD");
        
        gestionarPreempcion(mejorCandidato, "Prioridad");
    }

    private static void ejecutarEDF() {
        
        PCB mejorCandidato = buscarMejorCandidato("EDF");
        
        gestionarPreempcion(mejorCandidato, "EDF");
    }


    private static void gestionarPreempcion(PCB candidato, String algoritmo) {
        if (candidato == null) return;

        if (Nucleo.procesoEnEjecucion == null) {
            Nucleo.colaListos.remover(candidato);
            asignarCPU(candidato);
            return;
        }

        boolean debePreemptar = false;

        if (algoritmo.equals("SRT")) {
            int restanteActual = Nucleo.procesoEnEjecucion.getInstruccionesTotales() - Nucleo.procesoEnEjecucion.getProgramCounter();
            int restanteCandidato = candidato.getInstruccionesTotales() - candidato.getProgramCounter();
            if (restanteCandidato < restanteActual) debePreemptar = true;
        } 
        else if (algoritmo.equals("PRIORIDAD")) {
            if (candidato.getPrioridad() < Nucleo.procesoEnEjecucion.getPrioridad()) debePreemptar = true;
        }
        else if (algoritmo.equals("EDF")) {
            if (candidato.getDeadline() < Nucleo.procesoEnEjecucion.getDeadline()) debePreemptar = true;
        }

        if (debePreemptar) {
            System.out.println("[" + algoritmo + "] Preempción: " + candidato.getNombre() + " expulsa a " + Nucleo.procesoEnEjecucion.getNombre());
            
            PCB saliente = Nucleo.procesoEnEjecucion;
            saliente.setEstado(EstadoProceso.LISTO);
            Nucleo.colaListos.encolar(saliente); 
            
            Nucleo.colaListos.remover(candidato);
            asignarCPU(candidato);
        }
    }

    private static PCB buscarMejorCandidato(String criterio) {
        if (Nucleo.colaListos.estaVacia()) return null;

        PCB mejor = Nucleo.colaListos.obtenerPorIndice(0);
        
        for (int i = 1; i < Nucleo.colaListos.getTamano(); i++) {
            PCB actual = Nucleo.colaListos.obtenerPorIndice(i);
            
            if (criterio.equals("SRT")) {
                int restMejor = mejor.getInstruccionesTotales() - mejor.getProgramCounter();
                int restActual = actual.getInstruccionesTotales() - actual.getProgramCounter();
                if (restActual < restMejor) mejor = actual;
            }
            else if (criterio.equals("PRIORIDAD")) {
                if (actual.getPrioridad() < mejor.getPrioridad()) mejor = actual;
            }
            else if (criterio.equals("EDF")) {
                if (actual.getDeadline() < mejor.getDeadline()) mejor = actual;
            }
        }
        return mejor;
    }

    private static void asignarCPU(PCB proceso) {
        proceso.setEstado(EstadoProceso.EJECUCION);
        Nucleo.procesoEnEjecucion = proceso;
        Nucleo.contadorQuantum = 0; 
        System.out.println("[DISPATCHER] CPU asignada a: " + proceso.getNombre());
    }
    
public static void contabilizarEspera() {
    incrementarEsperaEnCola(Nucleo.colaListos);
    incrementarEsperaEnCola(Nucleo.colaReadySuspended);
}

private static void incrementarEsperaEnCola(Cola<PCB> cola) {
    for (int i = 0; i < cola.getTamano(); i++) {
        PCB p = cola.obtenerPorIndice(i);
        if (p != null) p.incrementarEspera();
    }
}
}