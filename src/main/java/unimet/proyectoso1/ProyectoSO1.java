

package unimet.proyectoso1;


import unimet.proyectoso1.modelo.PCB;
import unimet.proyectoso1.sistema.Nucleo;
import unimet.proyectoso1.sistema.Reloj;

public class ProyectoSO1 {
    public static void main(String[] args) {
        Nucleo.colaNuevos.encolar(new PCB(1, "Sistema Navegacion", 1, 5, 100, 0));
        Nucleo.colaNuevos.encolar(new PCB(2, "Radio Telemetria", 2, 8, 200, 0));
        Nucleo.colaNuevos.encolar(new PCB(3, "Sensores Temperatura", 3, 4, 150, 0));
        Nucleo.colaNuevos.encolar(new PCB(4, "Camara Espacial", 2, 6, 300, 0)); 
        Nucleo.colaNuevos.encolar(new PCB(5, "Panel Solar", 1, 3, 500, 0));     

        Reloj reloj = new Reloj(1000); 
        
        System.out.println("=== BOOTING SATELLITE RTOS ===");
        reloj.start(); 
    }
}
