package unimet.proyectoso1;

import unimet.proyectoso1.gui.VentanaPrincipal;
import unimet.proyectoso1.sistema.Reloj;
import unimet.proyectoso1.sistema.GeneradorMisiones;

public class ProyectoSO1 {

    public static void main(String[] args) {
        VentanaPrincipal ventana = new VentanaPrincipal();
        
        ventana.generarProcesosLote(10);
        
        GeneradorMisiones generador = new GeneradorMisiones(ventana);
        generador.start();

        Reloj reloj = new Reloj(ventana);
        reloj.start();
        
        System.out.println("[SISTEMA] RTOS iniciado con éxito. Hilos de Reloj y Generador activos.");
    }
}