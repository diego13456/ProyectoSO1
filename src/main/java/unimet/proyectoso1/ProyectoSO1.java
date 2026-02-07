package unimet.proyectoso1;

import unimet.proyectoso1.gui.VentanaPrincipal;
import unimet.proyectoso1.modelo.PCB;
import unimet.proyectoso1.sistema.Nucleo;
import unimet.proyectoso1.sistema.Reloj;

public class ProyectoSO1 {
    public static void main(String[] args) {
        Nucleo.colaNuevos.encolar(new PCB(1, "Sistema Navegacion", 1, 15, 100, 5));
        Nucleo.colaNuevos.encolar(new PCB(2, "Radio Telemetria", 2, 20, 200, 8));
        Nucleo.colaNuevos.encolar(new PCB(3, "Sensores Temperatura", 3, 10, 150, 3));
        Nucleo.colaNuevos.encolar(new PCB(4, "Camara Espacial", 2, 12, 300, 6)); 
        Nucleo.colaNuevos.encolar(new PCB(5, "Panel Solar", 1, 8, 500, 4));     

        VentanaPrincipal ventana = new VentanaPrincipal();
        
        Reloj reloj = new Reloj(1000, ventana); 
        
        System.out.println("=== BOOTING SATELLITE RTOS ===");
        ventana.log("Sistemas de control satelital iniciados.");
        
        reloj.start(); 
    }
}