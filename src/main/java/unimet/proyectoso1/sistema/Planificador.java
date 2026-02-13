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
        
        // --- 1. CARGA DINÁMICA: Si hay espacio en RAM, subir procesos del Disco ---
        // Este bloque se asegura de llenar la RAM hasta el límite antes de intentar swapping
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

        // --- 2. SWAPPING PREEMPTIVO: RAM llena, comparar urgencias ---
        // CAMBIO CRÍTICO: Solo entramos aquí si la RAM está realmente llena O excedida
        if (!Nucleo.colaReadySuspended.estaVacia() && getProcesosEnRAM() >= Nucleo.LIMITE_MEMORIA_RAM) {
            
            // Buscamos al mejor candidato en Disco
            PCB masUrgenteDisco = buscarMejorCandidato(Nucleo.colaReadySuspended, "EDF");
            
            if (masUrgenteDisco != null) {
                // Buscamos a la "mejor" víctima para expulsar (la que tenga el Deadline más largo)
                PCB peorBloqueado = buscarPeorCandidato(Nucleo.colaBloqueados);
                PCB peorListo = buscarPeorCandidato(Nucleo.colaListos);
                
                // Determinamos quién es el proceso menos útil en RAM actualmente
                PCB victima = null;
                // Preferimos expulsar a un bloqueado antes que a un listo, 
                // pero SOLO si el de disco es más urgente que el bloqueado.
                if (peorBloqueado != null) {
                    victima = peorBloqueado;
                } else {
                    victima = peorListo;
                }

                // SOLO HACEMOS SWAPPING si el proceso de disco tiene un Deadline menor (es más urgente)
                // que la víctima que está en RAM. 
                if (victima != null && masUrgenteDisco.getDeadline() < victima.getDeadline()) {
                    
                    System.out.println("[SWAP] Preempción de Memoria: Sacando " + victima.getNombre() + 
                                       " (Deadline: " + victima.getDeadline() + ") por " + masUrgenteDisco.getNombre() + 
                                       " (Deadline: " + masUrgenteDisco.getDeadline() + ")");
                    
                    // 1. Sacar de RAM y mover a la cola de suspendidos correspondiente
                    if (Nucleo.colaBloqueados.remover(victima)) {
                        victima.setEstado(EstadoProceso.BLOQUEADO_SUSPENDIDO);
                        Nucleo.colaBlockedSuspended.encolar(victima);
                    } else if (Nucleo.colaListos.remover(victima)) {
                        victima.setEstado(EstadoProceso.LISTO_SUSPENDIDO);
                        Nucleo.colaReadySuspended.encolar(victima);
                    }
                    
                    // 2. Subir el urgente de disco a RAM
                    Nucleo.colaReadySuspended.remover(masUrgenteDisco);
                    masUrgenteDisco.setEstado(EstadoProceso.LISTO);
                    Nucleo.colaListos.encolar(masUrgenteDisco);
                }
            }
        }
    }
    
     public static void gestionarBloqueados() {
        
        // 1. EVENTO DE BLOQUEO: El proceso en ejecución solicita I/O
        if (Nucleo.procesoEnEjecucion != null && Math.random() < Nucleo.PROBABILIDAD_IO) {
            PCB p = Nucleo.procesoEnEjecucion;
            p.setEstado(EstadoProceso.BLOQUEADO);
            Nucleo.colaBloqueados.encolar(p);
            
            System.out.println("[I/O] Solicitud de E/S: " + p.getNombre() + " pasa a Bloqueado (RAM).");
            
            // Liberar CPU y resetear quantum
            Nucleo.procesoEnEjecucion = null;
            Nucleo.contadorQuantum = 0;
        }

        // 2. DESBLOQUEO EN RAM: Procesos en cola BLOQUEADO que terminan su espera
        int tamanoBloq = Nucleo.colaBloqueados.getTamano();
        for (int i = 0; i < tamanoBloq; i++) {
            PCB p = Nucleo.colaBloqueados.desencolar();
            if (p == null) continue;

            // Simulación: 15% de probabilidad de terminar el I/O en este ciclo
            if (Math.random() < 0.15) {
                p.setEstado(EstadoProceso.LISTO);
                Nucleo.colaListos.encolar(p);
                System.out.println("[I/O] Evento Finalizado: " + p.getNombre() + " vuelve a LISTO (RAM).");
            } else {
                // Si no ha terminado, vuelve a la cola de bloqueados
                Nucleo.colaBloqueados.encolar(p);
            }
        }

        // 3. DESBLOQUEO EN DISCO: Procesos en BLOQUEADO_SUSPENDIDO que terminan su espera
        int tamanoSusp = Nucleo.colaBlockedSuspended.getTamano();
        for (int i = 0; i < tamanoSusp; i++) {
            PCB p = Nucleo.colaBlockedSuspended.desencolar();
            if (p == null) continue;

            // Si termina su I/O mientras está en el disco
            if (Math.random() < 0.15) {
                // Pasa de "Bloqueado en Disco" a "Listo en Disco"
                p.setEstado(EstadoProceso.LISTO_SUSPENDIDO);
                Nucleo.colaReadySuspended.encolar(p);
                System.out.println("[I/O] Evento en Disco Finalizado: " + p.getNombre() + " pasa a LISTO_SUSPENDIDO.");
            } else {
                // Sigue esperando I/O en el disco
                Nucleo.colaBlockedSuspended.encolar(p);
            }
        }
    }

    private static int getProcesosEnRAM() {
    // Solo cuentan los que están físicamente en la memoria RAM
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

    // --- BUSCAR AL PEOR (El menos urgente, el que tiene el deadline más lejano) ---
    // Se usa para decidir a quién sacar de la RAM al disco
    private static PCB buscarPeorCandidato(Cola<PCB> cola) {
        if (cola == null || cola.estaVacia()) return null;

        PCB peor = cola.obtenerPorIndice(0);
        
        for (int i = 1; i < cola.getTamano(); i++) {
            PCB actual = cola.obtenerPorIndice(i);
            if (actual == null) continue;

            // El "peor" es el que tiene el Deadline más alto (le queda más tiempo)
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
    
    // Solo decrementa a los que ya entraron al sistema operativo
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
        // Si no hay nadie en CPU y nadie en lista, nada que hacer
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
            case "Prioridad": // Prioridad Estática Preemptiva
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

    // --- ALGORITMOS ---

    private static void ejecutarFCFS() {
        // No expropiativo. Solo asigna si está libre.
        if (Nucleo.procesoEnEjecucion == null && !Nucleo.colaListos.estaVacia()) {
            asignarCPU(Nucleo.colaListos.desencolar());
        }
    }

    private static void ejecutarRoundRobin() {
        // Lógica de expropiación por Quantum
        if (Nucleo.procesoEnEjecucion != null) {
            Nucleo.contadorQuantum++;
            
            if (Nucleo.contadorQuantum >= Nucleo.QUANTUM) {
                System.out.println("[RR] Fin de Quantum para: " + Nucleo.procesoEnEjecucion.getNombre());
                
                // Context Switch: Volver a cola de listos
                PCB saliente = Nucleo.procesoEnEjecucion;
                saliente.setEstado(EstadoProceso.LISTO);
                Nucleo.colaListos.encolar(saliente);
                Nucleo.procesoEnEjecucion = null;
                Nucleo.contadorQuantum = 0;
            }
        }

        // Si CPU está libre, tomar el siguiente (FCFS logic)
        if (Nucleo.procesoEnEjecucion == null && !Nucleo.colaListos.estaVacia()) {
            asignarCPU(Nucleo.colaListos.desencolar());
        }
    }

    private static void ejecutarSRT() {
        // Shortest Remaining Time (Expropiativo)
        // Busca el proceso con MENOR tiempo restante (Instrucciones Totales - PC)
        
        PCB mejorCandidato = buscarMejorCandidato("SRT");
        
        gestionarPreempcion(mejorCandidato, "SRT");
    }

    private static void ejecutarPrioridad() {
        // Prioridad Estática Preemptiva
        // Asumimos que MENOR valor numérico es MAYOR prioridad (ej. 1 es más urgente que 5).
        
        PCB mejorCandidato = buscarMejorCandidato("PRIORIDAD");
        
        gestionarPreempcion(mejorCandidato, "Prioridad");
    }

    private static void ejecutarEDF() {
        // Earliest Deadline First (Expropiativo)
        // Busca el proceso con MENOR deadline absoluto.
        
        PCB mejorCandidato = buscarMejorCandidato("EDF");
        
        gestionarPreempcion(mejorCandidato, "EDF");
    }

    // --- UTILIDADES ---

    private static void gestionarPreempcion(PCB candidato, String algoritmo) {
        if (candidato == null) return;

        // Si no hay nadie ejecutando, el candidato entra directo
        if (Nucleo.procesoEnEjecucion == null) {
            // Remover de la cola (OJO: puede estar en medio de la cola)
            Nucleo.colaListos.remover(candidato);
            asignarCPU(candidato);
            return;
        }

        // Si hay alguien ejecutando, comparamos para ver si hay preempción
        boolean debePreemptar = false;

        if (algoritmo.equals("SRT")) {
            int restanteActual = Nucleo.procesoEnEjecucion.getInstruccionesTotales() - Nucleo.procesoEnEjecucion.getProgramCounter();
            int restanteCandidato = candidato.getInstruccionesTotales() - candidato.getProgramCounter();
            if (restanteCandidato < restanteActual) debePreemptar = true;
        } 
        else if (algoritmo.equals("PRIORIDAD")) {
            // Menor valor = Mayor prioridad
            if (candidato.getPrioridad() < Nucleo.procesoEnEjecucion.getPrioridad()) debePreemptar = true;
        }
        else if (algoritmo.equals("EDF")) {
            if (candidato.getDeadline() < Nucleo.procesoEnEjecucion.getDeadline()) debePreemptar = true;
        }

        if (debePreemptar) {
            System.out.println("[" + algoritmo + "] Preempción: " + candidato.getNombre() + " expulsa a " + Nucleo.procesoEnEjecucion.getNombre());
            
            // Sacar al actual
            PCB saliente = Nucleo.procesoEnEjecucion;
            saliente.setEstado(EstadoProceso.LISTO);
            Nucleo.colaListos.encolar(saliente); // Vuelve al final (o debería ser ordenado? En SRT/Prioridad se reordenará al buscar next)
            
            // Sacar al candidato de la cola y ponerlo en CPU
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
        Nucleo.contadorQuantum = 0; // Resetear quantum al entrar
        System.out.println("[DISPATCHER] CPU asignada a: " + proceso.getNombre());
    }
}