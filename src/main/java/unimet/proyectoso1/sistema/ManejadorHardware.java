package unimet.proyectoso1.sistema;

import unimet.proyectoso1.gui.VentanaPrincipal;

public class ManejadorHardware {
    private VentanaPrincipal gui;

    public ManejadorHardware(VentanaPrincipal gui) {
        this.gui = gui;
    }

    public void activarInterrupcion() {
    Thread hiloInterrupcion = new Thread(() -> {
        try {
            Nucleo.mutex.acquire(); 
            Nucleo.bajoInterrupcion = true;
            Nucleo.ciclosRestantesISR = 5;
            gui.log("!!! ALERTA: MICRO-METEORITO !!! Atendiendo ISR en hilo independiente...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Nucleo.mutex.release();
        }
    });
    hiloInterrupcion.start();
}
}
