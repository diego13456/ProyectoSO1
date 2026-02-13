package unimet.proyectoso1.sistema;

import java.util.concurrent.Semaphore;
import unimet.proyectoso1.estructuras.Cola;
import unimet.proyectoso1.modelo.PCB;

public class Nucleo {
    public static Cola<PCB> colaNuevos = new Cola<>();
    public static Cola<PCB> colaListos = new Cola<>();
    public static Cola<PCB> colaBloqueados = new Cola<>();
    public static Cola<PCB> colaReadySuspended = new Cola<>();
    public static Cola<PCB> colaBlockedSuspended = new Cola<>();
    public static Cola<PCB> colaTerminados = new Cola<>(); 

    public static PCB procesoEnEjecucion = null;
    public static int relojDelSistema = 0;
    public static final int LIMITE_MEMORIA_RAM = 3;

    public static int misionesExitosas = 0;
    public static int misionesFallidas = 0;
    public static int ciclosCPUOcupada = 0;
    public static long sumatoriaTiempoEspera = 0; 
    public static int totalProcesosFinalizados = 0;

    public static final Semaphore mutex = new Semaphore(1);
    public static int contadorQuantum = 0;
    public static final int QUANTUM = 4;
    public static final double PROBABILIDAD_IO = 0.05;
    public static volatile boolean bajoInterrupcion = false;
    public static volatile int ciclosRestantesISR = 0;

    public static double getTasaExito() {
        int total = misionesExitosas + misionesFallidas;
        return (total == 0) ? 0 : ((double) misionesExitosas / total) * 100;
    }

    public static double getUtilizacionCPU() {
        if (relojDelSistema == 0) return 0;
        return ((double) ciclosCPUOcupada / relojDelSistema) * 100;
    }

    public static double getPromedioEspera() {
        if (totalProcesosFinalizados == 0) return 0;
        return (double) sumatoriaTiempoEspera / totalProcesosFinalizados;
    }
}