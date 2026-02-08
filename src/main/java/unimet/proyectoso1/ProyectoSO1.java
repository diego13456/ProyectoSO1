package unimet.proyectoso1;

import unimet.proyectoso1.gui.VentanaPrincipal;
import unimet.proyectoso1.sistema.Reloj;

public class ProyectoSO1 {

    public static void main(String[] args) {
        // 1. Creamos la interfaz
        VentanaPrincipal ventana = new VentanaPrincipal();
        
        // 2. CORRECCIÓN: Pasamos SOLAMENTE la ventana al constructor del Reloj
        Reloj reloj = new Reloj(ventana);
        
        // 3. Iniciamos el hilo del tiempo
        reloj.start();
    }
}