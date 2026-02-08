package unimet.proyectoso1.sistema;

import unimet.proyectoso1.gui.VentanaPrincipal;

public class ManejadorHardware {
    private VentanaPrincipal gui;

    public ManejadorHardware(VentanaPrincipal gui) {
        this.gui = gui;
    }

    public void activarInterrupcion() {
        Thread isr = new Thread(() -> {
            try {
                Nucleo.bajoInterrupcion = true; // Usamos la variable global del Nucleo
                gui.log("!!! ALERTA: MICRO-METEORITO !!! Suspendiendo procesos...");
                
                Thread.sleep(2500); 
                
                gui.log("ISR Finalizada: Sistema estabilizado.");
            } catch (InterruptedException e) {
                gui.log("Error en ISR.");
            } finally {
                Nucleo.bajoInterrupcion = false;
            }
        });
        isr.setPriority(Thread.MAX_PRIORITY);
        isr.start();
    }
}
