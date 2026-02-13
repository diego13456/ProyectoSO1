package unimet.proyectoso1;

import unimet.proyectoso1.gui.VentanaPrincipal;
import unimet.proyectoso1.sistema.Reloj;
import unimet.proyectoso1.sistema.GeneradorMisiones;

public class ProyectoSO1 {

    public static void main(String[] args) {
        // 1. Inicializar la Ventana (Hilo de la interfaz GUI)
        VentanaPrincipal ventana = new VentanaPrincipal();
        
        // 2. Generar una carga inicial de procesos en el sistema
        ventana.generarProcesosLote(10);
        
        // 3. Iniciar el hilo de Generador de Misiones (Concurrencia de entrada de datos)
        // Este hilo simula los procesos externos requeridos por el PDF
        GeneradorMisiones generador = new GeneradorMisiones(ventana);
        generador.start();

        // 4. Iniciar el hilo del Reloj (Simulación del procesamiento y planificación)
        Reloj reloj = new Reloj(ventana);
        reloj.start();
        
        System.out.println("[SISTEMA] RTOS iniciado con éxito. Hilos de Reloj y Generador activos.");
    }
}