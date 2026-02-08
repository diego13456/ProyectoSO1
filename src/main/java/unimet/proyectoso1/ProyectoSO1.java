package unimet.proyectoso1;

import unimet.proyectoso1.gui.VentanaPrincipal;
import unimet.proyectoso1.sistema.Reloj;

public class ProyectoSO1 {

    public static void main(String[] args) {
        VentanaPrincipal ventana = new VentanaPrincipal();
        ventana.generarProcesosLote(10);
        Reloj reloj = new Reloj(ventana);
        
        reloj.start();
    }
}