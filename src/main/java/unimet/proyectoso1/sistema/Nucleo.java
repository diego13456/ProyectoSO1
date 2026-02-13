package unimet.proyectoso1.sistema;

import java.util.concurrent.Semaphore;
import unimet.proyectoso1.estructuras.Cola;
import unimet.proyectoso1.modelo.PCB;

public class Nucleo {
    public static Cola<PCB> colaNuevos = new Cola<>();
    public static Cola<PCB> colaListos = new Cola<>();
    public static Cola<PCB> colaTerminados = new Cola<>();
    public static Cola<PCB> colaBloqueados = new Cola<>();       // RAM
    public static Cola<PCB> colaReadySuspended = new Cola<>();   // DISCO
    public static Cola<PCB> colaBlockedSuspended = new Cola<>(); // DISCO
    
    public static PCB procesoEnEjecucion = null;

    public static int relojDelSistema = 0;
    public static final int LIMITE_MEMORIA_RAM = 3; // Aumentado para ver mejor los efectos

    // Variables para Round Robin
    public static final int QUANTUM = 4; // Cada proceso tiene 4 ciclos de CPU
    public static int contadorQuantum = 0; 

    public static volatile boolean bajoInterrupcion = false; 
    public static volatile int ciclosRestantesISR = 0; 

    public static final Semaphore mutex = new Semaphore(1);
    public static int misionesExitosas = 0;
    public static int misionesFallidas = 0;

    public static double getTasaExito() {
        int total = misionesExitosas + misionesFallidas;
        if (total == 0) return 0.0;
        return ((double) misionesExitosas / total) * 100;
    }
    
    public static final double PROBABILIDAD_IO = 0.05; 
}