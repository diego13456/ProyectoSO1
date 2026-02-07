
package unimet.proyectoso1.sistema;


import java.util.concurrent.Semaphore;
import unimet.proyectoso1.estructuras.Cola;
import unimet.proyectoso1.modelo.PCB;

public class Nucleo {
    public static Cola<PCB> colaNuevos = new Cola<>();
    public static Cola<PCB> colaListos = new Cola<>();
    public static Cola<PCB> colaTerminados = new Cola<>(); 
    
    public static PCB procesoEnEjecucion = null;

    public static int relojDelSistema = 0;
    public static final int LIMITE_MEMORIA_RAM = 3; 


    public static final Semaphore mutex = new Semaphore(1);
}